package com.bitsystem.bitapp.repository;

import com.bitsystem.bitapp.model.HistoricoSaude;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HistoricoSaudeRepository extends JpaRepository<HistoricoSaude, Long> {
    List<HistoricoSaude> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM HistoricoSaude e WHERE e.userId IN :ids")
    int apagarPorUsuarios(@Param("ids") Collection<Long> ids);
}
