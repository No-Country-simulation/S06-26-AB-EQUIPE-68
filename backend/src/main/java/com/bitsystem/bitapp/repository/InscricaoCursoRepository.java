package com.bitsystem.bitapp.repository;

import com.bitsystem.bitapp.domain.InscricaoCurso;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InscricaoCursoRepository extends JpaRepository<InscricaoCurso, Long> {

    boolean existsByUsuarioIdAndCursoId(Long usuarioId, Long cursoId);

    Optional<InscricaoCurso> findByUsuarioIdAndCursoId(Long usuarioId, Long cursoId);

    List<InscricaoCurso> findByUsuarioIdOrderByDataInscricaoDesc(Long usuarioId);
}