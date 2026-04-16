package com.piedrazul.msusermanagement.domain.model.repository;


import com.piedrazul.msusermanagement.domain.model.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByCedulaIdentidad(String cedula);
    Optional<Paciente> findByUsuarioId(Long usuarioId);
    List<Paciente>     findByNombreCompletoContainingIgnoreCase(String nombre);
    boolean            existsByCedulaIdentidad(String cedula);
}