package com.bitsystem.bitapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * ============================================================================
 * CLASSES DTO: SaudeDto
 * ============================================================================
 * 
 * Data Transfer Objects para check-ins de saúde mental.
 * Suporta validação, processamento por IA e armazenamento em histórico.
 * 
 * FLUXO:
 * Client → Request DTO → SaudeMentalService → Gemini API → 
 * RawResponse (parse) → enriquecimento → Response DTO → Client + DB
 * 
 * @author BiT System
 * @version 1.0.0
 */
public class SaudeDto {

    /**
     * REQUISIÇÃO: Check-in de saúde mental do usuário
     * 
     * Enviado via POST /api/saude
     * Dados brutos coletados pelo front-end
     */
    public record Request(
        /** ID do usuário no banco (identificador da sessão) */
        Long usuarioId,

        /** Nota do check-in (escala única CVV v2: 9=Muito feliz, 7=Feliz,
         *  5=Tranquilo, 3=Triste, 1=Muito triste). OPCIONAL — check-in só de
         *  texto (sem emoji) chega com nota=null: não deriva ao CVV, não
         *  entra na agregação diária nem na tendência semanal, só acolhimento.
         *  Único campo que decide a derivação ao CVV quando presente: nota 1 =
         *  reforçada, nota 3 = preventiva, nota 5/7/9 = nenhuma. A nota NUNCA
         *  é inferida do texto livre ou de IA (ver NivelCheckin). */
        @Min(value = 1, message = "Nota de check-in inválida")
        @Max(value = 9, message = "Nota de check-in inválida")
        Integer nota,

        /** Contexto livre sobre o estado (pressões, desafios, etc) */
        String contexto,

        /** Idioma da UI ("pt"|"es"), default "pt" — campo aditivo (lote i18n). */
        String idioma
    ) {
        public String idiomaOuPadrao() {
            return "es".equalsIgnoreCase(idioma) ? "es" : "pt";
        }
    }

    /**
     * RESPOSTA: Acolhimento e sugestões de ação emitidas por IA
     * 
     * Retornado por POST /api/saude
     * Enriquecido com derivação CVV e alertas críticos
     */
    public record Response(
        /** Mensagem empática de acolhimento gerada pelo Gemini */
        String mensagem,
        
        /** Ação prática sugerida para melhoria imediata */
        String acaoSugerida,
        
        /** Flag: necessita derivação para Centro de Valorização da Vida? */
        Boolean derivarCvv,
        
        /** Nota do check-in reportada (escala única: 9/7/5/3/1), ou null
         *  quando o check-in foi só texto. */
        Integer nota,

        /** Token descritivo de status (uso interno/log, não é texto de UI):
         *  DERIVACAO_REFORCADA | DERIVACAO_PREVENTIVA | ESTAVEL */
        String alerta,

        /** Nível da derivação ao CVV, para o frontend escolher o painel:
         *  "REFORCADO" (nota 1) | "PREVENTIVO" (nota 3) | null (nota 5/7/9 ou
         *  ausente). Decidido SÓ pela nota; IA/agente/texto nunca influenciam. */
        String nivelDerivacao,

        /** Frase curta e empática descrevendo o estado emocional percebido
         *  (emoji e/ou texto do check-in). Nunca cita a nota numérica. Sempre
         *  preenchida — via IA (Gemini) ou fallback determinístico. Campo
         *  aditivo (lote 4.1). */
        String leituraEmocional
    ) {}

    /**
     * RESPOSTA BRUTA: Parse intermediário da resposta Gemini
     *
     * Mapeamento JSON simples da IA antes do pós-processamento
     * Usado internamente por SaudeMentalService
     */
    public record RawResponse(
        /** Mensagem bruta do Gemini */
        String mensagem,

        /** Ação bruta do Gemini */
        String acaoSugerida,

        /** Leitura emocional bruta do Gemini (pode ser null nas fontes que só
         *  fornecem texto, ex.: agente n8n — nesse caso o serviço aplica um
         *  fallback determinístico). Campo aditivo (lote 4.1). */
        String leituraEmocional
    ) {
        /** Construtor de conveniência para as fontes que só fornecem
         *  mensagem/ação (curadas, agente n8n) — leituraEmocional fica null e
         *  é preenchida por SaudeMentalService.leituraEmocionalFallback. */
        public RawResponse(String mensagem, String acaoSugerida) {
            this(mensagem, acaoSugerida, null);
        }
    }

    /**
     * RESPOSTA: Item do histórico de check-ins
     * 
     * Retornado por GET /api/saude/historico
     * Lista de registros anteriores do usuário
     */
    public record HistoricoResponse(
        Long id,
        Integer nota,
        String contexto,
        Boolean derivouCvv,
        java.time.LocalDateTime createdAt
    ) {}
}
