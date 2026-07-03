package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.domain.Curso;
import com.bitsystem.bitapp.integration.GeminiClient;
import com.bitsystem.bitapp.repository.CursoRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Gera o texto "como resolver" do match vaga x usuário: skills faltantes +
 * cursos do catálogo cujas áreas casam com elas. O número do match já foi
 * calculado em Java (VagaMatchService) — aqui só entra a redação da IA
 * (reusa o padrão do GeminiClient/OrientacaoService), com fallback determinístico.
 */
@Service
public class ComoResolverService {

    private static final Logger log = LoggerFactory.getLogger(ComoResolverService.class);

    /** Palavra-chave normalizada (sem acento/lowercase) -> área do catálogo de cursos/vagas. */
    private static final Map<String, String> AREA_POR_PALAVRA_CHAVE = Map.ofEntries(
        Map.entry("java", "Java"),
        Map.entry("spring", "Java"),
        Map.entry("kafka", "Java"),
        Map.entry("microservices", "Java"),
        Map.entry("microsservicos", "Java"),
        Map.entry("javascript", "Web"),
        Map.entry("typescript", "Web"),
        Map.entry("react", "Web"),
        Map.entry("angular", "Web"),
        Map.entry("vue", "Web"),
        Map.entry("html", "Web"),
        Map.entry("css", "Web"),
        Map.entry("node", "Web"),
        Map.entry("python", "Dados"),
        Map.entry("sql", "Dados"),
        Map.entry("power bi", "Dados"),
        Map.entry("tableau", "Dados"),
        Map.entry("spark", "Dados"),
        Map.entry("hadoop", "Dados"),
        Map.entry("docker", "Infraestrutura"),
        Map.entry("kubernetes", "Infraestrutura"),
        Map.entry("aws", "Infraestrutura"),
        Map.entry("azure", "Infraestrutura"),
        Map.entry("gcp", "Infraestrutura"),
        Map.entry("linux", "Infraestrutura"),
        Map.entry("terraform", "Infraestrutura")
    );

    private static final String FALLBACK_TOTAL = "Você já atende a todos os requisitos desta vaga. Continue assim!";

    private final GeminiClient geminiClient;
    private final CursoRepository cursoRepository;

    public ComoResolverService(GeminiClient geminiClient, CursoRepository cursoRepository) {
        this.geminiClient = geminiClient;
        this.cursoRepository = cursoRepository;
    }

    /** matchPercentual null (vaga sem tecnologias) -> não há o que resolver. */
    public String gerar(Integer matchPercentual, List<String> skillsFaltantes) {
        if (matchPercentual == null) {
            return null;
        }
        if (skillsFaltantes.isEmpty()) {
            return FALLBACK_TOTAL;
        }

        List<Curso> cursos = buscarCursosRelacionados(skillsFaltantes);

        if (geminiClient.isConfigured()) {
            try {
                String prompt = buildPrompt(skillsFaltantes, cursos);
                String resposta = geminiClient.generateContent(prompt);
                return resposta.trim();
            } catch (Exception ex) {
                log.warn("[ComoResolverService] Gemini indisponivel, usando fallback local: {}", ex.getMessage());
            }
        }
        return fallback(skillsFaltantes, cursos);
    }

    private List<Curso> buscarCursosRelacionados(List<String> skillsFaltantes) {
        Set<String> areasAlvo = new LinkedHashSet<>();
        for (String skill : skillsFaltantes) {
            String normalizado = VagaMatchService.canonicalizar(skill);
            AREA_POR_PALAVRA_CHAVE.forEach((chave, area) -> {
                if (normalizado.contains(chave) || chave.contains(normalizado)) {
                    areasAlvo.add(area);
                }
            });
        }
        if (areasAlvo.isEmpty()) {
            return List.of();
        }
        return cursoRepository.findByAtivaTrue().stream()
                .filter(c -> areasAlvo.contains(c.getArea()))
                .limit(3)
                .collect(Collectors.toList());
    }

    private String buildPrompt(List<String> skillsFaltantes, List<Curso> cursos) {
        String faltantesTexto = String.join(", ", skillsFaltantes);
        String cursosTexto = cursos.isEmpty()
                ? "nenhum curso específico encontrado no catálogo"
                : cursos.stream().map(Curso::getTitulo).collect(Collectors.joining(", "));

        return String.format("""
            Voce e um orientador de carreira do BiT. O usuario nao atende as seguintes
            competencias exigidas por uma vaga: %s.
            Cursos disponiveis no catalogo do BiT que podem ajudar: %s.
            Escreva uma recomendacao curta (maximo 3 frases) sobre como o usuario pode
            evoluir nessas competencias, citando o(s) curso(s) pelo nome quando houver.
            Responda no idioma do usuario. Retorne APENAS o texto da recomendacao, sem markdown.
            """,
            faltantesTexto,
            cursosTexto
        );
    }

    private String fallback(List<String> skillsFaltantes, List<Curso> cursos) {
        String faltantesTexto = String.join(", ", skillsFaltantes);
        if (cursos.isEmpty()) {
            return "Para avançar nesta vaga, foque em: " + faltantesTexto + ".";
        }
        String nomes = cursos.stream().map(Curso::getTitulo).collect(Collectors.joining(", "));
        return "Para avançar nesta vaga, foque em: " + faltantesTexto + ". Cursos no BiT: " + nomes + ".";
    }
}
