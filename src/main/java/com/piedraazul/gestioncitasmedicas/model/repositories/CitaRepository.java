package com.piedraazul.gestioncitasmedicas.model.repositories;

import com.piedraazul.gestioncitasmedicas.model.entities.Cita;
import com.piedraazul.gestioncitasmedicas.model.entities.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CitaRepository extends JpaRepository<Cita, UUID> {
    List<Cita> findByPacienteId(UUID pacienteId);
    List<Cita> findByProfesionalId(Integer profesionalId);
    List<Cita> findByProfesionalIdAndFechaHoraBetween(
            Integer profesionalId,
            ZonedDateTime inicio,
            ZonedDateTime fin
    );
    List<Cita> findByPacienteIdAndEstado(UUID pacienteId, EstadoCita estado);
    boolean    existsByProfesionalIdAndFechaHora(Integer profesionalId, ZonedDateTime fechaHora);
}