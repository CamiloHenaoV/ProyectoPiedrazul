package com.piedrazul.msscheduling.application.service.impl;

import com.piedrazul.msscheduling.application.service.interfaces.ICitaService;
import com.piedrazul.msscheduling.domain.model.builder.CitaProgramadaBuilder;
import com.piedrazul.msscheduling.domain.model.builder.DirectorCita;
import com.piedrazul.msscheduling.domain.model.dto.CitaDTO;
import com.piedrazul.msscheduling.domain.model.entity.Cita;
import com.piedrazul.msscheduling.domain.model.entity.UsuarioLocal;
import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;
import com.piedrazul.msscheduling.domain.model.exceptions.CitaNoEncontradaException;
import com.piedrazul.msscheduling.domain.model.exceptions.HorarioOcupadoException;
import com.piedrazul.msscheduling.domain.model.exceptions.UsuarioNoEncontradoException;
import com.piedrazul.msscheduling.domain.model.repository.BloqueoDisponibilidadRepository;
import com.piedrazul.msscheduling.domain.model.repository.CitaRepository;
import com.piedrazul.msscheduling.domain.model.repository.DisponibilidadSemanalRepository;
import com.piedrazul.msscheduling.domain.model.repository.UsuarioLocalRepository;
import com.piedrazul.msscheduling.domain.model.state.CitaEstadoResolver;
import com.piedrazul.msscheduling.infra.messaging.publisher.CitaEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CitaServiceImpl implements ICitaService {

    private final CitaRepository                  citaRepository;
    private final DisponibilidadSemanalRepository disponibilidadRepository;
    private final BloqueoDisponibilidadRepository bloqueoRepository;
    private final UsuarioLocalRepository          usuarioLocalRepository;
    private final CitaEventPublisher              citaEventPublisher;
    private final CitaEstadoResolver              estadoResolver;

    public CitaServiceImpl(CitaRepository citaRepository,
                           DisponibilidadSemanalRepository disponibilidadRepository,
                           BloqueoDisponibilidadRepository bloqueoRepository,
                           UsuarioLocalRepository usuarioLocalRepository,
                           CitaEventPublisher citaEventPublisher,
                           CitaEstadoResolver estadoResolver) {
        this.citaRepository           = citaRepository;
        this.disponibilidadRepository = disponibilidadRepository;
        this.bloqueoRepository        = bloqueoRepository;
        this.usuarioLocalRepository   = usuarioLocalRepository;
        this.citaEventPublisher       = citaEventPublisher;
        this.estadoResolver           = estadoResolver;
    }

    // ── Agendar ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CitaDTO agendarCita(CitaDTO dto) {
        if (!isProfesionalDisponible(dto.getProfesionalId(), dto.getFechaHora())) {
            throw new HorarioOcupadoException();
        }

        UsuarioLocal paciente    = usuarioLocalRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new UsuarioNoEncontradoException(dto.getPacienteId().toString()));
        UsuarioLocal profesional = usuarioLocalRepository.findById(dto.getProfesionalId())
                .orElseThrow(() -> new UsuarioNoEncontradoException(dto.getProfesionalId().toString()));

        DirectorCita director = new DirectorCita();
        director.setCitaBuilder(new CitaProgramadaBuilder());
        director.construirCita(paciente, profesional, dto.getFechaHora());
        Cita cita = director.getCita();

        CitaDTO guardada = toDTO(citaRepository.save(cita));
        citaEventPublisher.publicarCitaAgendada(guardada);
        log.info("Cita agendada: paciente={} profesional={} fecha={}",
                paciente.getId(), profesional.getId(), dto.getFechaHora());
        return guardada;
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    @Override
    public CitaDTO buscarPorId(Long id) {
        return citaRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new CitaNoEncontradaException(id.toString()));
    }

    @Override
    public List<CitaDTO> listarPorPaciente(Long pacienteId) {
        return citaRepository.findByPacienteId(pacienteId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CitaDTO> listarPorProfesional(Long profesionalId) {
        return citaRepository.findByProfesionalId(profesionalId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ZonedDateTime> obtenerHorariosDisponibles(Long profesionalId, LocalDate fecha) {
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
                        slot.isAfter(ZonedDateTime.now()) &&
                        !citaRepository.existsByProfesionalIdAndFechaHora(profesionalId, slot) &&
                        !bloqueoRepository.existeBloqueoEnFecha(profesionalId, slot)
                )
                .collect(Collectors.toList());
    }

    @Override
    public long contarCitasPorEstado(EstadoCita estado) {
        return citaRepository.findAll()
                .stream()
                .filter(c -> c.getEstado() == estado)
                .count();
    }

    // ── Transiciones de estado (patrón State) ────────────────────────────────

    /**
     * Cancela la cita si su estado actual lo permite.
     *
     * <p>El {@link CitaEstadoResolver} devuelve el handler correspondiente al
     * estado actual de la cita. Si el estado es {@code programada}, el handler
     * muta la entidad a {@code cancelada}. Si la cita ya está en un estado
     * terminal ({@code cancelada} o {@code completada}), el handler lanza
     * {@link com.piedrazul.msscheduling.domain.model.exceptions.TransicionEstadoInvalidaException}.
     *
     * @param id identificador de la cita a cancelar.
     * @return DTO de la cita actualizada.
     */
    @Override
    @Transactional
    public CitaDTO cancelarCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new CitaNoEncontradaException(id.toString()));

        // El handler valida la transición y muta el estado de la entidad.
        // Si el estado no permite cancelar, lanza TransicionEstadoInvalidaException.
        estadoResolver.resolve(cita.getEstado()).cancelar(cita);

        CitaDTO cancelada = toDTO(citaRepository.save(cita));
        citaEventPublisher.publicarCitaCancelada(cancelada);
        log.info("Cita cancelada: id={}", id);
        return cancelada;
    }

    /**
     * Marca la cita como completada si su estado actual lo permite.
     *
     * <p>Análogo a {@link #cancelarCita}: el handler del estado actual decide
     * si la transición es válida antes de mutar la entidad.
     *
     * @param id identificador de la cita a completar.
     * @return DTO de la cita actualizada.
     */
    @Override
    @Transactional
    public CitaDTO completarCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new CitaNoEncontradaException(id.toString()));

        // El handler valida la transición y muta el estado de la entidad.
        // Si el estado no permite completar, lanza TransicionEstadoInvalidaException.
        estadoResolver.resolve(cita.getEstado()).completar(cita);

        CitaDTO completada = toDTO(citaRepository.save(cita));
        citaEventPublisher.publicarCitaCompletada(completada);
        log.info("Cita completada: id={}", id);
        return completada;
    }

    // ── Utilidades privadas ───────────────────────────────────────────────────

    private boolean isProfesionalDisponible(Long profesionalId, ZonedDateTime fechaHora) {
        if (fechaHora.isBefore(ZonedDateTime.now())) return false;
        if (citaRepository.existsByProfesionalIdAndFechaHora(profesionalId, fechaHora)) return false;
        if (bloqueoRepository.existeBloqueoEnFecha(profesionalId, fechaHora)) return false;

        int diaSemana = fechaHora.getDayOfWeek().getValue() % 7;
        LocalTime hora = fechaHora.toLocalTime();

        return disponibilidadRepository
                .findByProfesionalIdAndDiaSemana(profesionalId, diaSemana)
                .stream()
                .anyMatch(d ->
                        !hora.isBefore(d.getHoraInicio()) &&
                        !hora.isAfter(d.getHoraFin().minusMinutes(d.getDuracionCitaMinutos()))
                );
    }

    private CitaDTO toDTO(Cita c) {
        return CitaDTO.builder()
                .id(c.getId())
                .pacienteId(c.getPacienteId())
                .pacienteNombre(c.getPacienteNombre())
                .profesionalId(c.getProfesionalId())
                .profesionalNombre(c.getProfesionalNombre())
                .fechaHora(c.getFechaHora())
                .estado(c.getEstado())
                .build();
    }
}
