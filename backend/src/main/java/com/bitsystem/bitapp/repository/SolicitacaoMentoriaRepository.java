package com.bitsystem.bitapp.repository;

import com.bitsystem.bitapp.domain.SolicitacaoMentoria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitacaoMentoriaRepository extends JpaRepository<SolicitacaoMentoria, Long> {

    List<SolicitacaoMentoria> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);
}
