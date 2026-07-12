package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.domain.User;
import com.bitsystem.bitapp.dto.DicaLazerDto;
import com.bitsystem.bitapp.dto.NetworkStatusDto;
import com.bitsystem.bitapp.dto.PontoLazerDto;
import com.bitsystem.bitapp.dto.SaudeDto;
import com.bitsystem.bitapp.dto.SugestaoDto;
import com.bitsystem.bitapp.exception.BusinessException;
import com.bitsystem.bitapp.integration.GeminiClient;
import com.bitsystem.bitapp.model.NivelCheckin;
import com.bitsystem.bitapp.repository.UserRepository;
import com.bitsystem.bitapp.seed.DicasLazerSeed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * ============================================================================
 * SERVIÇO: SugestaoService
 * ============================================================================
 *
 * Sugestões de lazer/bem-estar personalizadas por humor + região +
 * conectividade, com IA (Gemini) e fallback determinístico obrigatório.
 * Consumido tanto pela seção "Dicas de Lazer" de saude-mental.html quanto
 * pela resposta do agente Bit após o check-in.
 *
 * @author BiT System
 * @version 1.0.0
 */
@Service
public class SugestaoService {

    private static final Logger log = LoggerFactory.getLogger(SugestaoService.class);
    private static final int QTD_SUGESTOES = 3;

    /** Categorias "ativas" (nota 7/9 — Feliz/Muito feliz). */
    private static final List<String> CATEGORIAS_ATIVAS = List.of("social", "cultura");
    /** Categorias "calmas/acolhedoras" (nota 1/3 — Muito triste/Triste). */
    private static final List<String> CATEGORIAS_CALMAS = List.of("calma", "pausa");
    /** Categorias padrão/neutras — nota 5 (Tranquilo), nota ausente (check-in só-texto) ou nota inválida. */
    private static final List<String> CATEGORIAS_PADRAO = List.of("natureza", "social", "cultura");

    private final SaudeMentalService saudeMentalService;
    private final UserRepository userRepository;
    private final GeolocationService geolocationService;
    private final LazerService lazerService;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public SugestaoService(
            SaudeMentalService saudeMentalService,
            UserRepository userRepository,
            GeolocationService geolocationService,
            LazerService lazerService,
            GeminiClient geminiClient,
            ObjectMapper objectMapper) {
        this.saudeMentalService = saudeMentalService;
        this.userRepository = userRepository;
        this.geolocationService = geolocationService;
        this.lazerService = lazerService;
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    public SugestaoDto.Response gerarSugestoes(Long usuarioId, String idioma) {
        User user = userRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado: " + usuarioId));

        Integer nota = ultimaNota(usuarioId);
        String contexto = ultimoContexto(usuarioId);
        String regiao = user.getCidade();
        List<DicaLazerDto> candidatas = DicasLazerSeed.DICAS_LAZER.getOrDefault(regiao, DicasLazerSeed.DICAS_GERAIS);

        NetworkStatusDto networkStatus = geolocationService.getNetworkStatus(usuarioId, 5000);
        boolean sugerirOffline = networkStatus.isSugerirOffline();
        String zonaPredominante = zonaPredominante(regiao);

        if (geminiClient.isConfigured()) {
            try {
                SugestaoDto.Response viaGemini = chamarGemini(nota, contexto, regiao, candidatas, sugerirOffline, zonaPredominante, idioma);
                if (viaGemini != null && !viaGemini.sugestoes().isEmpty()) {
                    log.info("[SugestaoService] Sugestões via Gemini para usuarioId={}", usuarioId);
                    return viaGemini;
                }
            } catch (Exception ex) {
                log.warn("[SugestaoService] Gemini indisponivel, usando fallback determinístico: {}", ex.getMessage());
            }
        }

        return fallbackDeterministico(nota, candidatas, sugerirOffline);
    }

    private Integer ultimaNota(Long usuarioId) {
        List<SaudeDto.HistoricoResponse> historico = saudeMentalService.buscarHistorico(usuarioId);
        return historico.stream()
            .findFirst()
            .map(SaudeDto.HistoricoResponse::nota)
            .orElse(null);
    }

    /** Texto livre do último check-in (lote 4.1) — aditivo, contexto extra
     *  para a IA; o fallback determinístico continua ignorando este campo. */
    private String ultimoContexto(Long usuarioId) {
        List<SaudeDto.HistoricoResponse> historico = saudeMentalService.buscarHistorico(usuarioId);
        return historico.stream()
            .findFirst()
            .map(SaudeDto.HistoricoResponse::contexto)
            .filter(c -> c != null && !c.isBlank())
            .orElse(null);
    }

    /** Zona predominante (moda) dos pontos de Lazer da região, se houver. */
    private String zonaPredominante(String regiao) {
        if (regiao == null) {
            return null;
        }
        Map<String, Long> contagem = lazerService.listarPontos().stream()
            .filter(p -> regiao.equals(p.regiao()))
            .collect(Collectors.groupingBy(PontoLazerDto.Response::zonaMovimento, Collectors.counting()));
        return contagem.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  FALLBACK DETERMINÍSTICO (sem IA)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Filtra as dicas candidatas pelas categorias da faixa de nota do último
     * check-in (1/3=calmas, 5=neutras, 7/9=ativas; nota ausente=neutras);
     * completa até QTD_SUGESTOES com o restante da lista regional e, se ainda
     * faltar, com DICAS_GERAIS. Quando sugerirOffline=true, prioriza as dicas
     * offlineFriendly=true (mantendo a ordem relativa das demais).
     */
    SugestaoDto.Response fallbackDeterministico(Integer nota, List<DicaLazerDto> candidatas, boolean sugerirOffline) {
        List<String> categorias = categoriasPorNota(nota);

        // LinkedHashMap por título: preserva ordem de inserção e remove duplicatas.
        Map<String, DicaLazerDto> selecionadas = new LinkedHashMap<>();

        candidatas.stream()
            .filter(d -> categorias.contains(d.categoria()))
            .forEach(d -> selecionadas.putIfAbsent(d.titulo(), d));

        if (selecionadas.size() < QTD_SUGESTOES) {
            candidatas.forEach(d -> selecionadas.putIfAbsent(d.titulo(), d));
        }
        if (selecionadas.size() < QTD_SUGESTOES) {
            DicasLazerSeed.DICAS_GERAIS.forEach(d -> selecionadas.putIfAbsent(d.titulo(), d));
        }

        List<DicaLazerDto> ordenadas = new ArrayList<>(selecionadas.values());
        if (sugerirOffline) {
            ordenadas.sort(Comparator.comparing(d -> !d.offlineFriendly()));
        }

        List<SugestaoDto.Item> itens = ordenadas.stream()
            .limit(QTD_SUGESTOES)
            .map(d -> new SugestaoDto.Item(d.titulo(), d.desc()))
            .toList();

        return new SugestaoDto.Response(itens);
    }

    /** Categorias por faixa de nota do check-in (nota ausente → padrão/neutra). */
    private static List<String> categoriasPorNota(Integer nota) {
        if (nota == null) {
            return CATEGORIAS_PADRAO;
        }
        if (nota <= 3) {
            return CATEGORIAS_CALMAS;
        }
        if (nota >= 7) {
            return CATEGORIAS_ATIVAS;
        }
        return CATEGORIAS_PADRAO;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  IA (Gemini)
    // ════════════════════════════════════════════════════════════════════════

    private SugestaoDto.Response chamarGemini(Integer nota, String contextoCheckin, String regiao, List<DicaLazerDto> candidatas,
            boolean sugerirOffline, String zonaPredominante, String idioma) throws Exception {
        String prompt = buildPrompt(nota, contextoCheckin, regiao, candidatas, sugerirOffline, zonaPredominante, idioma);
        String resposta = geminiClient.generateContent(prompt);
        return parsearResposta(resposta);
    }

    private String buildPrompt(Integer nota, String contextoCheckin, String regiao, List<DicaLazerDto> candidatas,
            boolean sugerirOffline, String zonaPredominante, String idioma) {
        String idiomaTexto = "es".equalsIgnoreCase(idioma) ? "espanhol" : "portugues";
        String lista = candidatas.stream()
            .map(d -> "- " + d.titulo() + ": " + d.desc())
            .collect(Collectors.joining("\n"));

        String rotulo = nota != null
            ? NivelCheckin.fromNota(nota).getRotulo()
            : "não informado";

        String contextoZona = zonaPredominante != null
            ? "Zona predominante da região: " + zonaPredominante + ". Se o estado emocional indicar sobrecarga, priorize sugestões compatíveis com ambientes tranquilos."
            : "";
        String contextoOffline = sugerirOffline
            ? "A conectividade do usuário está fraca — priorize sugestões que não dependem de internet."
            : "";
        // Lote 4.1: texto livre do último check-in, quando houver, para sugestões mais específicas.
        String contextoCheckinTexto = contextoCheckin != null
            ? "Último relato do usuário (se ajudar a entender o momento): " + contextoCheckin
            : "";

        return String.format("""
            Voce e um assistente de bem-estar que sugere atividades de lazer regionais.
            Escolha exatamente %d itens da LISTA CANDIDATA abaixo, os mais adequados ao
            estado emocional do usuario, e retorne um JSON EXATAMENTE neste formato:

            {
              "sugestoes": [
                {"titulo": "titulo exato de um item da lista", "motivo": "por que essa sugestao combina com o momento do usuario"}
              ]
            }

            Estado emocional atual do usuario: %s
            Região: %s
            %s
            %s
            %s

            LISTA CANDIDATA (só pode escolher títulos desta lista):
            %s

            Regras:
            - Só escolha títulos que existem literalmente na lista candidata
            - NUNCA cite ou mencione notas numéricas de bem-estar
            - Responda em %s (idioma escolhido pelo usuario na interface)
            - Retorne APENAS o JSON, sem texto adicional
            """,
            QTD_SUGESTOES, rotulo, regiao != null ? regiao : "não informada",
            contextoZona, contextoOffline, contextoCheckinTexto, lista, idiomaTexto
        );
    }

    private SugestaoDto.Response parsearResposta(String resposta) {
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
            List<SugestaoDto.Item> itens = new ArrayList<>();
            for (JsonNode item : root.path("sugestoes")) {
                String titulo = item.path("titulo").asText(null);
                String motivo = item.path("motivo").asText(null);
                if (titulo != null && !titulo.isBlank()) {
                    itens.add(new SugestaoDto.Item(titulo, motivo));
                }
            }
            return new SugestaoDto.Response(itens);

        } catch (Exception ex) {
            log.error("[SugestaoService] Erro ao parsear resposta Gemini: {}", ex.getMessage());
            throw new RuntimeException("Falha ao processar resposta da IA", ex);
        }
    }
}