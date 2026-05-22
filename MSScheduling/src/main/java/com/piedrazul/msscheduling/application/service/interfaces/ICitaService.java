package com.piedrazul.msscheduling.application.service.interfaces;

import com.piedrazul.msscheduling.domain.model.dto.CitaDTO;
import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public interface ICitaService {
    CitaDTO agendarCita(CitaDTO dto);
    CitaDTO buscarPorId(Long id);
    List<CitaDTO> listarPorPaciente(Long pacienteId);
    List<CitaDTO> listarPorProfesional(Long profesionalId);

    // HU-6.1: búsqueda de citas de un profesional en una fecha específica
    List<CitaDTO> listarPorProfesionalYFecha(Long profesionalId, LocalDate fecha);

    List<ZonedDateTime> obtenerHorariosDisponibles(Long profesionalId, LocalDate fecha);
    CitaDTO cancelarCita(Long id);
    CitaDTO completarCita(Long id);

    // HU-6.3: reprogramar una cita existente
    CitaDTO actualizarCita(Long id, CitaDTO dto);

    long contarCitasPorEstado(EstadoCita estado);
}
