package com.bitsystem.bitapp.repository;

import com.bitsystem.bitapp.domain.MentalHealthRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentalHealthRecordRepository extends JpaRepository<MentalHealthRecord, Long> {
    List<MentalHealthRecord> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);
}
