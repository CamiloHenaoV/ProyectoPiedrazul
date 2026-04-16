package com.piedrazul.msusermanagement.domain.model.repository;


import com.piedrazul.msusermanagement.domain.model.entity.Usuario;
import com.piedrazul.msusermanagement.domain.model.entity.enums.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByLogin(String login);
    List<Usuario>     findByRol(RolUsuario rol);
    List<Usuario>     findByActivoTrue();
    boolean           existsByLogin(String login);
    long countByActivoTrue();
}