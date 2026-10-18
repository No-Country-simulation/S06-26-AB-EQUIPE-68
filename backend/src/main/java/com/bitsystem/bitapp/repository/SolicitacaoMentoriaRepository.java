package com.bitsystem.bitapp.repository;

import com.bitsystem.bitapp.domain.SolicitacaoMentoria;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SolicitacaoMentoriaRepository extends JpaRepository<SolicitacaoMentoria, Long> {

    List<SolicitacaoMentoria> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SolicitacaoMentoria e WHERE e.usuarioId IN :ids")
    int apagarPorUsuarios(@Param("ids") Collection<Long> ids);
}
