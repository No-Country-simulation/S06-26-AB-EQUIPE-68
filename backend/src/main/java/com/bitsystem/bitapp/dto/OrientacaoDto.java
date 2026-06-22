package com.bitsystem.bitapp.dto;

import java.util.List;

/**
 * ============================================================================
 * CLASSES DTO: OrientacaoDto
 * ============================================================================
 * 
 * Data Transfer Objects para encapsular requisições e respostas do endpoint
 * de orientação profissional e acadêmica.
 * 
 * FLUXO:
 * Client → Request DTO → OrientacaoService → Gemini API → Response DTO → Client
 * 
 * MOTIVO DE USAR RECORDS:
 * - Imutabilidade (thread-safe para APIs concorrentes)
 * - Getters/equals/hashCode/toString gerados automaticamente
 * - Código conciso sem boilerplate
 * 
 * @author BiT System
 * @version 1.0.0
 */
public class OrientacaoDto {
    
    /**
     * REQUISIÇÃO: Dados do perfil do usuário para análise de orientação
     * 
     * Enviado pelo front via POST /api/orientar
     * Processado por OrientacaoService e repassado ao Gemini
     */
    public record Request(
        /** ID do usuário no banco (identificador da sessão) */
        Long usuarioId,
        
        /** Resumo do perfil (competências, experiência, etc) */
        String perfil,
        
        /** Nível de expertise desejado (ex: Iniciante, Mid, Senior) */
        String nivel,
        
        /** Região/país do usuário (para recomendações regionalizadas) */
        String regiao,
        
        /** Idioma preferido para respostas da IA */
        String idioma,
        
        /** Latitude da localização do usuário (CDRView) */
        Double lat,
        
        /** Longitude da localização do usuário (CDRView) */
        Double lng
    ) {}

    /**
     * RESPOSTA: Análise estruturada de orientação profissional gerada pelo Gemini
     * 
     * Retornado por POST /api/orientar após processamento
     * Parseado de JSON via ObjectMapper
     */
    public record Response(
        /** Percentual do gap de competências (0-100) */
        Integer gapPercentual,
        
        /** Lista de competências faltantes para o objetivo */
        List<String> gapItens,
        
        /** Trilha de estudo sugerida (cursos, projetos, livros) */
        List<String> trilhaSugerida,
        
        /** Vagas de emprego compatíveis com o perfil */
        List<String> vagasCompatibles,
        
        /** Nível de confiança da análise (0.0 a 1.0) */
        Double confianca
    ) {}
}
