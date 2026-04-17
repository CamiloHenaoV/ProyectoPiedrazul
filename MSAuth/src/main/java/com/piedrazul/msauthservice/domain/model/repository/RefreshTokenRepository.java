package com.piedrazul.msauthservice.domain.model.repository;

import com.piedrazul.msauthservice.domain.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    /** Revoca todos los refresh tokens activos de un usuario (logout-all) */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revocado = true WHERE r.usuarioId = :usuarioId AND r.revocado = false")
    void revocarTodosPorUsuarioId(Long usuarioId);
}
