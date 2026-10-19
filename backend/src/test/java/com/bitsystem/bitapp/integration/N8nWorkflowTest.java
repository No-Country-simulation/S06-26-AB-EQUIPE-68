package com.bitsystem.bitapp.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Valida o workflow versionado (n8n/bitapp-workflow-v2-producao.json) contra o
 * contrato que o backend espera. Não executa o n8n: lê o JSON exportado.
 */
class N8nWorkflowTest {

    private static JsonNode workflow;

    @BeforeAll
    static void lerWorkflow() throws IOException {
        // Surefire define "basedir" como a pasta backend/; o workflow fica em ../n8n.
        Path base = Path.of(System.getProperty("basedir", "."));
        Path arquivo = base.resolve("../n8n/bitapp-workflow-v2-producao.json").normalize();
        workflow = new ObjectMapper().readTree(Files.readString(arquivo));
    }

    static JsonNode no(String nome) {
        for (JsonNode n : workflow.path("nodes")) {
            if (nome.equals(n.path("name").asText())) {
                return n;
            }
        }
        throw new AssertionError("Nó não encontrado no workflow: " + nome);
    }

    @Test
    void responder_devolveSoOPrimeiroItemComoObjeto_naoUmaLista() {
        // O Java lê {mensagem, acaoSugerida} (e os cinco campos do assessment) como objeto;
        // "allIncomingItems" devolveria uma lista e o cliente descartaria a resposta.
        assertThat(no("Responder").path("parameters").path("respondWith").asText()).isEqualTo("firstIncomingItem");
    }

    @Test
    void webhookEntrada_exigeAutenticacaoPorCabecalho_semSegredoNoArquivo() {
        JsonNode webhook = no("Webhook Entrada");

        assertThat(webhook.path("parameters").path("authentication").asText()).isEqualTo("headerAuth");
        // Só a referência à credencial: o valor do token é criado no n8n e vive em N8N_WEBHOOK_TOKEN.
        assertThat(webhook.path("credentials").path("httpHeaderAuth").path("name").asText()).isEqualTo("BiT Webhook Token");
        assertThat(webhook.path("credentials").path("httpHeaderAuth").has("value")).isFalse();
        assertThat(workflow.toString()).doesNotContain("X-Webhook-Token");
    }

    @Test
    void urlsDoAppVemDeUmaVariavelDeAmbienteDoN8n_naoDeUmHostFixo() {
        for (String nome : new String[] {"Buscar Catálogo", "Buscar Roadmap"}) {
            String url = no(nome).path("parameters").path("url").asText();
            assertThat(url).as(nome).startsWith("=").contains("$env.BITAPP_BASE_URL");
        }
        assertThat(no("Buscar Catálogo").path("parameters").path("url").asText()).endsWith("/api/cursos");
        assertThat(no("Buscar Roadmap").path("parameters").path("url").asText()).contains("/api/roadmaps/");
        assertThat(workflow.toString()).doesNotContain("onrender.com");
    }

    @Test
    void todasAsConexoesApontamParaNosQueExistem() {
        Set<String> nomes = new HashSet<>();
        workflow.path("nodes").forEach(n -> nomes.add(n.path("name").asText()));

        workflow.path("connections").fields().forEachRemaining(origem -> {
            assertThat(nomes).as("origem da conexão").contains(origem.getKey());
            origem.getValue().path("main").forEach(saida ->
                saida.forEach(destino ->
                    assertThat(nomes).as("destino de %s", origem.getKey()).contains(destino.path("node").asText())));
        });
    }

    @Test
    void nomesDeNosSaoUnicos() {
        Set<String> vistos = new HashSet<>();
        workflow.path("nodes").forEach(n ->
            assertThat(vistos.add(n.path("name").asText())).as("nome duplicado: " + n.path("name").asText()).isTrue());
    }

    @Test
    void promptDeAcolhimento_mandaFalarDeHojeSemAfirmarComoFoiASemana() {
        String codigo = no("Montar Prompt Acolhimento").path("parameters").path("jsCode").asText();

        assertThat(codigo).contains("está HOJE").contains("NÃO afirme nem descreva como foi a semana dela");
    }
}
