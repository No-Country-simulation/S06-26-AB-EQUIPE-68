package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.domain.Curso;
import com.bitsystem.bitapp.repository.CursoRepository;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class CursoSeedService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CursoSeedService.class);

    private final CursoRepository cursoRepository;

    private static final String[] INSTITUICOES_GRATUITAS = {
        "SENAI Florianópolis", "SENAI São José", "SENAI Biguaçu",
        "SENAIC", "SENAC Florianópolis", "SENAC São José",
        "SENAT Florianópolis", "IFSC - Instituto Federal de SC",
        "CETEJ - Centro de Tecnologia da Informação",
        "Projeto Futuro Digital", "Fundação Catarinense de Educação",
        "Associação Comunitária Monte Azul", "Casa de Apoio Social",
        "ONG Ação Comunitária", "Centro Social Padre Eustáquio"
    };

    private static final String[] INSTITUICOES_PAGAS = {
        "Unicesumar", "UNISUL", "PUCRS Online", "Estácio EAD",
        "Kroton Educacional", "Anhanguera Digital", "Descomplica Faculdade",
        "FIAP Online", "DIO - Digital Innovation One", "Rocketseat",
        "Alura", "Udemy Business", "Coursera for Business"
    };

    private static final String[][] CURSO_TEMPLATES = {
        // {titulo, area, nivel, duracao, modalidade, gratuitasTags}
        {"Lógica de Programação", "Programação", "Básico", "40h", "Online", "true"},
        {"Desenvolvimento Web Completo", "Web", "Básico", "120h", "Online", "true"},
        {"Java para Iniciantes", "Java", "Básico", "80h", "Online", "true"},
        {"Python e Ciência de Dados", "Dados", "Básico", "60h", "Online", "true"},
        {"HTML, CSS e JavaScript", "Web", "Básico", "50h", "Online", "true"},
        {"React do Zero ao Pro", "Web", "Intermediário", "90h", "Online", "true"},
        {"Spring Boot Essencial", "Java", "Intermediário", "70h", "Online", "true"},
        {"SQL e Banco de Dados", "Dados", "Básico", "40h", "Online", "true"},
        {"Power BI para Negócios", "Dados", "Básico", "30h", "Online", "true"},
        {"DevOps e Cloud Computing", "DevOps", "Intermediário", "100h", "Online", "true"},
        {"Docker e Kubernetes", "DevOps", "Intermediário", "60h", "Online", "true"},
        {"Inteligência Artificial com Python", "IA", "Intermediário", "80h", "Online", "true"},
        {"Machine Learning Aplicado", "IA", "Avançado", "100h", "Online", "true"},
        {"Segurança da Informação", "Cibersegurança", "Básico", "40h", "Online", "true"},
        {"Redes de Computadores", "Infraestrutura", "Básico", "60h", "Presencial", "true"},
        {"Administração de Linux", "Infraestrutura", "Intermediário", "50h", "Presencial", "true"},
        {"Flutter - Apps Mobile", "Mobile", "Básico", "70h", "Online", "true"},
        {"Unity para Iniciantes", "Games", "Básico", "80h", "Online", "true"},
        {"UI/UX Design com Figma", "UI/UX", "Básico", "40h", "Online", "true"},
        {"Automação com Python", "Dados", "Intermediário", "45h", "Online", "true"},
        {"JavaScript Avançado", "Web", "Avançado", "60h", "Online", "false"},
        {"Arquitetura de Microsserviços", "Java", "Avançado", "80h", "Online", "false"},
        {"Data Engineering com Spark", "Dados", "Avançado", "90h", "Online", "false"},
        {"AWS Certified Solutions Architect", "DevOps", "Avançado", "120h", "Online", "false"},
        {"Certificação LPIC-1 Linux", "Infraestrutura", "Intermediário", "80h", "Presencial", "false"},
        {"Desenvolvimento iOS com Swift", "Mobile", "Intermediário", "100h", "Online", "false"},
        {"Criação de Games com Unreal", "Games", "Intermediário", "120h", "Online", "false"},
        {"Design System e Componentização", "UI/UX", "Intermediário", "50h", "Online", "false"},
    };

    private static final String[] AREAS = {"Programação", "Web", "Java", "Dados", "DevOps",
                                            "IA", "Cibersegurança", "Infraestrutura", "Mobile",
                                            "Games", "UI/UX"};

    private static final String[] NIVEIS = {"Básico", "Intermediário", "Avançado"};

    private static final String[] MODALIDADES = {"Online", "Presencial", "Híbrido"};

    public CursoSeedService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    @Override
    public void run(String... args) {
        if (cursoRepository.count() > 0) {
            log.info("CursoSeedService: Banco já possui cursos, pulando seed.");
            return;
        }

        try {
            log.info("CursoSeedService: Iniciando geração de cursos...");
            Map<String, Integer> clusterUsuarios = lerDadosVisent();

            List<Curso> cursos = new ArrayList<>();

            // 1. Cursos gratuitos de instituições beneficentes (PRIORIDADE MÁXIMA)
            for (String[] template : CURSO_TEMPLATES) {
                boolean gratuito = Boolean.parseBoolean(template[5]);
                if (!gratuito) continue;

                String inst = INSTITUICOES_GRATUITAS[
                    ThreadLocalRandom.current().nextInt(INSTITUICOES_GRATUITAS.length)];
                String regiao = escolherRegiao(clusterUsuarios);

                String beneficente = inst.contains("ONG") || inst.contains("Comunitária") ||
                                     inst.contains("Social") || inst.contains("Casa de Apoio") ||
                                     inst.contains("Fundação") || inst.contains("Associação")
                                     ? "Instituição Beneficente" : null;

                String vagasNum = String.valueOf(ThreadLocalRandom.current().nextInt(15, 120));
                String desc = gerarDescricao(template[0], inst, template[2], template[3], template[4], true);

                cursos.add(new Curso(
                    template[0], inst, regiao, desc, template[1],
                    template[2], template[3], template[4],
                    true, true, vagasNum, "#", beneficente
                ));
            }

            // 2. Cursos pagos complementares
            for (String[] template : CURSO_TEMPLATES) {
                boolean gratuito = Boolean.parseBoolean(template[5]);
                if (gratuito) continue;

                String inst = INSTITUICOES_PAGAS[
                    ThreadLocalRandom.current().nextInt(INSTITUICOES_PAGAS.length)];
                String regiao = escolherRegiao(clusterUsuarios);

                String vagasNum = String.valueOf(ThreadLocalRandom.current().nextInt(20, 200));
                String desc = gerarDescricao(template[0], inst, template[2], template[3], template[4], false);

                cursos.add(new Curso(
                    template[0], inst, regiao, desc, template[1],
                    template[2], template[3], template[4],
                    false, true, vagasNum, "#", null
                ));
            }

            // 3. Cursos adicionais por região (baseados na concentração VISent)
            for (Map.Entry<String, Integer> entry : clusterUsuarios.entrySet()) {
                String cluster = entry.getKey();
                int usuarios = entry.getValue();

                if (usuarios > 1000) {
                    String inst = INSTITUICOES_GRATUITAS[
                        ThreadLocalRandom.current().nextInt(INSTITUICOES_GRATUITAS.length)];
                    String titulo = "Capacitação Tech — " + cluster.replace("_", " ");
                    String desc = "Programa intensivo de capacitação em tecnologia para moradores da região de " +
                                  cluster.replace("_", " ") + ". Aulas presenciais com equipamentos fornecidos.";
                    String vagasNum = String.valueOf(ThreadLocalRandom.current().nextInt(20, 60));

                    cursos.add(new Curso(
                        titulo, inst, cluster, desc,
                        AREAS[ThreadLocalRandom.current().nextInt(AREAS.length)],
                        "Básico", "120h", "Presencial",
                        true, true, vagasNum, "#", "Programa Social"
                    ));
                }
            }

            cursoRepository.saveAll(cursos);
            log.info("CursoSeedService: {} cursos gerados com sucesso!", cursos.size());

        } catch (Exception e) {
            log.error("CursoSeedService: Erro ao gerar cursos: ", e);
            gerarCursosFallback();
        }
    }

    private void gerarCursosFallback() {
        List<Curso> cursos = new ArrayList<>();
        String[] regioes = {"CBD_BEIRAMAR", "TRINDADE", "UFSC", "CAMPECHE", "INGLESES",
                            "SAO_JOSE_CENTRO", "ESTREITO_CAPOEIRAS", "LAGOA_CONCEICAO"};

        for (String[] template : CURSO_TEMPLATES) {
            boolean gratuito = Boolean.parseBoolean(template[5]);
            String inst = gratuito
                ? INSTITUICOES_GRATUITAS[ThreadLocalRandom.current().nextInt(INSTITUICOES_GRATUITAS.length)]
                : INSTITUICOES_PAGAS[ThreadLocalRandom.current().nextInt(INSTITUICOES_PAGAS.length)];
            String regiao = regioes[ThreadLocalRandom.current().nextInt(regioes.length)];
            String vagasNum = String.valueOf(ThreadLocalRandom.current().nextInt(15, 100));
            String desc = gerarDescricao(template[0], inst, template[2], template[3], template[4], gratuito);

            String beneficente = null;
            if (gratuito && (inst.contains("ONG") || inst.contains("Social") || inst.contains("Fundação"))) {
                beneficente = "Instituição Beneficente";
            }

            cursos.add(new Curso(
                template[0], inst, regiao, desc, template[1],
                template[2], template[3], template[4],
                gratuito, true, vagasNum, "#", beneficente
            ));
        }

        cursoRepository.saveAll(cursos);
        log.info("CursoSeedService: {} cursos fallback gerados.", cursos.size());
    }

    private Map<String, Integer> lerDadosVisent() {
        Map<String, Integer> clusters = new LinkedHashMap<>();
        try {
            ClassPathResource resource = new ClassPathResource("data/assinantes.csv");
            if (!resource.exists()) return gerarClustersDefault();

            try (InputStream is = resource.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

                String header = reader.readLine();
                if (header == null) return gerarClustersDefault();

                String sep = header.contains(";") ? ";" : ",";
                String[] h = header.split(sep);
                int idxCluster = findCol(h, "home_cluster", "cluster");

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] v = line.split(sep);
                    String cluster = getVal(v, idxCluster);
                    if (cluster != null && !cluster.isEmpty()) {
                        clusters.merge(cluster, 1, Integer::sum);
                    }
                }
            }
        } catch (Exception e) {
            log.error("CursoSeedService: Erro ao ler VISent: ", e);
            return gerarClustersDefault();
        }
        return clusters.isEmpty() ? gerarClustersDefault() : clusters;
    }

    private Map<String, Integer> gerarClustersDefault() {
        Map<String, Integer> clusters = new LinkedHashMap<>();
        String[] nomes = {"CBD_BEIRAMAR", "TRINDADE", "UFSC", "CAMPECHE", "INGLESES",
                          "SAO_JOSE_CENTRO", "ESTREITO_CAPOEIRAS", "LAGOA_CONCEICAO"};
        for (String n : nomes) clusters.put(n, ThreadLocalRandom.current().nextInt(500, 3000));
        return clusters;
    }

    private String escolherRegiao(Map<String, Integer> clusters) {
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(clusters.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());
        int top = Math.min(5, sorted.size());
        return sorted.get(ThreadLocalRandom.current().nextInt(top)).getKey();
    }

    private String gerarDescricao(String titulo, String inst, String nivel, String duracao,
                                   String modalidade, boolean gratuito) {
        StringBuilder sb = new StringBuilder();
        sb.append("Curso de ").append(titulo).append(" oferecido por ").append(inst).append(". ");
        sb.append("Nível ").append(nivel).append(" com duração de ").append(duracao).append(". ");
        sb.append("Modalidade ").append(modalidade).append(". ");
        if (gratuito) {
            sb.append("Curso 100% gratuito com certificado de conclusão. ");
            sb.append("Vagas limitadas — inscreva-se agora!");
        } else {
            sb.append("Investimento acessível com opção de bolsa. ");
            sb.append("Certificado reconhecido pelo MEC.");
        }
        return sb.toString();
    }

    private int findCol(String[] headers, String... candidates) {
        for (int i = 0; i < headers.length; i++) {
            String h = headers[i].trim().toLowerCase();
            for (String c : candidates) {
                if (h.contains(c.toLowerCase())) return i;
            }
        }
        return -1;
    }

    private String getVal(String[] values, int idx) {
        if (idx >= 0 && idx < values.length) {
            return values[idx].trim().replaceAll("^\"|\"$", "");
        }
        return null;
    }
}
