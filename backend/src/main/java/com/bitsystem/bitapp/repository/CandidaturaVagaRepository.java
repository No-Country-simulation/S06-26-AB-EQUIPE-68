package com.bitsystem.bitapp.repository;

import com.bitsystem.bitapp.domain.CandidaturaVaga;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidaturaVagaRepository extends JpaRepository<CandidaturaVaga, Long> {

    boolean existsByUsuarioIdAndVagaId(Long usuarioId, Long vagaId);

    List<CandidaturaVaga> findByUsuarioIdOrderByDataEnvioDesc(Long usuarioId);
}