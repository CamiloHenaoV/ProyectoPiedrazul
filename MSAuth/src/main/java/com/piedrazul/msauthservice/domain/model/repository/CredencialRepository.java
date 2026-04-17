package com.piedrazul.msauthservice.domain.model.repository;

import com.piedrazul.msauthservice.domain.model.entity.Credencial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CredencialRepository extends JpaRepository<Credencial, Long> {
    Optional<Credencial> findByLogin(String login);
    Optional<Credencial> findByUsuarioId(Long usuarioId);
    boolean existsByLogin(String login);
    boolean existsByUsuarioId(Long usuarioId);
}
