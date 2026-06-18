package com.bitsystem.bitapp.dto;

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
        
        /** Estado de humor (ex: "😢 Triste", "😰 Ansioso", "😊 Feliz") */
        String humor,
        
        /** Nota numérica de bem-estar (1-5)
         *  1 = Muito ruim (risco)
         *  5 = Excelente */
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
        
        /** Nota de bem-estar reportada no check-in */
        Integer notaAtual,
        
        /** Alerta crítico (texto descritivo) */
        String alerta
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
}
