package com.bitsystem.bitapp.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/** Lê os exemplos de contrato com o n8n (src/test/resources/contracts/n8n/). */
public final class ContratosN8n {

    /** Mesmo ObjectMapper que o RestTemplate do N8NBaseClient usa para ler a resposta do n8n. */
    public static final ObjectMapper MAPPER_DO_CLIENTE = Jackson2ObjectMapperBuilder.json().build();

    private ContratosN8n() {
    }

    public static String texto(String nome) {
        try (InputStream in = ContratosN8n.class.getResourceAsStream("/contracts/n8n/" + nome + ".json")) {
            if (in == null) {
                throw new IllegalStateException("Exemplo de contrato não encontrado: " + nome);
            }
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static JsonNode json(String nome) {
        try {
            return MAPPER_DO_CLIENTE.readTree(texto(nome));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> T ler(String nome, Class<T> tipo) {
        try {
            return MAPPER_DO_CLIENTE.readValue(texto(nome), tipo);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static List<String> campos(JsonNode objeto) {
        List<String> nomes = new ArrayList<>();
        objeto.fieldNames().forEachRemaining(nomes::add);
        return nomes;
    }
}
