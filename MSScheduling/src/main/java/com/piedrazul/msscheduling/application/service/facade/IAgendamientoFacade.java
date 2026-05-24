package com.piedrazul.msscheduling.application.service.facade;

import com.piedrazul.msscheduling.domain.model.dto.CitaDTO;
import com.piedrazul.msscheduling.domain.model.dto.ConfiguracionAgendamientoDTO;
import com.piedrazul.msscheduling.domain.model.dto.DiaNoDisponibleDTO;
import com.piedrazul.msscheduling.domain.model.dto.DisponibilidadSemanalDTO;
import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Patrón Facade — «Facade»
 *
 * Proporciona una interfaz única y simplificada al subsistema de agendamiento.
 * Los controllers solo dependen de esta interfaz en lugar de coordinar
 * cuatro servicios distintos (ICitaService, IDisponibilidadService,
 * IConfiguracionAgendamientoService, IDiaNoDisponibleService).
 *
 * Beneficio inmediato: CitaController pasa de 4 dependencias a 1.
 */
public interface IAgendamientoFacade {

    // ── Operaciones de Cita ──────────────────────────────────────────────────

    CitaDTO agendarCita(CitaDTO dto);

    CitaDTO buscarCitaPorId(Long id);

    List<CitaDTO> listarCitasPorPaciente(Long pacienteId);

    List<CitaDTO> listarCitasPorProfesional(Long profesionalId);

    List<CitaDTO> listarCitasPorProfesionalYFecha(Long profesionalId, LocalDate fecha);

    List<ZonedDateTime> obtenerHorariosDisponibles(Long profesionalId, LocalDate fecha);

    CitaDTO cancelarCita(Long id);

    CitaDTO completarCita(Long id);

    CitaDTO actualizarCita(Long id, CitaDTO dto);

    long contarCitasPorEstado(EstadoCita estado);

    // ── Operaciones de Disponibilidad ────────────────────────────────────────

    DisponibilidadSemanalDTO crearDisponibilidad(DisponibilidadSemanalDTO dto);

    DisponibilidadSemanalDTO actualizarDisponibilidad(Long id, DisponibilidadSemanalDTO dto);

    List<DisponibilidadSemanalDTO> listarDisponibilidadPorProfesional(Long profesionalId);

    void eliminarDisponibilidad(Long id);

    // ── Operaciones de Configuración ─────────────────────────────────────────

    ConfiguracionAgendamientoDTO obtenerConfiguracion();

    ConfiguracionAgendamientoDTO actualizarConfiguracion(ConfiguracionAgendamientoDTO dto);

    LocalDate obtenerFechaMaximaAgendamiento();

    // ── Operaciones de Días No Disponibles ───────────────────────────────────

    DiaNoDisponibleDTO registrarDiaNoDisponible(DiaNoDisponibleDTO dto);

    List<DiaNoDisponibleDTO> listarDiasNoDisponibles();

    List<DiaNoDisponibleDTO> listarDiasNoDisponiblesEnRango(LocalDate desde, LocalDate hasta);

    void eliminarDiaNoDisponible(Long id);
}
