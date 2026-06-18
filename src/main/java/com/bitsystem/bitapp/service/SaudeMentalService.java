package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.client.GoogleGeminiClient;
import com.bitsystem.bitapp.config.GoogleGeminiProperties;
import com.bitsystem.bitapp.dto.SaudeDto;
import com.bitsystem.bitapp.model.HistoricoSaude;
import com.bitsystem.bitapp.model.Usuario;
import com.bitsystem.bitapp.repository.HistoricoSaudeRepository;
import com.bitsystem.bitapp.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * ============================================================================
 * SERVIÇO: SaudeMentalService
 * ============================================================================
 *
 * Classe de serviços responsável por analisar o bem-estar emocional e estado
 * mental
 * dos utilizadores do ecossistema BiT.
 *
 * Utiliza o Google Gemini como agente inteligente de escuta ativa e acolhimento
 * baseado em Comunicação Não-Violenta (CNV), com um sistema de fallback clínico
 * local caso a cota da API seja excedida.
 *
 * @author BiT System
 * @version 1.2.0
 */
@Service
public class SaudeMentalService {

    private static final Logger log = LoggerFactory.getLogger(SaudeMentalService.class);

    private final GoogleGeminiClient geminiClient;
    private final GoogleGeminiProperties geminiProperties;
    private final HistoricoSaudeRepository saudeRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;
    private final EmotionResponseProvider emotionResponseProvider;

    /**
     * Construtor para injeção de dependências do ecossistema Spring.
     */
    public SaudeMentalService(
            GoogleGeminiClient geminiClient,
            GoogleGeminiProperties geminiProperties,
            HistoricoSaudeRepository saudeRepository,
            UsuarioRepository usuarioRepository,
            ObjectMapper objectMapper,
            EmotionResponseProvider emotionResponseProvider) {
        this.geminiClient = geminiClient;
        this.geminiProperties = geminiProperties;
        this.saudeRepository = saudeRepository;
        this.usuarioRepository = usuarioRepository;
        this.objectMapper = objectMapper;
        this.emotionResponseProvider = emotionResponseProvider;
    }

    /**
     * Avalia o estado mental e o humor reportado pelo utilizador, acionando
     * o agente inteligente para validação e acolhimento emocional.
     *
     * @param request Payload de dados contendo humor, nota semanal e contexto
     * @return SaudeDto.Response Estrutura contendo a resposta acolhedora e plano de
     *         ação imediato
     */
    public SaudeDto.Response avaliarEstadoMental(SaudeDto.Request request) {
        // Alerta de intervenção se a nota de humor semanal for crítica
        boolean derivarCvv = request.notaSemanal() < 4;

        Usuario usuario = usuarioRepository
                .findById(request.usuarioId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Utilizador não encontrado para o ID informado: " + request.usuarioId()));

        // Define a resposta curada dinâmica local para servir de plano B imediato
        // (Fallback)
        SaudeDto.RawResponse rawResponse = obterSuporteLocalDinamico(request);

        if (geminiDisponivel()) {
            try {
                SaudeDto.RawResponse geminiResponse = chamarGemini(request);
                if (geminiResponse != null && StringUtils.hasText(geminiResponse.mensagem())) {
                    rawResponse = geminiResponse;
                }
            } catch (Exception ex) {
                log.warn(
                        "Gemini indisponível ou cota esgotada para suporte emocional. A utilizar o motor local de escuta ativa: {}",
                        ex.getMessage());
            }
        }

        // Persistência do histórico de humor do utilizador no MySQL para acompanhamento
        // clínico
        HistoricoSaude historico = HistoricoSaude.builder()
                .usuario(usuario)
                .humor(request.humor())
                .notaSemanal(request.notaSemanal())
                .contexto(request.contexto())
                .derivouCvv(derivarCvv)
                .build();
        saudeRepository.save(historico);

        String alerta = derivarCvv
                ? "ALERTA_CRITICO: Indicadores de sofrimento severo ou humor debilitado. Direcionando canais de apoio imediato."
                : "ESTÁVEL";

        return new SaudeDto.Response(
                rawResponse.mensagem(),
                rawResponse.acaoSugerida(),
                derivarCvv,
                request.notaSemanal(),
                alerta);
    }

    /**
     * Verifica a conectividade e disponibilidade da API Key do Gemini.
     */
    private boolean geminiDisponivel() {
        return StringUtils.hasText(geminiProperties.getApiKey());
    }

    /**
     * Comunica diretamente com o agente cognitivo do Gemini utilizando
     * técnicas profundas de modulação de tom, CNV e formatação segura de JSON.
     */
    private SaudeDto.RawResponse chamarGemini(SaudeDto.Request request) throws Exception {
        String userPrompt = """
                Tu és o Agente Especialista em Acolhimento, Empatia e Saúde Mental do ecossistema BiT App.
                A tua função não é diagnosticar clinicamente, mas sim prover escuta ativa, validar sentimentos, exercitar a empatia profunda e oferecer caminhos saudáveis de enfrentamento com base na Comunicação Não-Violenta (CNV).

                DADOS CONTEXTUAIS DO UTILIZADOR:
                - Identificador do Utilizador (ID): %d
                - Humor Autodeclarado: %s
                - Nota de Bem-Estar Semanal (0 a 10): %d
                - Contexto / Relato Pessoal: %s

                INSTRUÇÕES CRÍTICAS PARA O PROCESSO DE RESPOSTA:
                1. Analisa o humor e o relato pessoal. Identifica a emoção subjacente (cansaço, ansiedade, frustração, solidão, alegria) e valida essa emoção explicitamente no início da resposta ('mensagem'). Nunca digas que o utilizador "não deveria sentir-se assim".
                2. Se a Nota Semanal for INFERIOR a 4 ou o relato indicar desespero, ansiedade aguda ou ideação grave, deves integrar obrigatoriamente na tua sugestão de ação ('acaoSugerida') o contacto imediato com o CVV (Centro de Valorização da Vida) pelo número nacional 188 no Brasil ou a Linha de Apoio Psicológico do SNS24 (808 24 24 24) em Portugal.
                3. Na 'acaoSugerida', propõe exercícios práticos e imediatos de autorregulação (exemplos: respiração diafragmática 4-7-8, técnica de grounding 5-4-3-2-1, escrita terapêutica ou pequenas pausas reflexivas).

                REQUISITO EXCLUSIVO DE RETORNO:
                Responde APENAS E EXCLUSIVAMENTE com um JSON estruturado e válido, sem qualquer tipo de formatação adicional, delimitadores markdown (como ```json) ou introduções.
                Estrutura exata do JSON:
                {
                  "mensagem": "[Texto empático e humanizado de validação, de 3 a 5 linhas, focado em CNV]",
                  "acaoSugerida": "[Instrução detalhada com exercícios práticos ou direcionamento para suporte]"
                }
                """
                .formatted(
                        request.usuarioId(),
                        request.humor(),
                        request.notaSemanal(),
                        request.contexto() != null ? request.contexto() : "Não providenciado pelo utilizador.");

        String geminiResult = geminiClient.generateText(userPrompt);
        return objectMapper.readValue(
                limparJson(geminiResult),
                SaudeDto.RawResponse.class);
    }

    /**
     * Motor estático de apoio emocional sob medida (Fallback Inteligente).
     * Caso a cota da API diária esteja esgotada, analisa a nota e o humor para
     * criar uma resposta
     * de acolhimento altamente individualizada e segura.
     */
    private SaudeDto.RawResponse obterSuporteLocalDinamico(SaudeDto.Request request) {
        String humorLower = request.humor() != null ? request.humor().toLowerCase() : "";
        int nota = request.notaSemanal();

        // Cenário 1: Crítico (Nota abaixo de 4)
        if (nota < 4) {
            return new SaudeDto.RawResponse(
                    "Olá. Percebo que as coisas têm estado incrivelmente pesadas para ti ultimamente e quero que saibas que a tua dor e o teu cansaço são válidos. Não precisas de passar por isto sem apoio.",
                    "Recomendo fortemente que faças uma pausa imediata. Se sentires que a carga está insustentável, por favor, liga para o Apoio Psicológico do SNS24 pelo 808 24 24 24 (Portugal) ou liga para o CVV através do 188 (Brasil). Conversar com um profissional fará toda a diferença neste momento.");
        }

        // Cenário 2: Instabilidade moderada (Nota entre 4 e 6 ou humor associado a
        // ansiedade/tristeza/cansaço)
        if (nota <= 6 || humorLower.contains("cansado") || humorLower.contains("triste")
                || humorLower.contains("ansioso")) {
            return new SaudeDto.RawResponse(
                    "Compreendo perfeitamente o teu cansaço. O quotidiano exige muito de nós e é perfeitamente normal sentir que a energia está a chegar ao fim ou que a ansiedade está a tentar assumir o controlo.",
                    "Sugiro que realizes agora a técnica de grounding '5-4-3-2-1': foca o teu olhar em 5 coisas à tua volta, toca em 4 texturas diferentes, ouve 3 sons distintos, sente 2 cheiros no ambiente e dá 1 passo focado na tua respiração. Permite-te desacelerar.");
        }

        // Cenário 3: Estável e positivo
        return new SaudeDto.RawResponse(
                "Fico genuinamente feliz por ver que o teu bem-estar está equilibrado e que tens conseguido encontrar estabilidade na tua jornada esta semana!",
                "Aproveita este bom momento de clareza mental para documentar as tuas conquistas recentes ou partilhar uma palavra de ânimo com um colega do teu ecossistema de aprendizagem.");
    }

    /**
     * Remove caracteres invasivos e delimitadores markdown gerados pelas respostas
     * do LLM.
     */
    private String limparJson(String raw) {
        if (raw == null) {
            return "{}";
        }

        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceAll("^```(?:json)?\\s*", "");
            trimmed = trimmed.replaceAll("\\s*```$", "");
        }
        return trimmed.trim();
    }
}
