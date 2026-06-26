package com.bitsystem.bitapp.repository;

import com.bitsystem.bitapp.domain.Vaga;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VagaRepository extends JpaRepository<Vaga, Long> {

    List<Vaga> findByAtivaTrue();

    List<Vaga> findByRegiaoAndAtivaTrue(String regiao);

    List<Vaga> findByNivelAndAtivaTrue(String nivel);

    List<Vaga> findByAreaAndAtivaTrue(String area);

    @Query("SELECT v FROM Vaga v WHERE v.ativa = true AND " +
           "(LOWER(v.titulo) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(v.empresa) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(v.tecnologias) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Vaga> buscarPorTexto(@Param("q") String query);

    @Query("SELECT v FROM Vaga v WHERE v.ativa = true AND " +
           "(:q IS NULL OR LOWER(v.titulo) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(v.empresa) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(v.tecnologias) LIKE LOWER(CONCAT('%', :q, '%'))) AND " +
           "(:regiao IS NULL OR v.regiao = :regiao) AND " +
           "(:nivel IS NULL OR v.nivel = :nivel) AND " +
           "(:area IS NULL OR v.area = :area) AND " +
           "(:contrato IS NULL OR v.tipoContrato = :contrato)")
    List<Vaga> buscarComFiltros(
        @Param("q") String q,
        @Param("regiao") String regiao,
        @Param("nivel") String nivel,
        @Param("area") String area,
        @Param("contrato") String contrato
    );
}
