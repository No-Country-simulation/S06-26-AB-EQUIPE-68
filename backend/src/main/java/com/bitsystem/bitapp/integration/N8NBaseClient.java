package com.bitsystem.bitapp.integration;

import com.bitsystem.bitapp.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

public abstract class N8NBaseClient {

    private static final Logger log = LoggerFactory.getLogger(N8NBaseClient.class);

    /** Cabeçalho que o nó "Webhook Entrada" do n8n confere (Header Auth). */
    public static final String HEADER_TOKEN = "X-Webhook-Token";

    // Só para medir o tamanho do payload no log: o conteúdo nunca é registrado.
    private static final ObjectMapper MEDIDOR = new ObjectMapper();

    /** Tamanho máximo, em caracteres, do corpo ou da mensagem de uma falha no log (já com o "…"). */
    private static final int LIMITE_DO_RESUMO = 200;
    private static final Pattern ESPACOS = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);

    protected final RestTemplate restTemplate;
    /** Rótulo do tipo de chamada ("mental-health", "assessment"), usado só no log. */
    protected final String tipo;
    protected final String webhookUrl;
    protected final int connectTimeout;
    protected final int readTimeout;
    protected final int maxRetries;
    /** Valor de N8N_WEBHOOK_TOKEN; vazio = o webhook do n8n não exige cabeçalho. */
    private final String webhookToken;

    /**
     * Cada tipo de chamada tem os seus tempos (em ms): o check-in é curto porque há um painel de
     * segurança esperando a resposta; a avaliação de carreira pode demorar mais. O pior caso de
     * uma chamada é connectTimeout + readTimeout, multiplicado por maxRetries.
     */
    public N8NBaseClient(String tipo, String webhookUrl, int connectTimeout, int readTimeout,
            int maxRetries, String webhookToken) {
        this.tipo = tipo;
        this.webhookToken = webhookToken;
        this.webhookUrl = webhookUrl;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.maxRetries = maxRetries;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        factory.setReadTimeout(Duration.ofMillis(readTimeout));
        this.restTemplate = new RestTemplate(factory);
    }

    /** false quando a URL do webhook está vazia: o chamador pula o n8n e usa o fallback local. */
    public boolean isConfigured() {
        return webhookUrl != null && !webhookUrl.isBlank();
    }

    protected <T, R> R callWebhook(T request, Class<R> responseType) {
        if (!isConfigured()) {
            throw new BusinessException("N8N_UNAVAILABLE", "Webhook do n8n não configurado.");
        }
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                if (webhookToken != null && !webhookToken.isBlank()) {
                    // Autenticação por cabeçalho do nó "Webhook Entrada"; o valor nunca vai para o log.
                    headers.set(HEADER_TOKEN, webhookToken);
                }

                HttpEntity<T> entity = new HttpEntity<>(request, headers);

                // Só metadados: o payload carrega o texto livre do check-in e não vai para o log.
                log.info("Chamando webhook n8n [tipo={}] tentativa {}/{} (payload de {} bytes)",
                        tipo, attempt, maxRetries, tamanhoEmBytes(request));
                ResponseEntity<R> response = restTemplate.postForEntity(
                        webhookUrl, entity, responseType);
                log.debug("Resposta do n8n [tipo={}]: status {}", tipo, response.getStatusCode());
                return response.getBody();

            } catch (Exception ex) {
                lastException = ex;
                log.warn("N8N webhook call failed (attempt {}/{}): {}", attempt, maxRetries, resumoDaFalha(ex));
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(Duration.ofSeconds(1).toMillis());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        throw new BusinessException("N8N_UNAVAILABLE",
                "Serviço de orquestração temporariamente indisponível. Tente novamente mais tarde.");
    }

    /**
     * Resumo de uma linha da falha, para o log. Quando o n8n (ou o proxy na frente dele) responde
     * com uma página de erro em HTML, o corpo inteiro (~6 KB) não vai para o log; mensagens curtas,
     * como 403 Forbidden: "Authorization data is wrong!", continuam aparecendo inteiras.
     */
    private static String resumoDaFalha(Exception ex) {
        if (ex instanceof RestClientResponseException resposta) {
            String status = (resposta.getStatusCode().value() + " " + resposta.getStatusText()).trim();
            String corpo = emUmaLinha(resposta.getResponseBodyAsString());
            return corpo.isEmpty() ? status : status + ": \"" + corpo + "\"";
        }
        String mensagem = emUmaLinha(ex.getMessage());
        String nome = ex.getClass().getSimpleName();
        return mensagem.isEmpty() ? nome : nome + ": " + mensagem;
    }

    /** Quebras de linha e sequências de espaços viram um espaço só; passando do limite, corta e termina em "…". */
    private static String emUmaLinha(String texto) {
        if (texto == null) {
            return "";
        }
        String linha = ESPACOS.matcher(texto.strip()).replaceAll(" ");
        if (linha.length() <= LIMITE_DO_RESUMO) {
            return linha;
        }
        int corte = LIMITE_DO_RESUMO - 1;
        if (Character.isHighSurrogate(linha.charAt(corte - 1))) {
            corte--; // não parte um par substituto ao meio
        }
        return linha.substring(0, corte) + "…";
    }

    private static int tamanhoEmBytes(Object payload) {
        try {
            return MEDIDOR.writeValueAsBytes(payload).length;
        } catch (Exception ex) {
            return -1;
        }
    }
}
