package com.bitsystem.bitapp.repository;

import com.bitsystem.bitapp.domain.InscricaoCurso;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InscricaoCursoRepository extends JpaRepository<InscricaoCurso, Long> {

    boolean existsByUsuarioIdAndCursoId(Long usuarioId, Long cursoId);

    Optional<InscricaoCurso> findByUsuarioIdAndCursoId(Long usuarioId, Long cursoId);

    List<InscricaoCurso> findByUsuarioIdOrderByDataInscricaoDesc(Long usuarioId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM InscricaoCurso e WHERE e.usuarioId IN :ids")
    int apagarPorUsuarios(@Param("ids") Collection<Long> ids);
}