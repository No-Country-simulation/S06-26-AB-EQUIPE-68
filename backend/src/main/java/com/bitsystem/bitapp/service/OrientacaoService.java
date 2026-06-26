package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.dto.OrientacaoDto;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrientacaoService {

    private static final Logger log = LoggerFactory.getLogger(OrientacaoService.class);

    public OrientacaoDto.Response processarOrientacao(OrientacaoDto.Request request) {
        return respostaFallbackDinamica(request);
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
        } else if (perfilLower.contains("dado") || perfilLower.contains("data") || perfilLower.contains("analis")
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
        } else {
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
}
