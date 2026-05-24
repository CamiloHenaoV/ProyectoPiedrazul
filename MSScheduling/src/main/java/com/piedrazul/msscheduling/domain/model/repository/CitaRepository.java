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

    // -----------------------------------------------------------------------
    // Replaces the removed existsByProfesionalIdAndFechaHora.
    //
    // Returns only PROGRAMADA rows in the given window so the service can do
    // an accurate overlap check in Java.  Cancelled/completed rows are
    // excluded, which also fixes the "cancelled slot permanently blocked" bug.
    // The caller widens the window by the maximum possible slot duration so
    // that a long appointment starting just before the window cannot be missed.
    // -----------------------------------------------------------------------
    List<Cita> findByProfesionalIdAndEstadoAndFechaHoraBetween(
            Long profesionalId,
            EstadoCita estado,
            ZonedDateTime inicio,
            ZonedDateTime fin);
}
