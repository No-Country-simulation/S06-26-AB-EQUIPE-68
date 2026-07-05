package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.dto.MentalHealthDto;
import com.bitsystem.bitapp.dto.SaudeDto;
import com.bitsystem.bitapp.integration.GeminiClient;
import com.bitsystem.bitapp.integration.N8NMentalHealthClient;
import com.bitsystem.bitapp.model.HistoricoSaude;
import com.bitsystem.bitapp.model.NivelCheckin;
import com.bitsystem.bitapp.repository.HistoricoSaudeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SaudeMentalService {

    private static final Logger log = LoggerFactory.getLogger(SaudeMentalService.class);

    /** Nota-teto da derivação REFORÇADA (crise aguda: modal de consentimento). */
    private static final int NOTA_MAX_REFORCADO = 1;
    /** Nota-teto da derivação PREVENTIVA (escuta suave, grava direto). */
    private static final int NOTA_MAX_PREVENTIVO = 3;

    private final HistoricoSaudeRepository saudeRepository;
    private final EmotionResponseProvider emotionResponseProvider;
    private final FallbackStorage fallbackStorage;
    private final GeminiClient geminiClient;
    private final N8NMentalHealthClient mentalHealthClient;
    private final ObjectMapper objectMapper;

    public SaudeMentalService(
            HistoricoSaudeRepository saudeRepository,
            EmotionResponseProvider emotionResponseProvider,
            FallbackStorage fallbackStorage,
            GeminiClient geminiClient,
            N8NMentalHealthClient mentalHealthClient,
            ObjectMapper objectMapper) {
        this.saudeRepository = saudeRepository;
        this.emotionResponseProvider = emotionResponseProvider;
        this.fallbackStorage = fallbackStorage;
        this.geminiClient = geminiClient;
        this.mentalHealthClient = mentalHealthClient;
        this.objectMapper = objectMapper;
    }

    public SaudeDto.Response avaliarEstadoMental(SaudeDto.Request request) {
        // ── DECISÃO DO CVV: 100% determinística, baseada SÓ na nota do check-in ──
        // A IA, o Mental Health Agent e o texto do usuário NUNCA entram aqui.
        String nivelDerivacao = nivelDerivacao(request.nota());
        boolean derivarCvv = nivelDerivacao != null;

        // ── MENSAGEM DE ACOLHIMENTO: agente do Tiago (n8n) com fallback gracioso ──
        SaudeDto.RawResponse rawResponse = obterAcolhimento(request);

        // ── LEITURA EMOCIONAL: texto descritivo (nunca decide/deriva CVV) ──
        // Sempre preenchida: usa a da IA/agente quando houver, senão um
        // fallback determinístico local — garante que todo caminho de
        // acolhimento (Gemini, agente n8n, curadas) responde com o campo.
        String leituraEmocional = (rawResponse.leituraEmocional() != null && !rawResponse.leituraEmocional().isBlank())
                ? rawResponse.leituraEmocional()
                : leituraEmocionalFallback(request);

        String alerta = switch (nivelDerivacao == null ? "" : nivelDerivacao) {
            case "REFORCADO" -> "DERIVACAO_REFORCADA";
            case "PREVENTIVO" -> "DERIVACAO_PREVENTIVA";
            default -> "ESTAVEL";
        };

        SaudeDto.Response response = new SaudeDto.Response(
                rawResponse.mensagem(),
                rawResponse.acaoSugerida(),
                derivarCvv,
                request.nota(),
                alerta,
                nivelDerivacao,
                leituraEmocional);

        try {
            HistoricoSaude historico = HistoricoSaude.builder()
                    .userId(request.usuarioId())
                    .nota(request.nota())
                    .contexto(request.contexto())
                    .derivouCvv(derivarCvv)
                    .build();
            saudeRepository.save(historico);

            log.info("[SaudeMentalService] Historico salvo no banco: usuarioId={}", request.usuarioId());

        } catch (Exception ex) {
            log.warn("[SaudeMentalService] Banco indisponivel, salvando em memoria: {}", ex.getMessage());
            fallbackStorage.saveSaudeRecord(
                request.usuarioId(), request.nota(),
                request.contexto(), derivarCvv
            );
        }

        return response;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  REGRA DO CVV — determinística e graduada, decidida SÓ pela nota semanal
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Nível de derivação ao CVV, decidido EXCLUSIVAMENTE pela nota do check-in
     * (escala única CVV v2: 9/7/5/3/1, ou null se check-in só-texto). A IA, o
     * Mental Health Agent e o texto do usuário NUNCA entram aqui. Retorna:
     *   - "REFORCADO"  → nota 1 (crise aguda: modal de consentimento + CVV em destaque);
     *   - "PREVENTIVO" → nota 3 (escuta suave, apresentada como recurso);
     *   - null         → nota 5/7/9 ou ausente (sem derivação, acolhimento normal).
     */
    private String nivelDerivacao(Integer nota) {
        if (nota == null) {
            return null;
        }
        if (nota <= NOTA_MAX_REFORCADO) {
            log.info("[SaudeMentalService] CVV: derivação REFORCADA (nota={})", nota);
            return "REFORCADO";
        }
        if (nota <= NOTA_MAX_PREVENTIVO) {
            log.info("[SaudeMentalService] CVV: derivação PREVENTIVA (nota={})", nota);
            return "PREVENTIVO";
        }
        return null;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LEITURA EMOCIONAL (lote 4.1) — só texto, nunca influencia derivarCvv
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Fallback determinístico de leitura emocional, usado quando a fonte de
     * acolhimento (agente n8n ou curadas) não forneceu uma. Nunca cita a nota.
     */
    private String leituraEmocionalFallback(SaudeDto.Request request) {
        boolean es = "es".equals(request.idiomaOuPadrao());
        if (request.nota() != null) {
            String rotulo = NivelCheckin.fromNota(request.nota()).getRotulo();
            return es
                    ? "Percibí que te estás sintiendo " + rotulo.toLowerCase() + "."
                    : "Percebi que você está se sentindo " + rotulo.toLowerCase() + ".";
        }
        boolean temContexto = request.contexto() != null && !request.contexto().isBlank();
        if (temContexto) {
            return es ? "Gracias por compartir cómo te sientes." : "Obrigado por compartilhar como você está se sentindo.";
        }
        return es ? "Estoy aquí para escucharte." : "Estou aqui para te ouvir.";
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MENSAGEM DE ACOLHIMENTO (Tarefa 4) — agente do Tiago com fallback
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Fonte da mensagem acolhedora, na ordem:
     *  1) Mental Health Agent (n8n) — quando disponível;
     *  2) Fallback: lógica atual (Gemini, se configurado; senão respostas curadas).
     *
     * IMPORTANTE: o agente só fornece TEXTO. Ele nunca decide/aciona/cancela o CVV.
     */
    private SaudeDto.RawResponse obterAcolhimento(SaudeDto.Request request) {
        try {
            MentalHealthDto.Response agente = mentalHealthClient.process(montarPayloadAgente(request));
            SaudeDto.RawResponse mapeada = mapearAgente(agente);
            if (mapeada != null) {
                log.info("[SaudeMentalService] Acolhimento via Mental Health Agent (n8n) usuarioId={}", request.usuarioId());
                return mapeada;
            }
            log.warn("[SaudeMentalService] Agente retornou vazio, usando fallback local");
        } catch (Exception ex) {
            log.warn("[SaudeMentalService] Mental Health Agent indisponivel, usando fallback local: {}", ex.getMessage());
        }
        return acolhimentoFallback(request);
    }

    private Map<String, Object> montarPayloadAgente(SaudeDto.Request request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", request.usuarioId());
        payload.put("nota", request.nota());
        payload.put("message", request.contexto() != null ? request.contexto() : "");
        payload.put("tipo", "mental-health");
        return payload;
    }

    /**
     * Mapeia {nivel, alerta, recomendacoes, acoes, canaisApoio} do agente para
     * a mensagem/ação que a tela renderiza. Campos ausentes são tolerados;
     * derivarCvv/scoreRisco do agente são deliberadamente IGNORADOS.
     */
    private SaudeDto.RawResponse mapearAgente(MentalHealthDto.Response agente) {
        if (agente == null) {
            return null;
        }
        String mensagem = null;
        if (agente.recomendacoes() != null && !agente.recomendacoes().isEmpty()) {
            mensagem = String.join(" ", agente.recomendacoes());
        } else if (agente.alerta() != null && !agente.alerta().isBlank()) {
            mensagem = agente.alerta();
        }
        if (mensagem == null || mensagem.isBlank()) {
            return null; // sem texto útil → deixa o fallback assumir
        }

        String acao = null;
        if (agente.acoes() != null && !agente.acoes().isEmpty()) {
            acao = agente.acoes().get(0);
        } else if (agente.canaisApoio() != null && !agente.canaisApoio().isEmpty()) {
            acao = agente.canaisApoio().get(0);
        }
        if (acao == null || acao.isBlank()) {
            acao = "Reserve um momento de cuidado com você hoje.";
        }
        return new SaudeDto.RawResponse(mensagem, acao);
    }

    /** Fallback de acolhimento: mantém a lógica anterior (Gemini → respostas curadas). */
    private SaudeDto.RawResponse acolhimentoFallback(SaudeDto.Request request) {
        if (geminiClient.isConfigured()) {
            try {
                SaudeDto.RawResponse viaGemini = chamarGemini(request);
                log.info("[SaudeMentalService] Acolhimento via Gemini (fallback) usuarioId={}", request.usuarioId());
                return viaGemini;
            } catch (Exception ex) {
                log.warn("[SaudeMentalService] Gemini indisponivel, usando respostas curadas: {}", ex.getMessage());
            }
        }
        return emotionResponseProvider.resolve(request.nota(), request.idiomaOuPadrao());
    }

    private SaudeDto.RawResponse chamarGemini(SaudeDto.Request request) throws Exception {
        String prompt = buildPrompt(request);
        String resposta = geminiClient.generateContent(prompt);
        return parsearResposta(resposta);
    }

    private String buildPrompt(SaudeDto.Request request) {
        String idioma = "es".equals(request.idiomaOuPadrao()) ? "espanhol" : "portugues";
        String rotuloTexto = request.nota() != null
                ? NivelCheckin.fromNota(request.nota()).getRotulo()
                : "não informado (check-in só com texto)";
        return String.format("""
            Voce e um profissional de saude mental especializado em acolhimento de pessoas em transicao de carreira.
            Analise o check-in do usuario e retorne um JSON EXATAMENTE neste formato:

            {
              "mensagem": "sua mensagem empatica e acolhedora",
              "acaoSugerida": "acao pratica e imediata que o usuario pode tomar agora",
              "leituraEmocional": "uma frase curta e empatica descrevendo o estado emocional que voce percebeu no rotulo e no texto"
            }

            Check-in do usuario:
            - Rótulo do check-in (não decide nada sozinho; pode estar ausente se o usuario so escreveu texto): %s
            - Contexto: %s

            Regras:
            - Seja sempre empatico e acolhedor, validando os sentimentos da pessoa
            - Se o rótulo indicar um momento difícil, ofereca o CVV (188) como recurso disponivel, de forma acolhedora e nunca como bloqueio
            - Se o rótulo indicar um bom momento, seja encorajador e motivador
            - NUNCA cite ou repita a nota numerica em nenhum dos campos, inclusive leituraEmocional
            - Responda em %s (idioma escolhido pelo usuario na interface)
            - Retorne APENAS o JSON, sem texto adicional
            """,
            rotuloTexto,
            request.contexto() != null && !request.contexto().isBlank() ? request.contexto() : "Nenhum contexto fornecido",
            idioma
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
            String leituraEmocional = root.path("leituraEmocional").asText(null);

            return new SaudeDto.RawResponse(mensagem, acaoSugerida, leituraEmocional);

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
                        h.getNota(),
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
                        r.nota(),
                        r.contexto(),
                        r.derivouCvv(),
                        r.createdAt()
                ));
            }
        }

        return resultado;
    }
}