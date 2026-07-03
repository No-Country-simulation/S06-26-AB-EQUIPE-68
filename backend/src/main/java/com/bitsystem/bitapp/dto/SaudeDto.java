package com.bitsystem.bitapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

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
        
        /** Estado de humor (emoji/rótulo). Alimenta só o TOM do acolhimento —
         *  nunca deriva ao CVV. */
        String humor,

        /** Autoavaliação da semana (0-10). Único campo que decide a derivação
         *  ao CVV: 0-1 = reforçada, 2-3 = preventiva, 4-10 = nenhuma. */
        @NotNull(message = "A nota semanal é obrigatória")
        @Min(value = 0, message = "A nota semanal deve ser no mínimo 0")
        @Max(value = 10, message = "A nota semanal deve ser no máximo 10")
        Integer notaSemanal,

        /** Contexto livre sobre o estado (pressões, desafios, etc) */
        String contexto
    ) {}

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
        
        /** Nota de bem-estar reportada no check-in (0-10) */
        Integer notaAtual,

        /** Token descritivo de status (uso interno/log, não é texto de UI):
         *  DERIVACAO_REFORCADA | DERIVACAO_PREVENTIVA | ESTAVEL */
        String alerta,

        /** Nível da derivação ao CVV, para o frontend escolher o painel:
         *  "REFORCADO" (nota 0-1) | "PREVENTIVO" (nota 2-3) | null (nota 4-10).
         *  Decidido SÓ pela nota; IA/agente/texto nunca influenciam. */
        String nivelDerivacao
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
        String acaoSugerida
    ) {}

    /**
     * RESPOSTA: Item do histórico de check-ins
     * 
     * Retornado por GET /api/saude/historico
     * Lista de registros anteriores do usuário
     */
    public record HistoricoResponse(
        Long id,
        String humor,
        Integer notaSemanal,
        String contexto,
        Boolean derivouCvv,
        java.time.LocalDateTime createdAt
    ) {}
}
