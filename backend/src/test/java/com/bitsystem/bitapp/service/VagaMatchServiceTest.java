package com.bitsystem.bitapp.service;

import static org.junit.jupiter.api.Assertions.*;

import com.bitsystem.bitapp.dto.VagaMatchDto;
import java.util.List;
import org.junit.jupiter.api.Test;

class VagaMatchServiceTest {

    // calcular() é puro (não toca repositórios), então os repos podem ser null nos testes.
    private final VagaMatchService service = new VagaMatchService(null, null);

    @Test
    void matchTotalDevolve100() {
        VagaMatchDto.Resultado r = service.calcular("Java, Spring Boot, SQL", "Java, Spring Boot, SQL");
        assertEquals(100, r.matchPercentual());
        assertEquals(3, r.skillsAtendidas().size());
        assertTrue(r.skillsFaltantes().isEmpty());
    }

    @Test
    void matchParcialTresDeQuatroDevolve75() {
        VagaMatchDto.Resultado r = service.calcular("Java, Spring Boot, SQL", "Java, Spring, SQL, Kafka");
        assertEquals(75, r.matchPercentual());
        assertEquals(3, r.skillsAtendidas().size());
        assertEquals(List.of("Kafka"), r.skillsFaltantes());
    }

    @Test
    void zeroIntersecaoDevolveZero() {
        VagaMatchDto.Resultado r = service.calcular("Photoshop, Illustrator", "Java, Kafka");
        assertEquals(0, r.matchPercentual());
        assertTrue(r.skillsAtendidas().isEmpty());
        assertEquals(2, r.skillsFaltantes().size());
    }

    @Test
    void sinonimoContaComoAtendida() {
        VagaMatchDto.Resultado r = service.calcular("Spring Boot", "Spring");
        assertEquals(100, r.matchPercentual());
        assertEquals(List.of("Spring"), r.skillsAtendidas());
        assertTrue(r.skillsFaltantes().isEmpty());
    }

    @Test
    void caseEAcentoNaoAfetamComparacao() {
        VagaMatchDto.Resultado r = service.calcular("java, sql", "JAVA, SQL");
        assertEquals(100, r.matchPercentual());
    }

    @Test
    void vagaSemTecnologiasDevolveMatchNull() {
        VagaMatchDto.Resultado r = service.calcular("Java, Spring Boot, SQL", "");
        assertNull(r.matchPercentual());
        assertTrue(r.skillsAtendidas().isEmpty());
        assertTrue(r.skillsFaltantes().isEmpty());
    }

    @Test
    void vagaComTecnologiasNulasDevolveMatchNull() {
        VagaMatchDto.Resultado r = service.calcular("Java, Spring Boot, SQL", null);
        assertNull(r.matchPercentual());
    }

    @Test
    void usuarioSemSkillsDevolveZeroComTodasFaltantes() {
        VagaMatchDto.Resultado r = service.calcular(null, "Java, Microservices, Kafka");
        assertEquals(0, r.matchPercentual());
        assertTrue(r.skillsAtendidas().isEmpty());
        assertEquals(3, r.skillsFaltantes().size());
    }

    @Test
    void textoForaDoParenteseFazMatchComTermoCurto() {
        VagaMatchDto.Resultado r = service.calcular("Java SE (Core Java)", "Java");
        assertEquals(100, r.matchPercentual());
        assertEquals(List.of("Java"), r.skillsAtendidas());
    }

    @Test
    void conteudoEntreParentesesComBarraGeraTokensSeparados() {
        VagaMatchDto.Resultado r = service.calcular(
                "Banco de Dados Relacionais (PostgreSQL/MySQL)", "PostgreSQL, MySQL");
        assertEquals(100, r.matchPercentual());
        assertEquals(2, r.skillsAtendidas().size());
        assertTrue(r.skillsAtendidas().containsAll(List.of("PostgreSQL", "MySQL")));
    }

    @Test
    void sinonimoContinuaFuncionandoComSufixoEntreParenteses() {
        VagaMatchDto.Resultado r = service.calcular("Spring Boot (e Spring Framework)", "Spring Boot");
        assertEquals(100, r.matchPercentual());
        assertEquals(List.of("Spring Boot"), r.skillsAtendidas());
    }

    @Test
    void javaNaoFazMatchComJavascript() {
        VagaMatchDto.Resultado r = service.calcular("Java", "JavaScript");
        assertEquals(0, r.matchPercentual());
        assertTrue(r.skillsAtendidas().isEmpty());
        assertEquals(List.of("JavaScript"), r.skillsFaltantes());
    }

    @Test
    void perfilCompletoDeExemploFazMatchComVagaJavaJpaPostgresql() {
        String competencias = "Java SE (Core Java), Spring Boot (e Spring Framework), "
                + "Banco de Dados Relacionais (PostgreSQL/MySQL), Python (Django/Flask)";
        VagaMatchDto.Resultado r = service.calcular(competencias, "Java, JPA/Hibernate, PostgreSQL");
        assertNotNull(r.matchPercentual());
        assertTrue(r.matchPercentual() > 0);
        assertTrue(r.skillsAtendidas().containsAll(List.of("Java", "PostgreSQL")));
    }
}
