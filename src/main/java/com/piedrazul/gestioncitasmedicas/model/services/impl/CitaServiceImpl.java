package com.piedrazul.gestioncitasmedicas.model.services.impl;

import com.piedrazul.gestioncitasmedicas.model.dto.CitaDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.Cita;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.EstadoCita;
import com.piedrazul.gestioncitasmedicas.model.exceptions.*;
import com.piedrazul.gestioncitasmedicas.model.repositories.*;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.ICitaService;
import com.piedrazul.gestioncitasmedicas.observer.AppEvent;
import com.piedrazul.gestioncitasmedicas.observer.EventBus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de citas médicas.
 * <p>
 * Coordina la lógica de agendamiento validando disponibilidad
 * del profesional antes de persistir cualquier cita.
 */
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
    /**
     * Agenda una nueva cita para un paciente con un profesional.
     * <p>
     * Delega la validación de disponibilidad a
     * {@link #isProfesionalDisponible(Integer, ZonedDateTime)}
     * antes de intentar persistir la cita.
     *
     * @param dto datos de la cita a agendar
     * @return {@link CitaDTO} con los datos de la cita creada
     * @throws HorarioOcupadoException si el profesional no está disponible
     */
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
    /**
     * Busca una cita por su identificador único.
     *
     * @param id identificador UUID de la cita
     * @return {@link CitaDTO} con los datos de la cita
     * @throws CitaNoEncontradaException si no existe una cita con ese ID
     */
    @Override
    public CitaDTO buscarPorId(UUID id) {
        return citaRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new CitaNoEncontradaException(id.toString()));
    }
    /**
     * Retorna todas las citas de un paciente.
     *
     * @param pacienteId identificador UUID del paciente
     * @return lista de {@link CitaDTO}, vacía si no tiene citas
     */
    @Override
    public List<CitaDTO> listarPorPaciente(UUID pacienteId) {
        return citaRepository.findByPacienteId(pacienteId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    /**
     * Retorna todas las citas asignadas a un profesional.
     *
     * @param profesionalId identificador del profesional
     * @return lista de {@link CitaDTO}, vacía si no tiene citas
     */
    @Override
    public List<CitaDTO> listarPorProfesional(Integer profesionalId) {
        return citaRepository.findByProfesionalId(profesionalId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    /**
     * Calcula los horarios disponibles de un profesional para una fecha dada.
     * <p>
     * El algoritmo funciona en tres pasos:
     * <ol>
     *     <li>Obtiene los bloques de disponibilidad semanal del profesional
     *         para el día de la semana correspondiente a la fecha</li>
     *     <li>Por cada bloque genera slots de tiempo separados por
     *         {@code duracionCitaMinutos}</li>
     *     <li>Filtra los slots que ya tienen cita o están dentro
     *         de un bloqueo activo</li>
     * </ol>
     * Ejemplo: bloque 08:00-10:00 con duración 30min genera
     * [08:00, 08:30, 09:00, 09:30] antes del filtrado.
     *
     * @param profesionalId identificador del profesional
     * @param fecha         fecha para la que se consulta disponibilidad
     * @return lista de {@link ZonedDateTime} con los horarios disponibles
     */
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
                        slot.isAfter(ZonedDateTime.now()) &&
                                !citaRepository.existsByProfesionalIdAndFechaHora(profesionalId, slot) &&
                                !bloqueoRepository.existeBloqueoEnFecha(profesionalId, slot)
                )
                .collect(Collectors.toList());
    }
    /**
     * Cancela una cita cambiando su estado a {@link EstadoCita#cancelada}.
     *
     * @param id identificador UUID de la cita a cancelar
     * @return {@link CitaDTO} con el estado actualizado
     * @throws CitaNoEncontradaException si no existe una cita con ese ID
     */
    @Override
    public CitaDTO cancelarCita(UUID id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new CitaNoEncontradaException(id.toString()));

        cita.setEstado(EstadoCita.cancelada);
        CitaDTO cancelada = toDTO(citaRepository.save(cita));
        eventBus.publish(AppEvent.CITA_CANCELADA, cancelada);
        return cancelada;
    }
    /**
     * Marca una cita como completada cambiando su estado
     * a {@link EstadoCita#completada}.
     *
     * @param id identificador UUID de la cita a completar
     * @return {@link CitaDTO} con el estado actualizado
     * @throws CitaNoEncontradaException si no existe una cita con ese ID
     */
    @Override
    public CitaDTO completarCita(UUID id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new CitaNoEncontradaException(id.toString()));

        cita.setEstado(EstadoCita.completada);
        CitaDTO completada = toDTO(citaRepository.save(cita));
        eventBus.publish(AppEvent.CITA_COMPLETADA, completada);
        return completada;
    }
    /**
     * Verifica si un profesional está disponible en una fecha y hora específica.
     * <p>
     * Las tres condiciones que se verifican en orden son:
     * <ol>
     *     <li>No existe otra cita en ese horario exacto</li>
     *     <li>El horario no cae dentro de un bloqueo activo</li>
     *     <li>El horario está dentro de su disponibilidad semanal</li>
     * </ol>
     *
     * @param profesionalId identificador del profesional
     * @param fechaHora     fecha y hora a verificar
     * @return {@code true} si el profesional está disponible
     */
    private boolean isProfesionalDisponible(Integer profesionalId, ZonedDateTime fechaHora) {
        if (fechaHora.isBefore(ZonedDateTime.now())) return false;
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
    /**
     * Convierte una entidad {@link Cita} a su representación {@link CitaDTO}.
     *
     * @param c entidad a convertir
     * @return DTO con los datos de la cita
     */
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