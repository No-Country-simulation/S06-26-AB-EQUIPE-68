package com.bitsystem.bitapp.repository;

import com.bitsystem.bitapp.domain.User;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    /** Contas fora da lista de e-mails dada (os visitantes) criadas antes do limite. */
    @Query("SELECT u.id FROM User u WHERE u.createdAt < :limite AND u.email NOT IN :emailsPreservados")
    List<Long> idsCriadosAntesDe(@Param("limite") LocalDateTime limite,
                                 @Param("emailsPreservados") Collection<String> emailsPreservados);
}
