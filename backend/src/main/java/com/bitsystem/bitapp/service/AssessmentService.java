package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.dto.AssessmentDto;
import com.bitsystem.bitapp.exception.BusinessException;
import com.bitsystem.bitapp.integration.N8NAssessmentClient;
import com.bitsystem.bitapp.domain.CareerAssessment;
import com.bitsystem.bitapp.repository.CareerAssessmentRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AssessmentService {

    private static final Logger log = LoggerFactory.getLogger(AssessmentService.class);

    private final N8NAssessmentClient n8nClient;
    private final CareerAssessmentRepository repository;
    private final FallbackStorage fallbackStorage;

    public AssessmentService(N8NAssessmentClient n8nClient, CareerAssessmentRepository repository,
            FallbackStorage fallbackStorage) {
        this.n8nClient = n8nClient;
        this.repository = repository;
        this.fallbackStorage = fallbackStorage;
    }

    public AssessmentDto.Response processar(AssessmentDto.Request request, Long usuarioId) {
        // ── Tentar N8N primeiro ────────────────────────────────────────────
        try {
            AssessmentDto.Request enrichedRequest = new AssessmentDto.Request(
                request.nome(),
                request.idade(),
                request.escolaridade(),
                request.experiencia(),
                request.hardSkills(),
                request.softSkills(),
                request.tecnologias(),
                "orientacao",
                request.idioma()
            );
            AssessmentDto.Response response = n8nClient.process(enrichedRequest);

            // Salvar no banco
            try {
                CareerAssessment assessment = CareerAssessment.builder()
                        .usuarioId(usuarioId)
                        .compatibilidade(response.compatibilidade())
                        .nivel(response.nivel())
                        .build();
                repository.save(assessment);
            } catch (Exception dbEx) {
                log.warn("[AssessmentService] Banco indisponível, salvando assessment em memória");
                fallbackStorage.saveAssessment(usuarioId, response);
            }

            log.info("[AssessmentService] Assessment processado via N8N: usuarioId={}", usuarioId);
            return response;

        } catch (Exception ex) {
            // ── Fallback: resposta local baseada no perfil ─────────────────
            log.warn("[AssessmentService] N8N indisponível, usando resposta local: {}", ex.getMessage());
            AssessmentDto.Response fallbackResponse = buildFallbackAssessment(request);

            try {
                fallbackStorage.saveAssessment(usuarioId, fallbackResponse);
            } catch (Exception storageEx) {
                log.warn("[AssessmentService] Falha ao salvar fallback: {}", storageEx.getMessage());
            }

            return fallbackResponse;
        }
    }

    /**
     * Gera uma avaliação local baseada nas skills e tecnologias informadas.
     * Textos por idioma (pt/es) — o número de compatibilidade não muda, só a redação.
     */
    private AssessmentDto.Response buildFallbackAssessment(AssessmentDto.Request request) {
        boolean es = "es".equals(request.idiomaOuPadrao());
        List<String> hardSkills = request.hardSkills() != null ? request.hardSkills() : List.of();
        List<String> softSkills = request.softSkills() != null ? request.softSkills() : List.of();
        List<String> tecnologias = request.tecnologias() != null ? request.tecnologias() : List.of();

        int totalSkills = hardSkills.size() + softSkills.size() + tecnologias.size();
        int compatibilidade = Math.min(95, Math.max(40, 50 + totalSkills * 5));

        List<String> pontosFortes = new ArrayList<>();
        List<String> gaps = new ArrayList<>();
        List<String> planoDesenvolvimento = new ArrayList<>();

        // Analisar hard skills
        if (hardSkills.stream().anyMatch(s -> s.toLowerCase().contains("java") || s.toLowerCase().contains("spring"))) {
            pontosFortes.add(es ? "Conocimiento sólido en desarrollo Java/Spring Boot" : "Conhecimento sólido em desenvolvimento Java/Spring Boot");
            gaps.add(es ? "Profundizar en microservicios y Cloud Native" : "Aprofundar em microsserviços e Cloud Native");
            planoDesenvolvimento.add(es ? "Estudiar Spring Cloud y Docker/Kubernetes" : "Estudar Spring Cloud e Docker/Kubernetes");
        }
        if (hardSkills.stream().anyMatch(s -> s.toLowerCase().contains("python") || s.toLowerCase().contains("data"))) {
            pontosFortes.add(es ? "Base en análisis y ciencia de datos" : "Base em análise e ciência de dados");
            gaps.add(es ? "Pipeline de datos y modelos de ML" : "Pipeline de dados e Modelos de ML");
            planoDesenvolvimento.add(es ? "Practicar ETL con Airflow y modelado con Scikit-learn" : "Praticar ETL com Airflow e modelagem com Scikit-learn");
        }
        if (hardSkills.stream().anyMatch(s -> s.toLowerCase().contains("react") || s.toLowerCase().contains("javascript"))) {
            pontosFortes.add(es ? "Desarrollo front-end moderno" : "Desenvolvimento front-end moderno");
            gaps.add(es ? "Pruebas automatizadas y rendimiento web" : "Testes automatizados e performance web");
            planoDesenvolvimento.add(es ? "Implementar Jest/RTL y optimizar Core Web Vitals" : "Implementar Jest/RTL e otimizar Core Web Vitals");
        }

        // Soft skills genéricas
        if (softSkills.isEmpty()) {
            pontosFortes.add(es ? "Disposición para el aprendizaje continuo" : "Disposição para aprendizado contínuo");
            gaps.add(es ? "Comunicación técnica y trabajo en equipo" : "Comunicação técnica e trabalho em equipe");
            planoDesenvolvimento.add(es ? "Participar en code reviews y eventos de networking" : "Participar de code reviews e eventos de networking");
        }

        // Gaps padrão se nenhum específico foi detectado
        if (gaps.isEmpty()) {
            gaps.add(es ? "Seguridad de APIs y buenas prácticas de autenticación" : "Segurança de APIs e boas práticas de autenticação");
            gaps.add(es ? "Control de versiones avanzado con Git y CI/CD" : "Controle de versão avançado com Git e CI/CD");
            gaps.add(es ? "Pruebas automatizadas y TDD" : "Testes automatizados e TDD");
        }
        if (planoDesenvolvimento.isEmpty()) {
            planoDesenvolvimento.add(es ? "Complementar formación con rutas gratuitas (Alura ONE, Oracle Next)" : "Complementar formação com trilhas gratuitas (Alura ONE, Oracle Next)");
            planoDesenvolvimento.add(es ? "Construir portafolio con proyectos open-source" : "Construir portfólio com projetos open-source");
            planoDesenvolvimento.add(es ? "Practicar entrevistas técnicas (LeetCode, HackerRank)" : "Praticar entrevistas técnicas (LeetCode, HackerRank)");
        }

        String nivel = compatibilidade >= 70 ? "Júnior Pleno" : "Júnior Trainee";

        return new AssessmentDto.Response(
            compatibilidade,
            nivel,
            pontosFortes,
            gaps,
            planoDesenvolvimento
        );
    }
}
