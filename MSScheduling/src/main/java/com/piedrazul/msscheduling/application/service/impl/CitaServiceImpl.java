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
import com.piedrazul.msscheduling.domain.model.exceptions.TransicionEstadoInvalidaException;
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

    /**
     * HU-6.1 – Devuelve todas las citas de un profesional en una fecha concreta.
     * Utiliza el query existente findByProfesionalIdAndFechaHoraBetween acotando
     * el rango al inicio y fin del día.
     */
    @Override
    public List<CitaDTO> listarPorProfesionalYFecha(Long profesionalId, LocalDate fecha) {
        ZoneId zona  = ZoneId.systemDefault();
        ZonedDateTime inicio = fecha.atStartOfDay(zona);
        ZonedDateTime fin    = fecha.atTime(LocalTime.MAX).atZone(zona);

        return citaRepository
                .findByProfesionalIdAndFechaHoraBetween(profesionalId, inicio, fin)
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

    @Override
    @Transactional
    public CitaDTO cancelarCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new CitaNoEncontradaException(id.toString()));

        estadoResolver.resolve(cita.getEstado()).cancelar(cita);

        CitaDTO cancelada = toDTO(citaRepository.save(cita));
        citaEventPublisher.publicarCitaCancelada(cancelada);
        log.info("Cita cancelada: id={}", id);
        return cancelada;
    }

    @Override
    @Transactional
    public CitaDTO completarCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new CitaNoEncontradaException(id.toString()));

        estadoResolver.resolve(cita.getEstado()).completar(cita);

        CitaDTO completada = toDTO(citaRepository.save(cita));
        citaEventPublisher.publicarCitaCompletada(completada);
        log.info("Cita completada: id={}", id);
        return completada;
    }

    /**
     * HU-6.3 – Reprograma el horario de una cita existente.
     * Solo se permite si la cita está en estado {@code programada}.
     * El nuevo horario debe estar disponible dentro de la agenda del profesional.
     */
    @Override
    @Transactional
    public CitaDTO actualizarCita(Long id, CitaDTO dto) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new CitaNoEncontradaException(id.toString()));

        // HU-6.3 – Restricción: solo citas programadas pueden ser reprogramadas
        if (cita.getEstado() != EstadoCita.programada) {
            throw new TransicionEstadoInvalidaException(
                    cita.getEstado(),
                    EstadoCita.programada
            );
        }

        ZonedDateTime nuevaFechaHora = dto.getFechaHora();

        // Solo validar disponibilidad si la fecha/hora cambia
        if (!nuevaFechaHora.equals(cita.getFechaHora())) {
            // isProfesionalDisponible también chequea existsByProfesionalIdAndFechaHora;
            // como la cita actual aún conserva su fechaHora antigua en DB, la consulta
            // sobre la nueva fechaHora no la encontrará → sin falso positivo.
            if (!isProfesionalDisponible(cita.getProfesionalId(), nuevaFechaHora)) {
                throw new HorarioOcupadoException();
            }
            cita.setFechaHora(nuevaFechaHora);
        }

        CitaDTO actualizada = toDTO(citaRepository.save(cita));
        log.info("Cita reprogramada: id={} nuevaFecha={}", id, nuevaFechaHora);
        return actualizada;
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
