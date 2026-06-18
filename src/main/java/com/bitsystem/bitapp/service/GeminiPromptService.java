package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.client.GoogleGeminiClient;
import org.springframework.stereotype.Service;

/**
 * ============================================================================
 * SERVIÇO: GeminiPromptService
 * ============================================================================
 * 
 * Abstração simplificada para chamadas genéricas à API Gemini.
 * Atua como adapter entre controller/services e GoogleGeminiClient.
 * 
 * RESPONSABILIDADES:
 * - Fornecer interface simplificada complete(prompt)
 * - Delegar para GoogleGeminiClient a complexidade HTTP
 * - Reutilizável para qualquer prompt genérico
 * 
 * PADRÃO:
 * Este service pode ser expandido com:
 * - cache de respostas frequentes
 * - retry logic com backoff exponencial
 * - logging/métricas de latência
 * - fallback para modelo alternativo (ex: OpenAI)
 * 
 * NOTA: Atualmente é um pass-through. Em produção, adicione:
 * 1. Validação de entrada (size máximo, conteúdo)
 * 2. Rate limiting per user
 * 3. Caching distribuído (Redis)
 * 4. Monitoramento de custos (tokens consumidos)
 * 
 * @author BiT System
 * @version 1.0.0
 */
@Service
public class GeminiPromptService {

    private final GoogleGeminiClient geminiClient;

    /**
     * Construtor com injeção do cliente Gemini
     * 
     * @param geminiClient Cliente HTTP pré-configurado
     */
    public GeminiPromptService(GoogleGeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    /**
     * Completa um prompt genérico via Gemini
     * 
     * FLUXO:
     * 1. Valida entrada (na produção)
     * 2. Chama geminiClient.generateText()
     * 3. Retorna resposta bruta (sem parsing)
     * 
     * CASOS DE USO:
     * - Prompts genéricos não estruturados
     * - Testes/debugging da API
     * - Assistentes de conversa simples
     * 
     * @param prompt Texto da instrução para Gemini (em português)
     * @return Resposta em string (pode ser JSON, markdown, plain text)
     * @throws IllegalStateException se Gemini retornar vazio
     */
    public String complete(String prompt) {
        return geminiClient.generateText(prompt);
    }
}
