package com.bitsystem.bitapp.repository;

import com.bitsystem.bitapp.domain.Mentor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorRepository extends JpaRepository<Mentor, Long> {

    List<Mentor> findByAtivoTrue();
}
