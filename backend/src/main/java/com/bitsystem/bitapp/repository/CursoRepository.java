package com.bitsystem.bitapp.repository;

import com.bitsystem.bitapp.domain.Curso;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    List<Curso> findByAtivaTrue();

    @Query("SELECT c FROM Curso c WHERE c.ativa = true AND " +
           "(:q IS NULL OR LOWER(c.titulo) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(c.instituicao) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(c.area) LIKE LOWER(CONCAT('%', :q, '%'))) AND " +
           "(:regiao IS NULL OR c.regiao = :regiao) AND " +
           "(:area IS NULL OR c.area = :area) AND " +
           "(:modalidade IS NULL OR c.modalidade = :modalidade) AND " +
           "(:gratuito IS NULL OR c.gratuito = :gratuito) AND " +
           "(:nivel IS NULL OR c.nivel = :nivel)")
    List<Curso> buscarComFiltros(
        @Param("q") String q,
        @Param("regiao") String regiao,
        @Param("area") String area,
        @Param("modalidade") String modalidade,
        @Param("gratuito") Boolean gratuito,
        @Param("nivel") String nivel
    );

    @Query("SELECT c FROM Curso c WHERE c.ativa = true AND c.gratuito = true ORDER BY c.instituicao")
    List<Curso> findGratuitos();

    @Query("SELECT c FROM Curso c WHERE c.ativa = true AND c.beneficente IS NOT NULL AND c.beneficente <> '' ORDER BY c.instituicao")
    List<Curso> findBeneficentes();
}
