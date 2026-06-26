package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.dto.MentalHealthDto;
import com.bitsystem.bitapp.exception.BusinessException;
import com.bitsystem.bitapp.integration.N8NMentalHealthClient;
import com.bitsystem.bitapp.domain.MentalHealthRecord;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.bitsystem.bitapp.repository.MentalHealthRecordRepository;

@Service
public class MentalHealthService {

    private static final Logger log = LoggerFactory.getLogger(MentalHealthService.class);

    private final N8NMentalHealthClient n8nClient;
    private final MentalHealthRecordRepository repository;
    private final FallbackStorage fallbackStorage;

    public MentalHealthService(N8NMentalHealthClient n8nClient, MentalHealthRecordRepository repository,
            FallbackStorage fallbackStorage) {
        this.n8nClient = n8nClient;
        this.repository = repository;
        this.fallbackStorage = fallbackStorage;
    }

    public MentalHealthDto.Response processar(Long usuarioId) {
        // ── Tentar N8N primeiro ────────────────────────────────────────────
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", usuarioId);
            payload.put("message", "");
            payload.put("profile", Collections.emptyMap());
            payload.put("history", Collections.emptyList());
            MentalHealthDto.Response response = n8nClient.process(payload);

            // Salvar no banco
            try {
                MentalHealthRecord record = MentalHealthRecord.builder()
                        .usuarioId(usuarioId)
                        .nivel(response.nivel())
                        .alerta(response.alerta())
                        .derivarCvv(response.derivarCvv())
                        .scoreRisco(response.scoreRisco())
                        .build();
                repository.save(record);
            } catch (Exception dbEx) {
                log.warn("[MentalHealthService] Banco indisponível, salvando registro em memória");
                fallbackStorage.saveMentalHealthRecord(usuarioId, response);
            }

            log.info("[MentalHealthService] Saúde mental processada via N8N: usuarioId={}", usuarioId);
            return response;

        } catch (Exception ex) {
            // ── Fallback: resposta local ───────────────────────────────────
            log.warn("[MentalHealthService] N8N indisponível, usando resposta local: {}", ex.getMessage());
            MentalHealthDto.Response fallbackResponse = buildFallbackMentalHealth(usuarioId);

            try {
                fallbackStorage.saveMentalHealthRecord(usuarioId, fallbackResponse);
            } catch (Exception storageEx) {
                log.warn("[MentalHealthService] Falha ao salvar fallback: {}", storageEx.getMessage());
            }

            return fallbackResponse;
        }
    }

    /**
     * Resposta local de saúde mental quando N8N está indisponível.
     */
    private MentalHealthDto.Response buildFallbackMentalHealth(Long usuarioId) {
        return new MentalHealthDto.Response(
            "estavel",
            "Servio de avaliação indisponível no momento. Retornando orientação padrão.",
            List.of(
                "Mantenha uma rotina de estudos equilibrada",
                "Faça pausas regulares a cada 50 minutos de estudo",
                "Pratique atividades físicas leves para reduzir o estresse",
                "Converse com amigos ou familiares sobre seus objetivos"
            ),
            List.of(
                "Reserve 10 minutos ao dia para mindfulness ou respiração",
                "Liste 3 conquistas da semana, por menores que sejam",
                "Reduza o tempo de tela antes de dormir"
            ),
            List.of(
                "CVV - Disque 188 (24h, gratuito e sigiloso)",
                "CAPS - Centro de Atenção Psicossocial do seu município",
                "CVV Online - www.cvv.org.br"
            ),
            false,
            2
        );
    }
}
