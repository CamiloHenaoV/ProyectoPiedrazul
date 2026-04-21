package com.piedrazul.msscheduling.domain.model.repository;

import com.piedrazul.msscheduling.domain.model.entity.Cita;
import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByPacienteId(Long pacienteId);
    List<Cita> findByProfesionalId(Long profesionalId);
    List<Cita> findByPacienteIdAndEstado(Long pacienteId, EstadoCita estado);
    List<Cita> findByProfesionalIdAndFechaHoraBetween(Long profesionalId, ZonedDateTime inicio, ZonedDateTime fin);
    boolean existsByProfesionalIdAndFechaHora(Long profesionalId, ZonedDateTime fechaHora);
}
