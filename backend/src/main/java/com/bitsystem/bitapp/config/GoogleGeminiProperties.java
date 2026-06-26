package com.bitsystem.bitapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * CLASSE DE CONFIGURAÇÃO: GoogleGeminiProperties
 * ============================================================================
 * 
 * Encapsula configurações da API Google Gemini carregadas do application.properties
 * com suporte a variáveis de ambiente.
 * 
 * RESPONSABILIDADES:
 * - Mapear propriedades spring.gemini.* para campos Java
 * - Fornecer defaults razoáveis para endpoint, modelo e temperatura
 * - Permitir injeção de dependência segura em GoogleGeminiClient
 * 
 * MAPEAMENTO (application.properties):
 * spring.gemini.api-key=${GEMINI_API_KEY}         → apiKey
 * spring.gemini.endpoint=https://...              → endpoint
 * spring.gemini.model=gemini-1.5-flash                  → model
 * spring.gemini.temperature=0.7                   → temperature
 * spring.gemini.candidate-count=1                 → candidateCount
 * 
 * DEFAULTS:
 * - Endpoint: API v1beta oficial do Google
 * - Model: gemini-pro (modelo padrão estável)
 * - Temperature: 0.7 (equilibrio entre criatividade e determinismo)
 * - CandidateCount: 1 (apenas 1 resposta por requisição)
 * 
 * SEGURANÇA:
 * - API_KEY deve ser variável de ambiente (nunca em git)
 * - Spring Boot injeta via property resolver em tempo de inicialização
 * 
 * @author BiT System
 * @version 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "spring.gemini")
public class GoogleGeminiProperties {

    // ========== CREDENCIAIS ==========
    /** Chave de API do Google Gemini (injetada via ${GEMINI_API_KEY}) */
    private String apiKey;

    // ========== ENDPOINTS ==========
    /** URL base da API Google Gemini v1beta */
    private String endpoint = "https://generativelanguage.googleapis.com/v1beta/models";

    // ========== MODELO ==========
    /** Nome do modelo Gemini a usar (ex: gemini-pro, gemini-1.5-flash) */
    private String model = "gemini-1.5-flash";

    // ========== PARÂMETROS DE GERAÇÃO ==========
    /**
     * Temperature: controla aleatoriedade das respostas (0.0 a 1.0)
     * 0.0 = Determinístico (ideal para tarefas estruturadas)
     * 0.7 = Equilibrio (padrão, bom para criatividade controlada)
     * 1.0 = Máxima aleatoriedade (para criatividade extrema)
     * 
     * Padrão 0.7 é apropriado para acolhimento empático mas coerente
     */
    private Double temperature = 0.7;

    /**
     * Número de respostas candidatas a gerar
     * Padrão 1 = apenas 1 resposta, reduz custo e latência
     */
    private Integer candidateCount = 1;

    // ========== GETTERS & SETTERS ==========
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getCandidateCount() {
        return candidateCount;
    }

    public void setCandidateCount(Integer candidateCount) {
        this.candidateCount = candidateCount;
    }
}
