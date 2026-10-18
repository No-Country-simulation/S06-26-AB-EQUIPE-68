package com.bitsystem.bitapp.repository;

import com.bitsystem.bitapp.domain.CandidaturaVaga;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CandidaturaVagaRepository extends JpaRepository<CandidaturaVaga, Long> {

    boolean existsByUsuarioIdAndVagaId(Long usuarioId, Long vagaId);

    List<CandidaturaVaga> findByUsuarioIdOrderByDataEnvioDesc(Long usuarioId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM CandidaturaVaga e WHERE e.usuarioId IN :ids")
    int apagarPorUsuarios(@Param("ids") Collection<Long> ids);
}