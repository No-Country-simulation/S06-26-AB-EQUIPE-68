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
}
