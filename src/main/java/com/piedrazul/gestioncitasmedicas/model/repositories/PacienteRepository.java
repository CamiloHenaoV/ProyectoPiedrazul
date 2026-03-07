package com.piedrazul.gestioncitasmedicas.model.repositories;

import com.piedrazul.gestioncitasmedicas.model.entities.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, UUID> {
    Optional<Paciente> findByCedulaIdentidad(String cedula);
    Optional<Paciente> findByUsuarioId(UUID usuarioId);
    List<Paciente>     findByNombreCompletoContainingIgnoreCase(String nombre);
    boolean            existsByCedulaIdentidad(String cedula);
}