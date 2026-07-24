package com.bitsystem.bitapp.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.bitsystem.bitapp.dto.StandardApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Teste end-to-end de GET /api/roadmaps/{area} — mesmo estilo de
 * MentoriaControllerTest.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoadmapControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void buscarPorArea_areaValida_retornaRoadmap() {
        ResponseEntity<StandardApiResponse<JsonNode>> response = restTemplate.exchange(
                "/api/roadmaps/Java", HttpMethod.GET, null,
                new ParameterizedTypeReference<StandardApiResponse<JsonNode>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());
        assertEquals("Java Back-End", response.getBody().data().get("titulo").asText());
    }

    @Test
    void buscarPorArea_casoInsensitivo_retornaRoadmap() {
        ResponseEntity<StandardApiResponse<JsonNode>> response = restTemplate.exchange(
                "/api/roadmaps/uiux", HttpMethod.GET, null,
                new ParameterizedTypeReference<StandardApiResponse<JsonNode>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("UI/UX Design", response.getBody().data().get("titulo").asText());
    }

    @Test
    void buscarPorArea_areaInexistente_retorna422() {
        ResponseEntity<StandardApiResponse> response = restTemplate.exchange(
                "/api/roadmaps/Blockchain", HttpMethod.GET, null,
                StandardApiResponse.class);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertEquals("ROADMAP_NAO_ENCONTRADO", response.getBody().codigo());
    }
}
