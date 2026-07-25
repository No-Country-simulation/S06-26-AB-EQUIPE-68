package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 * Roadmaps de carreira curados pelo Tiago (base_conhecimento.json),
 * carregados uma única vez no boot (bean singleton) e servidos da memória.
 * Conteúdo curado é obrigatório: falha na carga aborta o boot (fail-fast)
 * em vez de degradar silenciosamente.
 */
@Service
public class RoadmapService {

    private static final Logger log = LoggerFactory.getLogger(RoadmapService.class);

    // Dropdown "Área Alvo" (perfil.html/cadastro.html) -> chave em base_conhecimento.json
    private static final Map<String, String> AREA_PARA_CHAVE = Map.ofEntries(
            Map.entry("java", "java_backend"),
            Map.entry("web", "web_development"),
            Map.entry("dados", "data_analysis_bi"),
            Map.entry("mobile", "mobile"),
            Map.entry("devops", "devops_cloud"),
            Map.entry("ciberseguranca", "ciberseguranca"),
            Map.entry("ia", "inteligencia_artificial"),
            Map.entry("games", "game_development"),
            Map.entry("uiux", "ui_ux_design"),
            Map.entry("infraestrutura", "infraestrutura_redes")
    );

    private final Map<String, JsonNode> roadmapsPorChave = new HashMap<>();

    public RoadmapService(ObjectMapper objectMapper) {
        try {
            ClassPathResource resource = new ClassPathResource("base_conhecimento.json");
            try (InputStream is = resource.getInputStream()) {
                JsonNode raiz = objectMapper.readTree(is);
                JsonNode roadmaps = raiz.get("roadmaps");
                roadmaps.fields().forEachRemaining(entry ->
                        roadmapsPorChave.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue()));
            }
            log.info("[RoadmapService] {} roadmaps carregados de base_conhecimento.json", roadmapsPorChave.size());
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Falha ao carregar base_conhecimento.json — conteúdo curado obrigatório no boot", ex);
        }
    }

    /**
     * Busca o roadmap pela área do dropdown (ex.: "Java", "UIUX") ou,
     * defensivamente, pela chave crua do JSON (ex.: "java_backend") —
     * ambos case-insensitive.
     */
    public JsonNode buscarPorArea(String area) {
        String normalizado = area == null ? "" : area.trim().toLowerCase(Locale.ROOT);
        String chave = AREA_PARA_CHAVE.getOrDefault(normalizado, normalizado);
        JsonNode roadmap = roadmapsPorChave.get(chave);
        if (roadmap == null) {
            throw new BusinessException("ROADMAP_NAO_ENCONTRADO",
                    "Nenhum roadmap disponível para a área: " + area);
        }
        return roadmap;
    }
}
