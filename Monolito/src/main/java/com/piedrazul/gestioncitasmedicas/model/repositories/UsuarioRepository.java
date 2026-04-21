package com.piedrazul.gestioncitasmedicas.model.repositories;

import com.piedrazul.gestioncitasmedicas.model.entities.Usuario;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByLogin(String login);
    List<Usuario>     findByRol(RolUsuario rol);
    List<Usuario>     findByActivoTrue();
    boolean           existsByLogin(String login);
    long countByActivoTrue();
}