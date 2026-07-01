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
                "orientacao"
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
     */
    private AssessmentDto.Response buildFallbackAssessment(AssessmentDto.Request request) {
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
            pontosFortes.add("Conhecimento sólido em desenvolvimento Java/Spring Boot");
            gaps.add("Aprofundar em microsserviços e Cloud Native");
            planoDesenvolvimento.add("Estudar Spring Cloud e Docker/Kubernetes");
        }
        if (hardSkills.stream().anyMatch(s -> s.toLowerCase().contains("python") || s.toLowerCase().contains("data"))) {
            pontosFortes.add("Base em análise e ciência de dados");
            gaps.add("Pipeline de dados e Modelos de ML");
            planoDesenvolvimento.add("Praticar ETL com Airflow e modelagem com Scikit-learn");
        }
        if (hardSkills.stream().anyMatch(s -> s.toLowerCase().contains("react") || s.toLowerCase().contains("javascript"))) {
            pontosFortes.add("Desenvolvimento front-end moderno");
            gaps.add("Testes automatizados e performance web");
            planoDesenvolvimento.add("Implementar Jest/RTL e otimizar Core Web Vitals");
        }

        // Soft skills genéricas
        if (softSkills.isEmpty()) {
            pontosFortes.add("Disposição para aprendizado contínuo");
            gaps.add("Comunicação técnica e trabalho em equipe");
            planoDesenvolvimento.add("Participar de code reviews e eventos de networking");
        }

        // Gaps padrão se nenhum específico foi detectado
        if (gaps.isEmpty()) {
            gaps.add("Segurança de APIs e boas práticas de autenticação");
            gaps.add("Controle de versão avançado com Git e CI/CD");
            gaps.add("Testes automatizados e TDD");
        }
        if (planoDesenvolvimento.isEmpty()) {
            planoDesenvolvimento.add("Complementar formação com trilhas gratuitas (Alura ONE, Oracle Next)");
            planoDesenvolvimento.add("Construir portfólio com projetos open-source");
            planoDesenvolvimento.add("Praticar entrevistas técnicas (LeetCode, HackerRank)");
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
