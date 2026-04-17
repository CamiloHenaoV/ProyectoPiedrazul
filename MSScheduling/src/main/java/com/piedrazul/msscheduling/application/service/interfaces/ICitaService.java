package com.piedrazul.msscheduling.application.service.interfaces;

import com.piedrazul.msscheduling.domain.model.dto.CitaDTO;
import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface ICitaService {
    CitaDTO agendarCita(CitaDTO dto);
    CitaDTO buscarPorId(UUID id);
    List<CitaDTO> listarPorPaciente(Long pacienteId);
    List<CitaDTO> listarPorProfesional(Long profesionalId);
    List<ZonedDateTime> obtenerHorariosDisponibles(Long profesionalId, LocalDate fecha);
    CitaDTO cancelarCita(UUID id);
    CitaDTO completarCita(UUID id);
    long contarCitasPorEstado(EstadoCita estado);
}
