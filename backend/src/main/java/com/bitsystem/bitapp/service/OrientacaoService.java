package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.dto.OrientacaoDto;
import com.bitsystem.bitapp.integration.GeminiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrientacaoService {

    private static final Logger log = LoggerFactory.getLogger(OrientacaoService.class);

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public OrientacaoService(GeminiClient geminiClient, ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    public OrientacaoDto.Response processarOrientacao(OrientacaoDto.Request request) {
        if (geminiClient.isConfigured()) {
            try {
                return chamarGemini(request);
            } catch (Exception ex) {
                log.warn("[OrientacaoService] Gemini indisponivel, usando fallback local: {}", ex.getMessage());
            }
        }
        return respostaFallbackDinamica(request);
    }

    private OrientacaoDto.Response chamarGemini(OrientacaoDto.Request request) throws Exception {
        String prompt = buildPrompt(request);
        String resposta = geminiClient.generateContent(prompt);
        return parsearResposta(resposta);
    }

    private String buildPrompt(OrientacaoDto.Request request) {
        return String.format("""
            Voce e um orientador profissional especializado em tecnologia da informacao.
            Analise o perfil do usuario e retorne um JSON EXATAMENTE neste formato:

            {
              "gapPercentual": <numero 0-100>,
              "gapItens": ["item1", "item2", "item3"],
              "trilhaSugerida": ["curso1", "curso2", "curso3"],
              "vagasCompatibles": ["vaga1", "vaga2"],
              "confianca": <numero 0.0-1.0>
            }

            Perfil do usuario:
            - Competencias: %s
            - Nivel profissional: %s
            - Regiao: %s

            Retorne APENAS o JSON, sem texto adicional.
            """,
            request.perfil(),
            request.nivel(),
            request.regiao()
        );
    }

    @SuppressWarnings("unchecked")
    private OrientacaoDto.Response parsearResposta(String resposta) {
        try {
            String jsonStr = resposta.trim();
            if (jsonStr.contains("```")) {
                jsonStr = jsonStr.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
            }

            // Extrair apenas o bloco JSON (ignorar texto antes/depois)
            int start = jsonStr.indexOf('{');
            int end = jsonStr.lastIndexOf('}');
            if (start >= 0 && end > start) {
                jsonStr = jsonStr.substring(start, end + 1);
            }

            JsonNode root = objectMapper.readTree(jsonStr);

            int gapPercentual = root.path("gapPercentual").asInt(30);

            List<String> gapItens = new ArrayList<>();
            if (root.has("gapItens")) {
                root.path("gapItens").forEach(node -> gapItens.add(node.asText()));
            }

            List<String> trilhaSugerida = new ArrayList<>();
            if (root.has("trilhaSugerida")) {
                root.path("trilhaSugerida").forEach(node -> trilhaSugerida.add(node.asText()));
            }

            List<String> vagasCompatibles = new ArrayList<>();
            if (root.has("vagasCompatives")) {
                root.path("vagasCompatives").forEach(node -> vagasCompatibles.add(node.asText()));
            } else if (root.has("vagasCompatibles")) {
                root.path("vagasCompatibles").forEach(node -> vagasCompatibles.add(node.asText()));
            }

            double confianca = root.path("confianca").asDouble(0.8);

            return new OrientacaoDto.Response(gapPercentual, gapItens, trilhaSugerida, vagasCompatibles, confianca);

        } catch (Exception ex) {
            log.error("[OrientacaoService] Erro ao parsear resposta Gemini: {}", ex.getMessage());
            throw new RuntimeException("Falha ao processar resposta da IA", ex);
        }
    }

    private OrientacaoDto.Response respostaFallbackDinamica(OrientacaoDto.Request request) {
        String perfilLower = request.perfil() != null ? request.perfil().toLowerCase() : "";

        List<String> gaps = new ArrayList<>();
        List<String> cursos = new ArrayList<>();
        List<String> vagas = new ArrayList<>();
        int gapPercentual = 35;

        if (perfilLower.contains("java") || perfilLower.contains("spring") || perfilLower.contains("backend")
                || perfilLower.contains("sql")) {
            gaps.addAll(List.of(
                    "Seguranca de APIs com Spring Security e OAuth2",
                    "Desenho de Arquitetura de Microsservicos",
                    "Otimizacao de Indices e Queries em Bases de Dados Relacionais"));
            cursos.addAll(List.of(
                    "Formacao Java e Spring Boot - Alura em Parceria com a Oracle (ONE)",
                    "Especializacao em Cloud Computing e AWS Services para Java Developers",
                    "Curso Avancado de Padroes de Desenho e Clean Architecture"));
            vagas.addAll(List.of(
                    "Programador Backend Java Junior - Aderencia estimada: 75%",
                    "Engenheiro de Software Junior (Foco em Spring Framework) - Aderencia estimada: 70%"));
            gapPercentual = 30;
        } else if (perfilLower.contains("dado") || perfilLower.contains("data") || perfilLower.contains("analis")
                || perfilLower.contains("python")) {
            gaps.addAll(List.of(
                    "Construcao de Pipelines de Dados ETL/ELT complexos",
                    "Desenvolvimento de Modelos de Machine Learning Supervisionados",
                    "Visualizacao de Dados Dinamica e Dashboards Executivos no Power BI"));
            cursos.addAll(List.of(
                    "Formacao Completa em Data Science e Machine Learning - Santander Open Academy",
                    "Curso Avancado de Linguagem SQL para Analise Estatistica",
                    "Bootcamp Engenheiro de Dados Junior - Plataforma Descomplica / FIAP"));
            vagas.addAll(List.of(
                    "Analista de Dados Junior - Aderencia estimada: 80%",
                    "Cientista de Dados Trainee (Inclusao de Talentos Tech) - Aderencia estimada: 65%"));
            gapPercentual = 25;
        } else {
            gaps.addAll(List.of(
                    "Gestao de Estados Globais e Integracao Assincrona com Axios",
                    "Estruturacao de Testes Unitarios e de Integracao com Jest/RTL",
                    "Otimizacao de Desempenho e Core Web Vitals"));
            cursos.addAll(List.of(
                    "Especializacao Front-End Moderno (React, TypeScript & Tailwind CSS) - Alura",
                    "Desenvolvimento Web Full Stack Responsivo - Programa Oracle Next Education",
                    "Gestao Agil de Projetos com Scrum e Kanban no Jira"));
            vagas.addAll(List.of(
                    "Desenvolvedor Frontend Web Junior - Aderencia estimada: 70%",
                    "Programador Javascript Trainee (Vaga Inclusiva) - Aderencia estimada: 85%"));
        }

        return new OrientacaoDto.Response(gapPercentual, gaps, cursos, vagas, 0.90);
    }
}
