package com.bitsystem.bitapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/rota")
public class RotaController {

    private static final Logger log = LoggerFactory.getLogger(RotaController.class);

    private final String orsApiKey;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String ORS_URL =
        "https://api.openrouteservice.org/v2/directions/driving-car/geojson";

    public RotaController(@Value("${ors.api.key:NAO_CONFIGURADA}") String orsApiKey) {
        this.orsApiKey = orsApiKey;
    }

    @PostMapping
    public ResponseEntity<?> calcularRota(@RequestBody Map<String, double[]> body) {
        double[] origem = body.get("origem");   // [lng, lat]
        double[] destino = body.get("destino"); // [lng, lat]

        if (origem == null || destino == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "Informe origem e destino."));
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", orsApiKey);

            Map<String, Object> payload = Map.of(
                "coordinates", new double[][]{ origem, destino }
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            log.info("Chamando ORS para rota de {} ate {}", origem, destino);
            ResponseEntity<String> resp = restTemplate.postForEntity(ORS_URL, entity, String.class);

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(resp.getBody());

        } catch (Exception ex) {
            log.warn("Falha ao chamar ORS: {}", ex.getMessage());
            // Fallback: avisa o frontend que a rota real falhou
            return ResponseEntity.status(503)
                .body(Map.of("erro", "ROTA_INDISPONIVEL",
                             "mensagem", "Servico de rotas temporariamente indisponivel."));
        }
    }
}
