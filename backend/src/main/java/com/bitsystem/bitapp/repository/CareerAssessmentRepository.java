package com.bitsystem.bitapp.repository;

import com.bitsystem.bitapp.domain.CareerAssessment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareerAssessmentRepository extends JpaRepository<CareerAssessment, Long> {
    List<CareerAssessment> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);
}
