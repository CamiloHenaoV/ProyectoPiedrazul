package com.piedraazul.gestioncitasmedicas.model.services.impl;

import com.piedraazul.gestioncitasmedicas.model.dto.CitaDTO;
import com.piedraazul.gestioncitasmedicas.model.entities.Cita;
import com.piedraazul.gestioncitasmedicas.model.entities.enums.EstadoCita;
import com.piedraazul.gestioncitasmedicas.model.exceptions.*;
import com.piedraazul.gestioncitasmedicas.model.repositories.*;
import com.piedraazul.gestioncitasmedicas.model.services.interfaces.ICitaService;
import com.piedraazul.gestioncitasmedicas.observer.AppEvent;
import com.piedraazul.gestioncitasmedicas.observer.EventBus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CitaServiceImpl implements ICitaService {

    private final CitaRepository                  citaRepository;
    private final PacienteRepository              pacienteRepository;
    private final ProfesionalRepository           profesionalRepository;
    private final DisponibilidadSemanalRepository disponibilidadRepository;
    private final BloqueoDisponibilidadRepository bloqueoRepository;
    private final EventBus                        eventBus;

    public CitaServiceImpl(
            CitaRepository                  citaRepository,
            PacienteRepository              pacienteRepository,
            ProfesionalRepository           profesionalRepository,
            DisponibilidadSemanalRepository disponibilidadRepository,
            BloqueoDisponibilidadRepository bloqueoRepository,
            EventBus                        eventBus
    ) {
        this.citaRepository           = citaRepository;
        this.pacienteRepository       = pacienteRepository;
        this.profesionalRepository    = profesionalRepository;
        this.disponibilidadRepository = disponibilidadRepository;
        this.bloqueoRepository        = bloqueoRepository;
        this.eventBus                 = eventBus;
    }

    @Override
    public CitaDTO agendarCita(CitaDTO dto) {
        if (!isProfesionalDisponible(dto.getProfesionalId(), dto.getFechaHora())) {
            throw new HorarioOcupadoException();
        }

        var paciente    = pacienteRepository.findById(dto.getPacienteId()).orElseThrow();
        var profesional = profesionalRepository.findById(dto.getProfesionalId()).orElseThrow();

        Cita cita = Cita.builder()
                .paciente(paciente)
                .profesional(profesional)
                .fechaHora(dto.getFechaHora())
                .estado(EstadoCita.programada)
                .build();

        CitaDTO guardada = toDTO(citaRepository.save(cita));
        eventBus.publish(AppEvent.CITA_AGENDADA, guardada);
        return guardada;
    }

    @Override
    public CitaDTO buscarPorId(UUID id) {
        return citaRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new CitaNoEncontradaException(id.toString()));
    }

    @Override
    public List<CitaDTO> listarPorPaciente(UUID pacienteId) {
        return citaRepository.findByPacienteId(pacienteId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CitaDTO> listarPorProfesional(Integer profesionalId) {
        return citaRepository.findByProfesionalId(profesionalId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ZonedDateTime> obtenerHorariosDisponibles(Integer profesionalId, LocalDate fecha) {
        int diaSemana = fecha.getDayOfWeek().getValue() % 7;

        return disponibilidadRepository
                .findByProfesionalIdAndDiaSemana(profesionalId, diaSemana)
                .stream()
                .flatMap(d -> {
                    List<ZonedDateTime> slots = new ArrayList<>();
                    LocalTime cursor = d.getHoraInicio();
                    while (!cursor.isAfter(d.getHoraFin().minusMinutes(d.getDuracionCitaMinutos()))) {
                        ZonedDateTime slot = ZonedDateTime.of(fecha, cursor, ZoneId.systemDefault());
                        slots.add(slot);
                        cursor = cursor.plusMinutes(d.getDuracionCitaMinutos());
                    }
                    return slots.stream();
                })
                .filter(slot ->
                        !citaRepository.existsByProfesionalIdAndFechaHora(profesionalId, slot) &&
                                !bloqueoRepository.existeBloqueoEnFecha(profesionalId, slot)
                )
                .collect(Collectors.toList());
    }

    @Override
    public CitaDTO cancelarCita(UUID id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new CitaNoEncontradaException(id.toString()));

        cita.setEstado(EstadoCita.cancelada);
        CitaDTO cancelada = toDTO(citaRepository.save(cita));
        eventBus.publish(AppEvent.CITA_CANCELADA, cancelada);
        return cancelada;
    }

    @Override
    public CitaDTO completarCita(UUID id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new CitaNoEncontradaException(id.toString()));

        cita.setEstado(EstadoCita.completada);
        CitaDTO completada = toDTO(citaRepository.save(cita));
        eventBus.publish(AppEvent.CITA_COMPLETADA, completada);
        return completada;
    }

    private boolean isProfesionalDisponible(Integer profesionalId, ZonedDateTime fechaHora) {
        if (citaRepository.existsByProfesionalIdAndFechaHora(profesionalId, fechaHora)) return false;
        if (bloqueoRepository.existeBloqueoEnFecha(profesionalId, fechaHora)) return false;

        int diaSemana       = fechaHora.getDayOfWeek().getValue() % 7;
        LocalTime hora      = fechaHora.toLocalTime();

        return disponibilidadRepository
                .findByProfesionalIdAndDiaSemana(profesionalId, diaSemana)
                .stream()
                .anyMatch(d ->
                        !hora.isBefore(d.getHoraInicio()) &&
                                !hora.isAfter(d.getHoraFin())
                );
    }

    private CitaDTO toDTO(Cita c) {
        return CitaDTO.builder()
                .id(c.getId())
                .pacienteId(c.getPaciente().getId())
                .pacienteNombre(c.getPaciente().getNombreCompleto())
                .profesionalId(c.getProfesional().getId())
                .profesionalNombre(c.getProfesional().getUsuario().getNombreCompleto())
                .fechaHora(c.getFechaHora())
                .estado(c.getEstado())
                .build();
    }
}