package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.domain.Curso;
import com.bitsystem.bitapp.repository.CursoRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

/**
 * Seed de cursos com programas REAIS (Oracle Next Education, Alura, Santander
 * Open Academy, Bootcamps DIO, Google Cloud Skills Boost, Escola Virtual
 * Fundação Bradesco) — cada um desdobrado em trilhas reais dessas instituições.
 *
 * As trilhas online são programas nacionais: regiao = "Nacional (EAD)".
 * O CursoRepository trata esse valor como coringa nos filtros por região.
 *
 * Também inclui cursos presenciais reais em Florianópolis (Floripa Mais Tec,
 * IFSC, Senac, SENAI, Go Up, ACATE, Católica SC): regiao = "Florianópolis".
 */
@Service
public class CursoSeedService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CursoSeedService.class);

    private static final String REGIAO_NACIONAL = "Nacional (EAD)";

    private final CursoRepository cursoRepository;

    private record Trilha(
        String titulo, String instituicao, String descricao, String area,
        String nivel, String duracao, boolean gratuito, String link
    ) {}

    private static final List<Trilha> TRILHAS = List.of(
        new Trilha(
            "Oracle Next Education — Formação Back-End (Java)", "Oracle & Alura",
            "Programa educacional gratuito da Oracle em parceria com a Alura, com formação técnica em " +
            "desenvolvimento back-end Java e preparação para o mercado de trabalho.",
            "Java", "Básico", "15 meses", true,
            "https://www.oracle.com/br/education/oracle-next-education/"
        ),
        new Trilha(
            "Oracle Next Education — Formação Front-End", "Oracle & Alura",
            "Programa educacional gratuito da Oracle em parceria com a Alura, com formação técnica em " +
            "desenvolvimento front-end e preparação para o mercado de trabalho.",
            "Web", "Básico", "15 meses", true,
            "https://www.oracle.com/br/education/oracle-next-education/"
        ),
        new Trilha(
            "Formação Java", "Alura",
            "Trilha completa de back-end com Java e Spring Boot, da plataforma Alura, cobrindo desde " +
            "fundamentos da linguagem até arquitetura de APIs REST.",
            "Java", "Intermediário", "80h", false,
            "https://www.alura.com.br/"
        ),
        new Trilha(
            "Formação Front-end", "Alura",
            "Trilha de desenvolvimento front-end da Alura, cobrindo HTML, CSS, JavaScript moderno e " +
            "frameworks de interface.",
            "Web", "Intermediário", "80h", false,
            "https://www.alura.com.br/"
        ),
        new Trilha(
            "Formação Dados", "Alura",
            "Trilha de dados da Alura, cobrindo análise, visualização e ciência de dados com Python e SQL.",
            "Dados", "Intermediário", "80h", false,
            "https://www.alura.com.br/"
        ),
        new Trilha(
            "Santander Open Academy — Trilha de Programação", "Santander",
            "Programa gratuito do Santander Open Academy com trilha de programação voltada à empregabilidade " +
            "em tecnologia.",
            "Java", "Básico", "40h", true,
            "https://www.santanderopenacademy.com/pt_br/index.html"
        ),
        new Trilha(
            "Santander Open Academy — Trilha de Dados", "Santander",
            "Programa gratuito do Santander Open Academy com trilha de dados voltada à empregabilidade " +
            "em tecnologia.",
            "Dados", "Básico", "40h", true,
            "https://www.santanderopenacademy.com/pt_br/index.html"
        ),
        new Trilha(
            "Bootcamp DIO — Java Developer", "DIO",
            "Bootcamp gratuito da DIO (Digital Innovation One) com trilha prática de desenvolvimento Java, " +
            "incluindo desafios de código e mentoria da comunidade.",
            "Java", "Intermediário", "60h", true,
            "https://www.dio.me/"
        ),
        new Trilha(
            "Bootcamp DIO — Front-end Developer", "DIO",
            "Bootcamp gratuito da DIO com trilha prática de desenvolvimento front-end, incluindo desafios " +
            "de código e mentoria da comunidade.",
            "Web", "Intermediário", "60h", true,
            "https://www.dio.me/"
        ),
        new Trilha(
            "Bootcamp DIO — Cloud (AWS/Azure)", "DIO",
            "Bootcamp gratuito da DIO com trilha prática de computação em nuvem, cobrindo fundamentos de " +
            "AWS e Azure.",
            "Infraestrutura", "Intermediário", "60h", true,
            "https://www.dio.me/"
        ),
        new Trilha(
            "Bootcamp DIO — Data Science", "DIO",
            "Bootcamp gratuito da DIO com trilha prática de ciência de dados, incluindo desafios de código " +
            "e mentoria da comunidade.",
            "Dados", "Intermediário", "60h", true,
            "https://www.dio.me/"
        ),
        new Trilha(
            "Google Cloud Skills Boost — Cloud Engineer Learning Path", "Google Cloud",
            "Trilha oficial do Google Cloud Skills Boost para formação de engenheiros de nuvem, com " +
            "laboratórios práticos na infraestrutura real do Google Cloud.",
            "Infraestrutura", "Intermediário", "Autoinstrucional", true,
            "https://cloud.google.com/learn/training"
        ),
        new Trilha(
            "Google Cloud Skills Boost — Data Analytics Learning Path", "Google Cloud",
            "Trilha oficial do Google Cloud Skills Boost para análise de dados na nuvem, com laboratórios " +
            "práticos na infraestrutura real do Google Cloud.",
            "Dados", "Intermediário", "Autoinstrucional", true,
            "https://www.skills.google/paths/18"
        ),
        new Trilha(
            "Google Cloud Skills Boost — Programa GEAR (Iniciantes)", "Google Cloud",
            "Programa GEAR do Google Cloud, com trilha introdutória gratuita para quem está começando em " +
            "computação em nuvem.",
            "Infraestrutura", "Básico", "Autoinstrucional", true,
            "https://cloud.google.com/learn/training"
        ),
        new Trilha(
            "Escola Virtual — Lógica de Programação", "Fundação Bradesco",
            "Curso gratuito da Escola Virtual da Fundação Bradesco com introdução à lógica de programação.",
            "Java", "Básico", "30h", true,
            "https://www.ev.org.br/"
        ),
        new Trilha(
            "Escola Virtual — Introdução ao Desenvolvimento Web", "Fundação Bradesco",
            "Curso gratuito da Escola Virtual da Fundação Bradesco com introdução ao desenvolvimento web.",
            "Web", "Básico", "30h", true,
            "https://www.ev.org.br/"
        ),
        new Trilha(
            "Escola Virtual — Introdução à Ciência de Dados", "Fundação Bradesco",
            "Curso gratuito da Escola Virtual da Fundação Bradesco com introdução à ciência de dados.",
            "Dados", "Básico", "30h", true,
            "https://www.ev.org.br/"
        )
    );

    private record CursoPresencial(
        String titulo, String instituicao, String endereco, String area,
        String nivel, boolean gratuito, String link, String beneficente
    ) {}

    private static final String REGIAO_FLORIANOPOLIS = "Florianópolis";

    private static final List<CursoPresencial> PRESENCIAIS = List.of(
        new CursoPresencial(
            "Floripa Mais Tec", "Prefeitura de Florianópolis, SENAI e ACATE",
            "SENAI Itacorubi, Rod. Admar Gonzaga 2765, Itacorubi",
            "Programação", "Básico", true,
            "https://floripamaistec.pmf.sc.gov.br", null
        ),
        new CursoPresencial(
            "Análise e Desenvolvimento de Sistemas e Gestão da TI", "IFSC",
            "Rua 14 de Julho 150, Coqueiros",
            "Programação", "Intermediário", true,
            "https://www.ifsc.edu.br", null
        ),
        new CursoPresencial(
            "Técnico em Informática / Desenvolvimento de Sistemas", "Senac PSG",
            "Rua Silva Jardim 360, Prainha",
            "Programação", "Básico", true,
            "https://www.sc.senac.br", "Renda per capita familiar de até 2 salários mínimos"
        ),
        new CursoPresencial(
            "Programação, IA e Design", "Go Up Treinamentos",
            "Rua Tenente Silveira 209, Centro",
            "Programação", "Intermediário", false,
            "https://gouptreinamentos.com", null
        ),
        new CursoPresencial(
            "Técnicos e graduações em TI", "SENAI SC",
            "Rod. Admar Gonzaga 2765, Itacorubi",
            "Programação", "Intermediário", false,
            "https://sc.senai.br", null
        ),
        new CursoPresencial(
            "Workshops e treinamentos em tecnologia", "ACATE",
            "SC 401, Km 4, Saco Grande",
            "Programação", "Básico", false,
            "https://acate.com.br", null
        ),
        new CursoPresencial(
            "Gestão da Tecnologia da Informação", "Católica SC",
            "Rua Felipe Schmidt 835, Centro",
            "Programação", "Avançado", false,
            "https://catolicasc.org.br", null
        )
    );

    public CursoSeedService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    @Override
    public void run(String... args) {
        if (cursoRepository.count() > 0) {
            log.info("CursoSeedService: Banco já possui cursos, pulando seed.");
            return;
        }

        log.info("CursoSeedService: Iniciando geração de cursos...");

        List<Curso> cursos = new ArrayList<>();
        for (Trilha t : TRILHAS) {
            String vagas = String.valueOf(ThreadLocalRandom.current().nextInt(20, 150));
            cursos.add(new Curso(
                t.titulo(), t.instituicao(), REGIAO_NACIONAL, t.descricao(),
                t.area(), t.nivel(), t.duracao(), "Online",
                t.gratuito(), true, vagas, t.link(), null
            ));
        }
        for (CursoPresencial p : PRESENCIAIS) {
            cursos.add(new Curso(
                p.titulo(), p.instituicao(), REGIAO_FLORIANOPOLIS,
                "Curso presencial em Florianópolis. Endereço: " + p.endereco() + ".",
                p.area(), p.nivel(), "Sob consulta", "Presencial",
                p.gratuito(), true, "Sob consulta", p.link(), p.beneficente()
            ));
        }

        cursoRepository.saveAll(cursos);
        log.info("CursoSeedService: {} cursos gerados com sucesso!", cursos.size());
    }
}
