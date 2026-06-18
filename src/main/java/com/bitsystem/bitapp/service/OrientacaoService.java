package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.client.GoogleGeminiClient;
import com.bitsystem.bitapp.config.GoogleGeminiProperties;
import com.bitsystem.bitapp.dto.OrientacaoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * ============================================================================
 * SERVIÇO: OrientacaoService
 * ============================================================================
 *
 * Classe responsável por gerir as orientações de carreira no ecossistema BiT.
 * Integra-se diretamente com a API do Google Gemini para decisões em tempo real
 * e fornece um motor de regras de contingência dinâmico (fallback) caso a chave
 * de API atinja os limites de cota de utilização diária.
 *
 * @author BiT System
 * @version 1.1.0
 */
@Service
public class OrientacaoService {

    private static final Logger log = LoggerFactory.getLogger(OrientacaoService.class);

    private final GoogleGeminiClient geminiClient;
    private final GoogleGeminiProperties geminiProperties;
    private final ObjectMapper objectMapper;

    /**
     * Construtor para injeção de dependências do Spring Boot.
     */
    public OrientacaoService(
            GoogleGeminiClient geminiClient,
            GoogleGeminiProperties geminiProperties,
            ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.geminiProperties = geminiProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * Processa o pedido de orientação cruzando informações geográficas, técnicas
     * e o nível de experiência para obter uma resposta dinâmica da IA.
     *
     * @param request Dados de entrada do perfil do utilizador
     * @return OrientacaoDto.Response Resposta estruturada com gaps e caminhos de
     *         aprendizagem
     */
    public OrientacaoDto.Response processarOrientacao(OrientacaoDto.Request request) {
        // Gera uma resposta dinâmica de contingência caso ocorra falha ou estouro de
        // cota
        OrientacaoDto.Response fallback = respostaFallbackDinamica(request);

        if (!geminiDisponivel()) {
            log.info("Chave de API do Gemini não configurada. A utilizar o motor de contingência dinâmico.");
            return fallback;
        }

        try {
            // Prompt avançado para autonomia de tomada de decisão do agente inteligente
            String userPrompt = """
                    Tu és o Mentor de Inteligência Artificial e Especialista de Carreira sénior do ecossistema BiT App.
                    O teu objetivo é analisar o perfil técnico, geográfico e de experiência de forma profunda e humanizada.

                    Analisa cuidadosamente os seguintes dados do utilizador:
                    - Identificador do Utilizador (ID): %d
                    - Perfil Técnico/Foco Atual: %s
                    - Nível de Experiência Indicado: %s
                    - Região Administrativa: %s
                    - Idioma Preferencial: %s
                    - Coordenadas de Localização (Latitude/Longitude): %s, %s

                    INSTRUÇÕES CRÍTICAS PARA O AGENTE DE IA:
                    1. Calcula o 'gapPercentual' (valor inteiro de 0 a 100) que representa a distância atual do utilizador para a proficiência ideal do seu nível.
                    2. Enumera no vetor 'gapItens' as competências técnicas (hard skills) ou comportamentais (soft skills) mais críticas que faltam para o perfil atual dele.
                    3. Sugere no vetor 'trilhaSugerida' cursos, bootcamps e especializações de plataformas de educação reais e respeitadas (como Alura, Santander Open Academy, Coursera, Udemy, Microsoft Learn, Oracle NEXT ou edX) que resolvam estes gaps de forma rápida.
                    4. Identifica no vetor 'vagasCompatibles' títulos de vagas e posições reais no mercado português/internacional condizentes com o nível dele, adicionando uma estimativa percentual de aderência.

                    REQUISITO IMPERATIVO DE SAÍDA:
                    Deves retornar APENAS e EXCLUSIVAMENTE um objeto JSON válido, perfeitamente formatado de acordo com a estrutura abaixo.
                    Não incluas explicações, blocos de formatação markdown (como ```json) ou qualquer caractere adicional fora do objeto JSON.

                    Estrutura de saída esperada:
                    {
                      "gapPercentual": 25,
                      "gapItens": ["Competência A", "Competência B"],
                      "trilhaSugerida": ["Curso X na Plataforma Y", "Especialização Z"],
                      "vagasCompatibles": ["Cargo W - Aderência K%%"],
                      "confianca": 0.95
                    }
                    """
                    .formatted(
                            request.usuarioId(),
                            request.perfil(),
                            request.nivel(),
                            request.regiao(),
                            request.idioma(),
                            request.lat(),
                            request.lng());

            // Comunicação com a API cliente do Gemini
            String geminiResult = geminiClient.generateText(userPrompt);

            // Desserialização segura do JSON limpo para o DTO de resposta
            return objectMapper.readValue(
                    limparJson(geminiResult),
                    OrientacaoDto.Response.class);
        } catch (Exception ex) {
            log.warn(
                    "Falha na comunicação ou processamento da API Gemini (Cota excedida, timeout ou erro de parser). A acionar contingência dinâmica: {}",
                    ex.getMessage());
            return fallback;
        }
    }

    /**
     * Valida se a propriedade da API Key está devidamente mapeada e ativa.
     */
    private boolean geminiDisponivel() {
        return StringUtils.hasText(geminiProperties.getApiKey());
    }

    /**
     * Gera uma resposta estruturada de forma inteligente com base no perfil de
     * entrada,
     * garantindo que o utilizador final tenha dados úteis e contextualizados mesmo
     * sem a API online.
     *
     * @param request Dados originais enviados pelo utilizador
     * @return OrientacaoDto.Response Estrutura dinâmica adequada à stack
     *         tecnológica
     */
    private OrientacaoDto.Response respostaFallbackDinamica(OrientacaoDto.Request request) {
        String perfilLower = request.perfil() != null ? request.perfil().toLowerCase() : "";

        List<String> gaps = new ArrayList<>();
        List<String> cursos = new ArrayList<>();
        List<String> vagas = new ArrayList<>();
        int gapPercentual = 35;

        // Análise condicional dinâmica: Perfis Backend e Java
        if (perfilLower.contains("java") || perfilLower.contains("spring") || perfilLower.contains("backend")
                || perfilLower.contains("sql")) {
            gaps.addAll(List.of(
                    "Segurança de APIs com Spring Security e OAuth2",
                    "Desenho de Arquitetura de Microsserviços",
                    "Otimização de Índices e Queries em Bases de Dados Relacionais"));
            cursos.addAll(List.of(
                    "Formação Java e Spring Boot - Alura em Parceria com a Oracle (ONE)",
                    "Especialização em Cloud Computing e AWS Services para Java Developers",
                    "Curso Avançado de Padrões de Desenho e Clean Architecture"));
            vagas.addAll(List.of(
                    "Programador Backend Java Júnior — Aderência estimada: 75%",
                    "Engenheiro de Software Júnior (Foco em Spring Framework) — Aderência estimada: 70%"));
            gapPercentual = 30;
        }
        // Análise condicional dinâmica: Perfis de Dados e Inteligência Artificial
        else if (perfilLower.contains("dado") || perfilLower.contains("data") || perfilLower.contains("analis")
                || perfilLower.contains("python")) {
            gaps.addAll(List.of(
                    "Construção de Pipelines de Dados ETL/ELT complexos",
                    "Desenvolvimento de Modelos de Machine Learning Supervisionados",
                    "Visualização de Dados Dinâmica e Dashboards Executivos no Power BI"));
            cursos.addAll(List.of(
                    "Formação Completa em Data Science e Machine Learning - Santander Open Academy",
                    "Curso Avançado de Linguagem SQL para Análise Estatística",
                    "Bootcamp Engenheiro de Dados Júnior - Plataforma Descomplica / FIAP"));
            vagas.addAll(List.of(
                    "Analista de Dados Júnior — Aderência estimada: 80%",
                    "Cientista de Dados Trainee (Inclusão de Talentos Tech) — Aderência estimada: 65%"));
            gapPercentual = 25;
        }
        // Análise condicional dinâmica: Desenvolvimento Web, Mobile e Frontend
        else {
            gaps.addAll(List.of(
                    "Gestão de Estados Globais e Integração Assíncrona com Axios",
                    "Estruturação de Testes Unitários e de Integração com Jest/RTL",
                    "Otimização de Desempenho e Core Web Vitals"));
            cursos.addAll(List.of(
                    "Especialização Front-End Moderno (React, TypeScript & Tailwind CSS) - Alura",
                    "Desenvolvimento Web Full Stack Responsivo - Programa Oracle Next Education",
                    "Gestão Ágil de Projetos com Scrum e Kanban no Jira"));
            vagas.addAll(List.of(
                    "Desenvolvedor Frontend Web Júnior — Aderência estimada: 70%",
                    "Programador Javascript Trainee (Vaga Inclusiva) — Aderência estimada: 85%"));
        }

        return new OrientacaoDto.Response(gapPercentual, gaps, cursos, vagas, 0.90);
    }

    /**
     * Limpa os metadados gerados pela formatação Markdown da Inteligência
     * Artificial,
     * garantindo que apenas a string JSON pura seja passada para o desserializador.
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
