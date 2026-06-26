package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.domain.Vaga;
import com.bitsystem.bitapp.repository.VagaRepository;
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
public class VagaSeedService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(VagaSeedService.class);

    private final VagaRepository vagaRepository;

    private static final String[] EMPRESAS = {
        "TechHub Florianópolis", "InnoTech SC", "DigitalWave", "CloudBit Systems",
        "DataFlow Analytics", "SmartCamp Solutions", "NexGen Software", "ByteForce",
        "CodeNest Florianópolis", "PixelCraft Studios", "Futuro Digital",
        "RedeTech Sul", "AgilePoint BR", "SoftServe SC", "Engenharia Digital"
    };

    private static final String[] TITULOS_JAVA = {
        "Desenvolvedor Java Júnior", "Desenvolvedor Java Pleno",
        "Engenheiro de Software Java", "Backend Developer",
        "Desenvolvedor Spring Boot", "Arquiteto de Software Java"
    };

    private static final String[] TITULOS_WEB = {
        "Desenvolvedor Front-End", "Desenvolvedor React",
        "UI/UX Developer", "Desenvolvedor Full Stack",
        "Desenvolvedor JavaScript", "Web Developer Pleno"
    };

    private static final String[] TITULOS_DADOS = {
        "Analista de Dados", "Engenheiro de Dados",
        "Cientista de Dados Júnior", "BI Developer",
        "Data Analyst", "Especialista em Big Data"
    };

    private static final String[] TITULOS_INFRA = {
        "Analista de Infraestrutura", "DevOps Engineer",
        "Cloud Engineer", "Especialista em Linux",
        "Administrador de Bancos de Dados", "SRE Engineer"
    };

    private static final String[] TECNOLOGIAS_JAVA = {
        "Java, Spring Boot, MySQL", "Java, JPA/Hibernate, PostgreSQL",
        "Java, Maven, REST API", "Java, Spring Cloud, Docker",
        "Java, Microservices, Kafka"
    };

    private static final String[] TECNOLOGIAS_WEB = {
        "JavaScript, React, CSS", "TypeScript, Next.js, Tailwind",
        "HTML, CSS, JavaScript, Vue.js", "React, Node.js, MongoDB",
        "Angular, TypeScript, RxJS"
    };

    private static final String[] TECNOLOGIAS_DADOS = {
        "Python, SQL, Power BI", "SQL, Excel, Tableau",
        "Python, Pandas, Jupyter", "Spark, Hadoop, Hive",
        "SQL, dbt, Looker"
    };

    private static final String[] TECNOLOGIAS_INFRA = {
        "Linux, Docker, AWS", "Kubernetes, Terraform, GCP",
        "Ansible, Jenkins, CI/CD", "MySQL, PostgreSQL, Redis",
        "Azure, Terraform, Bash"
    };

    private static final String[] NIVEIS = {"Estágio", "Júnior", "Pleno"};

    private static final String[] TIPOS_CONTRATO = {"CLT", "PJ", "Estágio", "Freelancer"};

    private static final String[] TURNOS = {"Manhã", "Tarde", "Noite", "Flexível"};

    public VagaSeedService(VagaRepository vagaRepository) {
        this.vagaRepository = vagaRepository;
    }

    @Override
    public void run(String... args) {
        if (vagaRepository.count() > 0) {
            log.info("VagaSeedService: Banco já possui vagas, pulando seed.");
            return;
        }

        try {
            log.info("VagaSeedService: Iniciando geração de vagas a partir dos dados VISent...");

            Map<String, ClusterInfo> clusters = lerDadosVisent();

            List<Vaga> vagas = new ArrayList<>();
            int idCounter = 1;

            for (Map.Entry<String, ClusterInfo> entry : clusters.entrySet()) {
                String cluster = entry.getKey();
                ClusterInfo info = entry.getValue();

                int numVagas = Math.max(2, Math.min(5, info.usuarios / 200));
                for (int i = 0; i < numVagas; i++) {
                    String area = escolherArea(info);
                    String nivel = escolherNivel(info);
                    String[] dados = gerarDadosVaga(area, nivel);

                    String remoto = info.distanciaMediaKm > 15 ? "Híbrido" :
                                    info.distanciaMediaKm > 8 ? "Presencial" : "Flexível";
                    if (ThreadLocalRandom.current().nextDouble() < 0.2) remoto = "Remoto";

                    Vaga vaga = new Vaga(
                        dados[0],
                        EMPRESAS[ThreadLocalRandom.current().nextInt(EMPRESAS.length)],
                        cluster,
                        dados[1],
                        nivel,
                        area,
                        TIPOS_CONTRATO[ThreadLocalRandom.current().nextInt(TIPOS_CONTRATO.length)],
                        gerarSalario(nivel),
                        dados[2],
                        "#",
                        remoto
                    );
                    vagas.add(vaga);
                    idCounter++;
                }
            }

            vagaRepository.saveAll(vagas);
            log.info("VagaSeedService: {} vagas geradas com sucesso!", vagas.size());

        } catch (Exception e) {
            log.error("VagaSeedService: Erro ao gerar vagas: ", e);
            gerarVagasFallback();
        }
    }

    private void gerarVagasFallback() {
        List<Vaga> vagas = new ArrayList<>();
        String[] regioesFallback = {"CBD_BEIRAMAR", "TRINDADE", "UFSC", "CAMPECHE", "INGLESES",
                                     "SAO_JOSE_CENTRO", "ESTREITO_CAPOEIRAS", "LAGOA_CONCEICAO"};

        for (String regiao : regioesFallback) {
            for (int i = 0; i < 3; i++) {
                String area = new String[]{"Java", "Web", "Dados", "Infraestrutura"}[ThreadLocalRandom.current().nextInt(4)];
                String nivel = NIVEIS[ThreadLocalRandom.current().nextInt(NIVEIS.length)];
                String[] dados = gerarDadosVaga(area, nivel);

                vagas.add(new Vaga(
                    dados[0],
                    EMPRESAS[ThreadLocalRandom.current().nextInt(EMPRESAS.length)],
                    regiao,
                    dados[1],
                    nivel,
                    area,
                    TIPOS_CONTRATO[ThreadLocalRandom.current().nextInt(TIPOS_CONTRATO.length)],
                    gerarSalario(nivel),
                    dados[2],
                    "#",
                    "Híbrido"
                ));
            }
        }

        vagaRepository.saveAll(vagas);
        log.info("VagaSeedService: {} vagas fallback geradas.", vagas.size());
    }

    private Map<String, ClusterInfo> lerDadosVisent() {
        Map<String, ClusterInfo> clusters = new LinkedHashMap<>();

        try {
            ClassPathResource resource = new ClassPathResource("data/assinantes.csv");
            if (!resource.exists()) {
                log.warn("VagaSeedService: assinantes.csv não encontrado, usando clusters padrão.");
                return gerarClustersDefault();
            }

            Map<String, int[]> clusterFaixa = new HashMap<>();
            Map<String, Double> clusterDistancia = new HashMap<>();

            try (InputStream is = resource.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

                String header = reader.readLine();
                if (header == null) return gerarClustersDefault();

                String sep = header.contains(";") ? ";" : ",";
                String[] h = header.split(sep);

                int idxCluster = findCol(h, "home_cluster", "cluster");
                int idxIdade = findCol(h, "age_group", "faixa");
                int idxMobilidade = findCol(h, "mobility_pattern", "mobilidade");

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] v = line.split(sep);
                    String cluster = getVal(v, idxCluster);
                    if (cluster == null || cluster.isEmpty()) continue;

                    clusters.putIfAbsent(cluster, new ClusterInfo());
                    ClusterInfo ci = clusters.get(cluster);
                    ci.usuarios++;

                    String idade = getVal(v, idxIdade);
                    if (idade != null) {
                        ci.faixas.merge(idade, 1, Integer::sum);
                    }

                    String mobilidade = getVal(v, idxMobilidade);
                    if (mobilidade != null) {
                        ci.mobilidade = mobilidade;
                    }
                }
            }

            ClassPathResource trajResource = new ClassPathResource("data/trajetos_comuns.csv");
            if (trajResource.exists()) {
                try (InputStream is = trajResource.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

                    String header = reader.readLine();
                    if (header != null) {
                        String sep = header.contains(";") ? ";" : ",";
                        String[] h = header.split(sep);

                        int idxOrig = findCol(h, "origin_cluster", "origem");
                        int idxDist = findCol(h, "dist_media_km", "distancia");

                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.trim().isEmpty()) continue;
                            String[] v = line.split(sep);
                            String origem = getVal(v, idxOrig);
                            String distStr = getVal(v, idxDist);

                            if (origem != null && distStr != null && clusters.containsKey(origem)) {
                                try {
                                    double dist = Double.parseDouble(distStr.replace(",", "."));
                                    ClusterInfo ci = clusters.get(origem);
                                    ci.distanciaMediaKm = (ci.distanciaMediaKm + dist) / 2.0;
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("VagaSeedService: Erro ao ler VISent: ", e);
            return gerarClustersDefault();
        }

        return clusters;
    }

    private Map<String, ClusterInfo> gerarClustersDefault() {
        Map<String, ClusterInfo> clusters = new LinkedHashMap<>();
        String[] nomes = {"CBD_BEIRAMAR", "TRINDADE", "UFSC", "CAMPECHE", "INGLESES",
                          "SAO_JOSE_CENTRO", "ESTREITO_CAPOEIRAS", "LAGOA_CONCEICAO"};
        for (String n : nomes) {
            ClusterInfo ci = new ClusterInfo();
            ci.usuarios = ThreadLocalRandom.current().nextInt(500, 3000);
            ci.distanciaMediaKm = ThreadLocalRandom.current().nextDouble(3, 25);
            clusters.put(n, ci);
        }
        return clusters;
    }

    private String escolherArea(ClusterInfo info) {
        if (info.mobilidade != null && info.mobilidade.contains("INTENSA")) {
            return ThreadLocalRandom.current().nextDouble() < 0.4 ? "Java" : "Web";
        }
        String[] areas = {"Java", "Web", "Dados", "Infraestrutura"};
        return areas[ThreadLocalRandom.current().nextInt(areas.length)];
    }

    private String escolherNivel(ClusterInfo info) {
        if (info.faixas.containsKey("18-24") && info.faixas.get("18-24") > info.usuarios * 0.4) {
            return ThreadLocalRandom.current().nextDouble() < 0.6 ? "Estágio" : "Júnior";
        }
        if (info.faixas.containsKey("35-44") && info.faixas.get("35-44") > info.usuarios * 0.3) {
            return "Pleno";
        }
        return NIVEIS[ThreadLocalRandom.current().nextInt(NIVEIS.length)];
    }

    private String[] gerarDadosVaga(String area, String nivel) {
        String titulo, desc, techs;
        switch (area) {
            case "Java":
                titulo = TITULOS_JAVA[ThreadLocalRandom.current().nextInt(TITULOS_JAVA.length)];
                techs = TECNOLOGIAS_JAVA[ThreadLocalRandom.current().nextInt(TECNOLOGIAS_JAVA.length)];
                desc = "Vaga para desenvolvedor " + nivel + " na área de backend Java. " +
                       "Responsável pelo desenvolvimento e manutenção de APIs RESTful, " +
                       "integração com bancos de dados e trabalho em equipe ágil.";
                break;
            case "Web":
                titulo = TITULOS_WEB[ThreadLocalRandom.current().nextInt(TITULOS_WEB.length)];
                techs = TECNOLOGIAS_WEB[ThreadLocalRandom.current().nextInt(TECNOLOGIAS_WEB.length)];
                desc = "Vaga para desenvolvedor " + nivel + " front-end/full-stack. " +
                       "Responsável por interfaces responsivas, componentização e " +
                       "colaboração com designers UX/UI.";
                break;
            case "Dados":
                titulo = TITULOS_DADOS[ThreadLocalRandom.current().nextInt(TITULOS_DADOS.length)];
                techs = TECNOLOGIAS_DADOS[ThreadLocalRandom.current().nextInt(TECNOLOGIAS_DADOS.length)];
                desc = "Vaga para profissional de dados " + nivel + ". " +
                       "Responsável por análise, modelagem e visualização de dados, " +
                       "auxiliando na tomada de decisão baseada em evidências.";
                break;
            default:
                titulo = TITULOS_INFRA[ThreadLocalRandom.current().nextInt(TITULOS_INFRA.length)];
                techs = TECNOLOGIAS_INFRA[ThreadLocalRandom.current().nextInt(TECNOLOGIAS_INFRA.length)];
                desc = "Vaga para profissional de infraestrutura " + nivel + ". " +
                       "Responsável por ambientes cloud, automação, monitoramento e " +
                       "garantia de disponibilidade dos serviços.";
                break;
        }
        return new String[]{titulo, desc, techs};
    }

    private String gerarSalario(String nivel) {
        switch (nivel) {
            case "Estágio": return "R$ 1.200 - R$ 1.800";
            case "Júnior":  return "R$ 2.500 - R$ 4.500";
            case "Pleno":   return "R$ 5.000 - R$ 8.000";
            default:        return "R$ 3.000 - R$ 6.000";
        }
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

    private static class ClusterInfo {
        int usuarios = 0;
        Double distanciaMediaKm = 0.0;
        String mobilidade = "MODERADA";
        Map<String, Integer> faixas = new HashMap<>();
    }
}
