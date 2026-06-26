package com.bitsystem.bitapp.repository;

import com.bitsystem.bitapp.model.HistoricoSaude;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoSaudeRepository extends JpaRepository<HistoricoSaude, Long> {
    List<HistoricoSaude> findByUser_IdOrderByCreatedAtDesc(Long userId);
}
