package com.bitsystem.bitapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bitsystem.bitapp.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testes de RoadmapService — carga do base_conhecimento.json no boot e busca
 * por área (mapeamento do dropdown + case-insensitive), incluindo o caminho
 * defensivo de área sem roadmap (422 ROADMAP_NAO_ENCONTRADO).
 */
class RoadmapServiceTest {

    private RoadmapService service;

    @BeforeEach
    void setUp() {
        service = new RoadmapService(new ObjectMapper());
    }

    @Test
    void buscarPorArea_todasAsDezAreasDoDropdownTemRoadmap() {
        String[] areas = {"Java", "Web", "Dados", "Mobile", "DevOps",
                "Ciberseguranca", "IA", "Games", "UIUX", "Infraestrutura"};
        for (String area : areas) {
            JsonNode roadmap = service.buscarPorArea(area);
            assertThat(roadmap.get("titulo").asText()).isNotBlank();
        }
    }

    @Test
    void buscarPorArea_casoInsensitivo() {
        JsonNode roadmap = service.buscarPorArea("java");
        assertThat(roadmap.get("titulo").asText()).isEqualTo("Java Back-End");
    }

    @Test
    void buscarPorArea_comEspacosEmVolta_normaliza() {
        JsonNode roadmap = service.buscarPorArea("  Web  ");
        assertThat(roadmap.get("titulo").asText()).isEqualTo("Web Development");
    }

    @Test
    void buscarPorArea_chaveCruaDoJson_tambemFunciona() {
        JsonNode roadmap = service.buscarPorArea("java_backend");
        assertThat(roadmap.get("titulo").asText()).isEqualTo("Java Back-End");
    }

    @Test
    void buscarPorArea_areaInexistente_lancaBusinessException422() {
        assertThatThrownBy(() -> service.buscarPorArea("Blockchain"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo("ROADMAP_NAO_ENCONTRADO"));
    }
}
