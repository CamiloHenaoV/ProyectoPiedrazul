package com.piedrazul.gestioncitasmedicas.model.services.interfaces;

import com.piedrazul.gestioncitasmedicas.model.dto.CitaDTO;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface ICitaService {
    CitaDTO          agendarCita(CitaDTO dto);
    CitaDTO          buscarPorId(UUID id);
    List<CitaDTO>    listarPorPaciente(UUID pacienteId);
    List<CitaDTO>    listarPorProfesional(Integer profesionalId);
    List<ZonedDateTime> obtenerHorariosDisponibles(Integer profesionalId, LocalDate fecha);
    CitaDTO          cancelarCita(UUID id);
    CitaDTO          completarCita(UUID id);
}