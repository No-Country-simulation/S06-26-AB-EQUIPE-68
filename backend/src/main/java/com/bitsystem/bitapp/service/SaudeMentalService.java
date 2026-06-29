package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.dto.SaudeDto;
import com.bitsystem.bitapp.integration.GeminiClient;
import com.bitsystem.bitapp.model.HistoricoSaude;
import com.bitsystem.bitapp.repository.HistoricoSaudeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SaudeMentalService {

    private static final Logger log = LoggerFactory.getLogger(SaudeMentalService.class);

    private final HistoricoSaudeRepository saudeRepository;
    private final EmotionResponseProvider emotionResponseProvider;
    private final FallbackStorage fallbackStorage;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public SaudeMentalService(
            HistoricoSaudeRepository saudeRepository,
            EmotionResponseProvider emotionResponseProvider,
            FallbackStorage fallbackStorage,
            GeminiClient geminiClient,
            ObjectMapper objectMapper) {
        this.saudeRepository = saudeRepository;
        this.emotionResponseProvider = emotionResponseProvider;
        this.fallbackStorage = fallbackStorage;
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    public SaudeDto.Response avaliarEstadoMental(SaudeDto.Request request) {
        boolean derivarCvv = request.notaSemanal() < 4;

        SaudeDto.RawResponse rawResponse;

        if (geminiClient.isConfigured()) {
            try {
                rawResponse = chamarGemini(request);
                log.info("[SaudeMentalService] Resposta gerada via Gemini para usuarioId={}", request.usuarioId());
            } catch (Exception ex) {
                log.warn("[SaudeMentalService] Gemini indisponivel, usando fallback local: {}", ex.getMessage());
                rawResponse = emotionResponseProvider.resolve(request.humor(), request.notaSemanal());
            }
        } else {
            rawResponse = emotionResponseProvider.resolve(request.humor(), request.notaSemanal());
        }

        String alerta = derivarCvv
                ? "ALERTA_CRITICO: Indicadores de sofrimento severo ou humor debilitado. Direcionando canais de apoio imediato."
                : "ESTAVEL";

        SaudeDto.Response response = new SaudeDto.Response(
                rawResponse.mensagem(),
                rawResponse.acaoSugerida(),
                derivarCvv,
                request.notaSemanal(),
                alerta);

        try {
            HistoricoSaude historico = HistoricoSaude.builder()
                    .userId(request.usuarioId())
                    .humor(request.humor())
                    .notaSemanal(request.notaSemanal())
                    .contexto(request.contexto())
                    .derivouCvv(derivarCvv)
                    .build();
            saudeRepository.save(historico);

            log.info("[SaudeMentalService] Historico salvo no banco: usuarioId={}", request.usuarioId());

        } catch (Exception ex) {
            log.warn("[SaudeMentalService] Banco indisponivel, salvando em memoria: {}", ex.getMessage());
            fallbackStorage.saveSaudeRecord(
                request.usuarioId(), request.humor(), request.notaSemanal(),
                request.contexto(), derivarCvv
            );
        }

        return response;
    }

    private SaudeDto.RawResponse chamarGemini(SaudeDto.Request request) throws Exception {
        String prompt = buildPrompt(request);
        String resposta = geminiClient.generateContent(prompt);
        return parsearResposta(resposta);
    }

    private String buildPrompt(SaudeDto.Request request) {
        return String.format("""
            Voce e um profissional de saude mental especializado em acolhimento de pessoas em transicao de carreira.
            Analise o check-in do usuario e retorne um JSON EXATAMENTE neste formato:

            {
              "mensagem": "sua mensagem empatica e acolhedora",
              "acaoSugerida": "acao pratica e imediata que o usuario pode tomar agora"
            }

            Check-in do usuario:
            - Humor: %s
            - Nota semanal (1-5): %d
            - Contexto: %s

            Regras:
            - Se a nota for menor que 4, seja mais acolhedor e direcione para o CVV (188)
            - Se a nota for 4 ou 5, seja encorajador e motivador
            - Retorne APENAS o JSON, sem texto adicional
            """,
            request.humor(),
            request.notaSemanal(),
            request.contexto() != null && !request.contexto().isBlank() ? request.contexto() : "Nenhum contexto fornecido"
        );
    }

    private SaudeDto.RawResponse parsearResposta(String resposta) {
        try {
            String jsonStr = resposta.trim();
            if (jsonStr.contains("```")) {
                jsonStr = jsonStr.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
            }

            int start = jsonStr.indexOf('{');
            int end = jsonStr.lastIndexOf('}');
            if (start >= 0 && end > start) {
                jsonStr = jsonStr.substring(start, end + 1);
            }

            JsonNode root = objectMapper.readTree(jsonStr);

            String mensagem = root.path("mensagem").asText("Resposta nao disponivel.");
            String acaoSugerida = root.path("acaoSugerida").asText("Tente novamente mais tarde.");

            return new SaudeDto.RawResponse(mensagem, acaoSugerida);

        } catch (Exception ex) {
            log.error("[SaudeMentalService] Erro ao parsear resposta Gemini: {}", ex.getMessage());
            throw new RuntimeException("Falha ao processar resposta da IA", ex);
        }
    }

    public List<SaudeDto.HistoricoResponse> buscarHistorico(Long usuarioId) {
        List<SaudeDto.HistoricoResponse> resultado = new ArrayList<>();

        try {
            List<HistoricoSaude> registros = saudeRepository.findByUserIdOrderByCreatedAtDesc(usuarioId);
            for (HistoricoSaude h : registros) {
                resultado.add(new SaudeDto.HistoricoResponse(
                        h.getId(),
                        h.getHumor(),
                        h.getNotaSemanal(),
                        h.getContexto(),
                        h.getDerivouCvv(),
                        h.getCreatedAt()
                ));
            }
        } catch (Exception ex) {
            log.warn("[SaudeMentalService] DB indisponivel, buscando historico em memoria: {}", ex.getMessage());
            List<FallbackStorage.SaudeRecord> fallbackRecords = fallbackStorage.findSaudeByUserId(usuarioId);
            for (FallbackStorage.SaudeRecord r : fallbackRecords) {
                resultado.add(new SaudeDto.HistoricoResponse(
                        r.id(),
                        r.humor(),
                        r.notaSemanal(),
                        r.contexto(),
                        r.derivouCvv(),
                        r.createdAt()
                ));
            }
        }

        return resultado;
    }
}
