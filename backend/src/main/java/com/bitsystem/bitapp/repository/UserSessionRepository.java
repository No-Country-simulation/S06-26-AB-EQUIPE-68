package com.bitsystem.bitapp.repository;

import com.bitsystem.bitapp.domain.UserSession;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByToken(String token);
    Optional<UserSession> findByRefreshToken(String refreshToken);
    List<UserSession> findByUsuarioId(Long usuarioId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByUsuarioId(Long usuarioId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM UserSession e WHERE e.usuarioId IN :ids")
    int apagarPorUsuarios(@Param("ids") Collection<Long> ids);
}
