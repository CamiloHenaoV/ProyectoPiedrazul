package com.piedrazul.msscheduling.application.service.impl;

import com.piedrazul.msscheduling.application.service.interfaces.IDisponibilidadService;
import com.piedrazul.msscheduling.domain.model.dto.DisponibilidadSemanalDTO;
import com.piedrazul.msscheduling.domain.model.entity.Cita;
import com.piedrazul.msscheduling.domain.model.entity.DisponibilidadSemanal;
import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;
import com.piedrazul.msscheduling.domain.model.exceptions.ConfiguracionInvalidaException;
import com.piedrazul.msscheduling.domain.model.repository.CitaRepository;
import com.piedrazul.msscheduling.domain.model.repository.DisponibilidadSemanalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de disponibilidad semanal.
 *
 * HU-1.5:
 *   SC-1: guardar configuración válida.
 *   SC-2: validar campos obligatorios (via @Valid en controller + validaciones de negocio aquí).
 *   SC-3: habilitar días específicos de atención.
 *   SC-4: configurar franjas horarias (horaInicio – horaFin).
 *
 * HU-1.6:
 *   SC-1: persistir la duración de cita = intervalo entre citas.
 *   SC-2: validar que el intervalo sea consistente con la franja horaria.
 *   SC-3: al actualizar, advertir si citas futuras quedan fuera del nuevo horario.
 */
@Service
@Slf4j
public class DisponibilidadServiceImpl implements IDisponibilidadService {

    private final DisponibilidadSemanalRepository disponibilidadRepository;
    private final CitaRepository                  citaRepository;

    public DisponibilidadServiceImpl(DisponibilidadSemanalRepository disponibilidadRepository,
                                     CitaRepository citaRepository) {
        this.disponibilidadRepository = disponibilidadRepository;
        this.citaRepository           = citaRepository;
    }

    // ── Crear ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public DisponibilidadSemanalDTO crear(DisponibilidadSemanalDTO dto) {
        validarFranjaHoraria(dto);
        validarIntervaloCaben(dto);

        DisponibilidadSemanal entidad = toEntidad(dto);
        DisponibilidadSemanal guardada = disponibilidadRepository.save(entidad);

        log.info("Disponibilidad creada: profesional={} dia={} {}–{} c/{}min",
                guardada.getProfesionalId(), guardada.getDiaSemana(),
                guardada.getHoraInicio(), guardada.getHoraFin(),
                guardada.getDuracionCitaMinutos());

        return toDTO(guardada);
    }

    // ── Actualizar ────────────────────────────────────────────────────────────

    /**
     * HU-1.6 SC-3: al modificar la configuración se recalcula la disponibilidad.
     * Si existen citas futuras que caen dentro del nuevo horario, se conservan.
     * Si hay citas futuras que quedarían fuera, se registra una advertencia
     * (no se cancelan automáticamente — requiere acción administrativa explícita).
     */
    @Override
    @Transactional
    public DisponibilidadSemanalDTO actualizar(Long id, DisponibilidadSemanalDTO dto) {
        DisponibilidadSemanal existente = disponibilidadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Configuración de disponibilidad no encontrada: " + id));

        validarFranjaHoraria(dto);
        validarIntervaloCaben(dto);

        // HU-1.6 SC-3: verificar citas futuras que puedan verse afectadas
        verificarCitasFuturas(existente, dto);

        existente.setDiaSemana(dto.getDiaSemana());
        existente.setHoraInicio(dto.getHoraInicio());
        existente.setHoraFin(dto.getHoraFin());
        existente.setDuracionCitaMinutos(dto.getDuracionCitaMinutos());

        DisponibilidadSemanal guardada = disponibilidadRepository.save(existente);

        log.info("Disponibilidad actualizada: profesional={} dia={} {}–{} c/{}min",
                guardada.getProfesionalId(), guardada.getDiaSemana(),
                guardada.getHoraInicio(), guardada.getHoraFin(),
                guardada.getDuracionCitaMinutos());

        return toDTO(guardada);
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    @Override
    public List<DisponibilidadSemanalDTO> listarPorProfesional(Long profesionalId) {
        return disponibilidadRepository.findByProfesionalId(profesionalId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        disponibilidadRepository.deleteById(id);
        log.info("Disponibilidad eliminada: id={}", id);
    }

    // ── Validaciones de negocio ───────────────────────────────────────────────

    /**
     * HU-1.5 SC-4 / HU-1.6 SC-2:
     * La hora de fin debe ser posterior a la hora de inicio.
     */
    private void validarFranjaHoraria(DisponibilidadSemanalDTO dto) {
        if (!dto.getHoraFin().isAfter(dto.getHoraInicio())) {
            throw new ConfiguracionInvalidaException(
                    "La hora de fin (" + dto.getHoraFin() +
                    ") debe ser posterior a la hora de inicio (" + dto.getHoraInicio() + ").");
        }
    }

    /**
     * HU-1.6 SC-2: el intervalo (duracionCitaMinutos) debe permitir
     * al menos una cita dentro de la franja horaria.
     */
    private void validarIntervaloCaben(DisponibilidadSemanalDTO dto) {
        long minutosTotales = java.time.Duration.between(
                dto.getHoraInicio(), dto.getHoraFin()).toMinutes();

        if (minutosTotales < dto.getDuracionCitaMinutos()) {
            throw new ConfiguracionInvalidaException(
                    "El intervalo de " + dto.getDuracionCitaMinutos() +
                    " minutos no cabe en la franja horaria de " + minutosTotales + " minutos. " +
                    "Reduzca el intervalo o amplíe la franja horaria.");
        }
    }

    /**
     * HU-1.6 SC-3: detecta citas futuras que quedarían fuera del nuevo horario.
     * Registra una advertencia en el log; no cancela automáticamente las citas.
     */
    private void verificarCitasFuturas(DisponibilidadSemanal existente,
                                        DisponibilidadSemanalDTO nuevo) {
        List<Cita> citasFuturas = citaRepository
                .findByProfesionalIdAndFechaHoraBetween(
                        existente.getProfesionalId(),
                        ZonedDateTime.now(),
                        ZonedDateTime.now().plusWeeks(52))
                .stream()
                .filter(c -> c.getEstado() == EstadoCita.programada)
                .filter(c -> {
                    int diaCita = c.getFechaHora().getDayOfWeek().getValue() % 7;
                    if (diaCita != nuevo.getDiaSemana()) return false;

                    LocalTime horaCita = c.getFechaHora().toLocalTime();
                    // La cita queda fuera si está antes del nuevo inicio o
                    // en/después del nuevo fin menos el intervalo
                    return horaCita.isBefore(nuevo.getHoraInicio()) ||
                           !horaCita.isBefore(nuevo.getHoraFin().minusMinutes(nuevo.getDuracionCitaMinutos()).plusMinutes(1));
                })
                .collect(Collectors.toList());

        if (!citasFuturas.isEmpty()) {
            log.warn("HU-1.6 SC-3: {} cita(s) futura(s) del profesional {} podrían quedar " +
                     "fuera del nuevo horario configurado. IDs: {}",
                    citasFuturas.size(),
                    existente.getProfesionalId(),
                    citasFuturas.stream().map(c -> c.getId().toString())
                                .collect(Collectors.joining(", ")));
        }
    }

    // ── Mapeos ────────────────────────────────────────────────────────────────

    private DisponibilidadSemanal toEntidad(DisponibilidadSemanalDTO dto) {
        return DisponibilidadSemanal.builder()
                .profesionalId(dto.getProfesionalId())
                .diaSemana(dto.getDiaSemana())
                .horaInicio(dto.getHoraInicio())
                .horaFin(dto.getHoraFin())
                .duracionCitaMinutos(
                        dto.getDuracionCitaMinutos() != null ? dto.getDuracionCitaMinutos() : 30)
                .build();
    }

    private DisponibilidadSemanalDTO toDTO(DisponibilidadSemanal d) {
        return DisponibilidadSemanalDTO.builder()
                .id(d.getId())
                .profesionalId(d.getProfesionalId())
                .diaSemana(d.getDiaSemana())
                .horaInicio(d.getHoraInicio())
                .horaFin(d.getHoraFin())
                .duracionCitaMinutos(d.getDuracionCitaMinutos())
                .build();
    }
}
