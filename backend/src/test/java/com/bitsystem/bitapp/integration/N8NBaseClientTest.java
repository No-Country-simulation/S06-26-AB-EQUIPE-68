package com.bitsystem.bitapp.integration;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.bitsystem.bitapp.dto.AssessmentDto;
import com.bitsystem.bitapp.dto.MentalHealthDto;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 * Clientes do n8n contra um servidor HTTP local que faz o papel do webhook:
 * devolve os exemplos de contrato e registra o que recebeu.
 */
class N8NBaseClientTest {

    private HttpServer servidor;
    private final AtomicInteger chamadas = new AtomicInteger();
    private final AtomicReference<String> tokenRecebido = new AtomicReference<>();
    private volatile String corpoDaResposta;
    private ListAppender<ILoggingEvent> logs;
    private Logger loggerDoCliente;

    @BeforeEach
    void subirN8nFalso() throws IOException {
        corpoDaResposta = ContratosN8n.texto("mental-health.response");
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/webhook/bit/agent", troca -> {
            chamadas.incrementAndGet();
            tokenRecebido.set(troca.getRequestHeaders().getFirst(N8NBaseClient.HEADER_TOKEN));
            byte[] corpo = corpoDaResposta.getBytes(StandardCharsets.UTF_8);
            troca.getResponseHeaders().add("Content-Type", "application/json");
            troca.sendResponseHeaders(200, corpo.length);
            troca.getResponseBody().write(corpo);
            troca.close();
        });
        servidor.start();

        loggerDoCliente = (Logger) LoggerFactory.getLogger(N8NBaseClient.class);
        loggerDoCliente.setLevel(Level.DEBUG);
        logs = new ListAppender<>();
        logs.start();
        loggerDoCliente.addAppender(logs);
    }

    @AfterEach
    void derrubar() {
        loggerDoCliente.detachAppender(logs);
        servidor.stop(0);
    }

    private String url() {
        return "http://127.0.0.1:" + servidor.getAddress().getPort() + "/webhook/bit/agent";
    }

    private String logsEmTexto() {
        return String.join("\n", logs.list.stream().map(ILoggingEvent::getFormattedMessage).toList());
    }

    private Map<String, Object> checkin(String relato) {
        return Map.of("userId", 7, "nota", 1, "message", relato, "tipo", "mental-health");
    }

    // ── tempo de espera por tipo de chamada ─────────────────────────────

    @Test
    void leitura_estouraNoTempoDoTipoDeChamada_semNovaTentativa() {
        servidor.createContext("/webhook/lento", troca -> {
            chamadas.incrementAndGet();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            troca.sendResponseHeaders(200, -1);
            troca.close();
        });
        String urlLenta = "http://127.0.0.1:" + servidor.getAddress().getPort() + "/webhook/lento";
        N8NMentalHealthClient checkin = new N8NMentalHealthClient(urlLenta, 1000, 300, 1, "");

        long inicio = System.nanoTime();
        org.junit.jupiter.api.Assertions.assertThrows(
                com.bitsystem.bitapp.exception.BusinessException.class, () -> checkin.process(checkin("qualquer")));
        long ms = (System.nanoTime() - inicio) / 1_000_000;

        assertThat(ms).as("desistiu no tempo de leitura do check-in, sem esperar o n8n lento (3 s)").isLessThan(2500);
        assertThat(chamadas.get()).as("uma tentativa só").isEqualTo(1);
    }

    @Test
    void cadaTipoDeChamadaGuardaOsSeusTempos() {
        N8NMentalHealthClient checkin = new N8NMentalHealthClient(url(), 1000, 5000, 1, "");
        N8NAssessmentClient avaliacao = new N8NAssessmentClient(url(), 2000, 23000, 1, "");

        assertThat(checkin.connectTimeout).isEqualTo(1000);
        assertThat(checkin.readTimeout).isEqualTo(5000);
        assertThat(avaliacao.connectTimeout).isEqualTo(2000);
        assertThat(avaliacao.readTimeout).isEqualTo(23000);
    }

    // ── contrato de resposta ────────────────────────────────────────────

    @Test
    void saudeMental_leMensagemEAcaoSugeridaDoExemploDoContrato() {
        MentalHealthDto.Response resposta = new N8NMentalHealthClient(url(), 1000, 2000, 1, "")
                .process(checkin("qualquer"));

        assertThat(resposta.mensagem()).startsWith("Semanas assim cansam mesmo");
        assertThat(resposta.acaoSugerida()).contains("respiração 4-4-6");
    }

    @Test
    void assessment_leOsCincoCamposDoExemploDoContrato() {
        corpoDaResposta = ContratosN8n.texto("assessment.response");

        AssessmentDto.Response resposta = new N8NAssessmentClient(url(), 1000, 2000, 1, "")
                .process(ContratosN8n.ler("assessment.request", AssessmentDto.Request.class));

        assertThat(resposta.compatibilidade()).isEqualTo(72);
        assertThat(resposta.planoDesenvolvimento()).hasSize(2);
    }

    @Test
    void respostaEmFormaDeLista_aindaNaoEhLidaComoObjeto_oQueOWorkflowAntigoFazia() {
        // Documenta o defeito corrigido no "Responder": uma lista não vira o objeto esperado.
        corpoDaResposta = "[" + ContratosN8n.texto("mental-health.response") + "]";
        N8NMentalHealthClient cliente = new N8NMentalHealthClient(url(), 1000, 2000, 1, "");

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> cliente.process(checkin("qualquer")));
    }

    // ── URL vazia: o n8n é pulado ───────────────────────────────────────

    @Test
    void urlVazia_naoEstaConfiguradoENaoFazNenhumaChamada() {
        N8NMentalHealthClient cliente = new N8NMentalHealthClient("", 1000, 2000, 3, "");

        assertThat(cliente.isConfigured()).isFalse();
        org.junit.jupiter.api.Assertions.assertThrows(com.bitsystem.bitapp.exception.BusinessException.class,
                () -> cliente.process(checkin("qualquer")));
        assertThat(chamadas.get()).isZero();
        assertThat(new N8NAssessmentClient("   ", 1000, 2000, 3, "").isConfigured()).isFalse();
        assertThat(new N8NMentalHealthClient(url(), 1000, 2000, 1, "").isConfigured()).isTrue();
    }

    // ── autenticação por cabeçalho ──────────────────────────────────────

    @Test
    void semToken_naoEnviaOCabecalho() {
        new N8NMentalHealthClient(url(), 1000, 2000, 1, "").process(checkin("qualquer"));

        assertThat(tokenRecebido.get()).isNull();
    }

    @Test
    void tokenEmBranco_naoEnviaOCabecalho() {
        new N8NMentalHealthClient(url(), 1000, 2000, 1, "   ").process(checkin("qualquer"));

        assertThat(tokenRecebido.get()).isNull();
    }

    @Test
    void comToken_enviaXWebhookToken_nosDoisClientes() {
        new N8NMentalHealthClient(url(), 1000, 2000, 1, "token-de-teste-123").process(checkin("qualquer"));
        assertThat(tokenRecebido.get()).isEqualTo("token-de-teste-123");

        tokenRecebido.set(null);
        corpoDaResposta = ContratosN8n.texto("assessment.response");
        new N8NAssessmentClient(url(), 1000, 2000, 1, "token-de-teste-123")
                .process(ContratosN8n.ler("assessment.request", AssessmentDto.Request.class));
        assertThat(tokenRecebido.get()).isEqualTo("token-de-teste-123");
    }

    @Test
    void oTokenNuncaApareceNoLog() {
        new N8NMentalHealthClient(url(), 1000, 2000, 1, "token-de-teste-123").process(checkin("qualquer"));

        assertThat(logsEmTexto()).doesNotContain("token-de-teste-123");
    }

    // ── privacidade do log ──────────────────────────────────────────────

    @Test
    void log_naoContemOTextoLivreDoCheckin_soMetadados() {
        String relato = "RELATO-SECRETO-DO-USUARIO nao posso aparecer em log";

        new N8NMentalHealthClient(url(), 1000, 2000, 1, "").process(checkin(relato));

        String tudo = logsEmTexto();
        assertThat(tudo).doesNotContain("RELATO-SECRETO").doesNotContain("userId");
        assertThat(tudo).contains("tipo=mental-health").contains("tentativa 1/1").contains("bytes");
    }

    // ── log de falha numa linha ─────────────────────────────────────────

    private void n8nResponde(int status, String corpo) {
        servidor.removeContext("/webhook/bit/agent");
        servidor.createContext("/webhook/bit/agent", troca -> {
            chamadas.incrementAndGet();
            byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
            troca.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            troca.sendResponseHeaders(status, bytes.length);
            troca.getResponseBody().write(bytes);
            troca.close();
        });
    }

    private String avisoDeFalha() {
        List<String> avisos = logs.list.stream()
                .filter(e -> e.getLevel() == Level.WARN)
                .map(ILoggingEvent::getFormattedMessage)
                .toList();
        assertThat(avisos).as("avisos de falha no log").hasSize(1);
        return avisos.get(0);
    }

    @Test
    void falhaComPaginaDeErroGrande_logaUmaLinhaCurta_semDespejarOHtml() {
        // Proxy ou n8n respondendo com a página 404 inteira (~5 KB, várias linhas).
        String pagina = "<html>\n<head><title>404 Not Found</title></head>\n<body>\n"
                + "<p>Esta página de erro não existe e ocupa muitas linhas.</p>\n".repeat(90)
                + "FIM-DA-PAGINA\n</body>\n</html>\n";
        assertThat(pagina.length()).isGreaterThan(5000);
        n8nResponde(404, pagina);

        org.junit.jupiter.api.Assertions.assertThrows(com.bitsystem.bitapp.exception.BusinessException.class,
                () -> new N8NMentalHealthClient(url(), 1000, 2000, 1, "").process(checkin("qualquer")));

        String aviso = avisoDeFalha();
        assertThat(aviso).contains("404").contains("Not Found").contains("…");
        // "<EOL>" é como o Spring já trocava as quebras na mensagem antiga: sem ele, a linha não é a resumida.
        assertThat(aviso).doesNotContain("\n").doesNotContain("\r").doesNotContain("<EOL>").doesNotContain("FIM-DA-PAGINA");
        assertThat(aviso.length()).as("parte da mensagem do aviso").isLessThanOrEqualTo(300);
    }

    @Test
    void corpoDeErro_ficaInteiroAte200Caracteres_eAcimaDissoViraUmaLinhaDe200ComReticencias() {
        String limite = "x".repeat(200);
        assertThat(avisoDaFalhaComCorpo(500, limite)).endsWith("500 Internal Server Error: \"" + limite + "\"");

        assertThat(avisoDaFalhaComCorpo(500, limite + "y"))
                .endsWith("500 Internal Server Error: \"" + "x".repeat(199) + "…\"");
    }

    @Test
    void corpoDeErro_cortadoNoMeioDeUmParSubstituto_naoDeixaMetadeDeUmEmoji() {
        // O 😀 ocupa as posições 198 e 199; cortar em 199 deixaria só a primeira metade.
        String corpo = "a".repeat(198) + "😀" + "b".repeat(50);

        String aviso = avisoDaFalhaComCorpo(500, corpo);

        assertThat(aviso).endsWith("500 Internal Server Error: \"" + "a".repeat(198) + "…\"").doesNotContain("😀");
    }

    private String avisoDaFalhaComCorpo(int status, String corpo) {
        logs.list.clear();
        n8nResponde(status, corpo);
        org.junit.jupiter.api.Assertions.assertThrows(com.bitsystem.bitapp.exception.BusinessException.class,
                () -> new N8NMentalHealthClient(url(), 1000, 2000, 1, "").process(checkin("qualquer")));
        return avisoDeFalha();
    }

    @Test
    void falhaComMensagemCurta_continuaAparecendoInteira_paraDiagnosticarOToken() {
        n8nResponde(403, "Authorization data is wrong!");

        org.junit.jupiter.api.Assertions.assertThrows(com.bitsystem.bitapp.exception.BusinessException.class,
                () -> new N8NMentalHealthClient(url(), 1000, 2000, 1, "token-errado").process(checkin("qualquer")));

        String aviso = avisoDeFalha();
        assertThat(aviso).contains("403 Forbidden: \"Authorization data is wrong!\"");
        assertThat(aviso).doesNotContain("…").doesNotContain("token-errado");
    }

    @Test
    void falhaDeConexao_logaONomeDaClasseEAMensagemEmUmaLinha() throws IOException {
        int portaFechada;
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
            portaFechada = socket.getLocalPort();
        }
        String urlSemServidor = "http://127.0.0.1:" + portaFechada + "/webhook/bit/agent";

        org.junit.jupiter.api.Assertions.assertThrows(com.bitsystem.bitapp.exception.BusinessException.class,
                () -> new N8NMentalHealthClient(urlSemServidor, 1000, 2000, 1, "").process(checkin("qualquer")));

        String aviso = avisoDeFalha();
        assertThat(aviso).contains("ResourceAccessException: ").doesNotContain("\n").doesNotContain("\r");
        assertThat(aviso.length()).isLessThanOrEqualTo(300);
    }

    @Test
    void umaTentativaSo_quandoMaxRetriesEh1_eFalhaClaraQuandoOServidorErra() {
        servidor.removeContext("/webhook/bit/agent");
        servidor.createContext("/webhook/bit/agent", troca -> {
            chamadas.incrementAndGet();
            troca.sendResponseHeaders(500, -1);
            troca.close();
        });

        org.junit.jupiter.api.Assertions.assertThrows(com.bitsystem.bitapp.exception.BusinessException.class,
                () -> new N8NMentalHealthClient(url(), 1000, 2000, 1, "").process(List.of()));
        assertThat(chamadas.get()).isEqualTo(1);
    }
}
