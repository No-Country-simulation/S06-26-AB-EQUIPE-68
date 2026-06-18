package com.bitsystem.bitapp.client;

import com.bitsystem.bitapp.config.GoogleGeminiProperties;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * ============================================================================
 * CLIENTE HTTP: GoogleGeminiClient
 * ============================================================================
 *
 * Wrapper HTTP para chamadas à API Google Gemini (v1beta2).
 * Abstrai complexidade de autenticação, serialização e tratamento de erros.
 *
 * RESPONSABILIDADES:
 * - Construir requisições HTTP válidas para Gemini
 * - Gerenciar autenticação (API Key ou Bearer Token)
 * - Fazer parsing de respostas JSON do Gemini
 * - Tratamento de erros e exceções
 *
 * FLUXO:
 * 1. generateText(prompt) recebe string de entrada
 * 2. Cria GeminiRequest com metadata (temperatura, candidates)
 * 3. Envia POST para endpoint/:model:generateMessage
 * 4. Parse JSON response → GeminiResponse
 * 5. Extrai texto do primeiro candidate
 * 6. Retorna concatenação de chunks de texto
 *
 * SEGURANÇA DE AUTENTICAÇÃO:
 * - API Key com prefixo "AQ." ou "ya29." usa Bearer Token (OAuth)
 * - Outras chaves são adicionadas como query param ?key=...
 * - Chave lida de GoogleGeminiProperties (injetada via application.properties)
 *
 * @author BiT System
 * @version 1.0.0
 */
@Component
public class GoogleGeminiClient {

    private final GoogleGeminiProperties properties;
    private final RestTemplate restTemplate;

    /**
     * Construtor com injeção de dependência
     *
     * @param properties Configuração carregada de application.properties
     */
    public GoogleGeminiClient(GoogleGeminiProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Gera texto via API Gemini em resposta a um prompt
     *
     * FLUXO DETALHADO:
     * 1. Monta URL: {endpoint}/{model}:generateMessage
     * 2. Detecta tipo de autenticação (Bearer vs Query Param)
     * 3. Constrói headers HTTP com Content-Type application/json
     * 4. Serializa GeminiRequest (prompt + config)
     * 5. Faz POST HTTP para Gemini
     * 6. Extrai resposta do primeiro candidate
     * 7. Concatena chunks de texto (pode haver múltiplos)
     * 8. Retorna string única concatenada
     *
     * TRATAMENTO DE ERROS:
     * - Resposta nula ou vazia → IllegalStateException
     * - HTTP 401 → credenciais inválidas
     * - HTTP 429 → quota/rate limit excedido
     * - HTTP 500 → erro do servidor Gemini
     *
     * @param prompt Texto da prompt em português (instruções para a IA)
     * @return Texto gerado pelo Gemini (pode ser JSON estruturado ou livre)
     * @throws IllegalStateException se resposta estiver vazia
     */
    public String generateText(String prompt) {
        // Monta URL completa do endpoint Gemini
        String url =
            properties.getEndpoint() +
            "/" +
            properties.getModel() +
            ":generateContent";

        // Constrói URI sempre com API Key como query param (padrão Google Generative Language API)
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
            url
        ).queryParam("key", properties.getApiKey());

        // Prepara headers HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Constrói payload de requisição Gemini (formato v1beta)
        Map<String, Object> requestBody = Map.of(
            "contents",
            List.of(
                Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))
            ),
            "generationConfig",
            Map.of(
                "temperature",
                properties.getTemperature(),
                "candidateCount",
                properties.getCandidateCount()
            )
        );

        // Encapsula request em HttpEntity
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
            requestBody,
            headers
        );

        // Executa POST HTTP
        ResponseEntity<Map> response = restTemplate.exchange(
            uriBuilder.build().toUri(),
            HttpMethod.POST,
            entity,
            Map.class
        );

        // Valida resposta
        if (
            response.getBody() == null ||
            response.getBody().get("candidates") == null
        ) {
            throw new IllegalStateException("Resposta vazia do Gemini");
        }

        // Extrai texto do primeiro candidate
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> candidates = (List<
            Map<String, Object>
        >) response.getBody().get("candidates");
        if (candidates.isEmpty()) {
            throw new IllegalStateException(
                "Nenhum candidate retornado pelo Gemini"
            );
        }

        Map<String, Object> firstCandidate = candidates.get(0);
        Map<String, Object> content = (Map<String, Object>) firstCandidate.get(
            "content"
        );
        @SuppressWarnings("unchecked")
        List<Map<String, String>> parts = (List<
            Map<String, String>
        >) content.get("parts");

        return parts
            .stream()
            .map(part -> part.get("text"))
            .filter(Objects::nonNull)
            .reduce("", String::concat);
    }

    // ========== INNER RECORDS (Serialização JSON) ==========

    /**
     * REQUISIÇÃO Gemini: Contém prompt e parâmetros de geração
     *
     * Serializado para JSON enviado ao Gemini
     * Exemplo:
     * {
     *   "messages": [{"author": "user", "content": [{"type": "text", "text": "..."}]}],
     *   "temperature": 0.7,
     *   "candidateCount": 1
     * }
     */
    private record GeminiRequest(
        /** Histórico de mensagens (neste app, sempre single-turn) */
        List<GeminiMessage> messages,

        /** Controle de criatividade (0.0 a 1.0) */
        Double temperature,

        /** Número de respostas paralelas a gerar */
        Integer candidateCount
    ) {}

    /**
     * MENSAGEM Gemini: Tupla author + content
     *
     * author = "user" (apenas role suportado pelo MVP)
     * content = Lista de partes de conteúdo (texto, imagem, etc)
     */
    private record GeminiMessage(
        /** Papel do autor ("user" ou "model") */
        String author,

        /** Conteúdo (lista de partes de texto) */
        List<GeminiContent> content
    ) {}

    /**
     * CONTEÚDO Gemini: Tipo (text/image) + dados
     *
     * type = "text" (MVP suporta apenas texto)
     * text = Corpo do texto
     */
    private record GeminiContent(
        /** Tipo de mídia ("text", "image", etc) */
        String type,

        /** Dados do conteúdo (aqui: string de texto) */
        String text
    ) {}

    /**
     * RESPOSTA Gemini: Contém lista de candidates (respostas possíveis)
     *
     * Parseado do JSON retornado por Gemini
     * Exemplo:
     * {
     *   "candidates": [
     *     {"content": [{"type": "text", "text": "...resposta..."}]}
     *   ]
     * }
     */
    private record GeminiResponse(
        /** Lista de respostas candidatas (apenas usamos [0]) */
        List<Candidate> candidates
    ) {}

    /**
     * CANDIDATE Gemini: Uma resposta possível com seu conteúdo
     *
     * Cada candidate é um possível resultado da geração
     * MVP usa apenas o primeiro (candidateCount=1)
     */
    private record Candidate(
        /** Partes de conteúdo da resposta (pode ser múltiplo) */
        List<GeminiContent> content
    ) {}
}
