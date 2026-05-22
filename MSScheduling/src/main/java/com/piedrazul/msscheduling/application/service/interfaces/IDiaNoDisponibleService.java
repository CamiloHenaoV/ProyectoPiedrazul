package com.piedrazul.msscheduling.application.service.interfaces;

import com.piedrazul.msscheduling.domain.model.dto.DiaNoDisponibleDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato del servicio de días no disponibles (festivos y bloqueos manuales).
 * HU-1.8: gestión de fechas no laborales que impiden el agendamiento.
 */
public interface IDiaNoDisponibleService {

    /**
     * Registra un día no disponible.
     * HU-1.8 SC-1: el sistema bloquea el agendamiento en esa fecha.
     */
    DiaNoDisponibleDTO registrar(DiaNoDisponibleDTO dto);

    /**
     * Lista todos los días no disponibles registrados.
     */
    List<DiaNoDisponibleDTO> listarTodos();

    /**
     * Lista días no disponibles dentro de un rango (para calendario UI).
     */
    List<DiaNoDisponibleDTO> listarEnRango(LocalDate desde, LocalDate hasta);

    /**
     * Elimina la restricción de una fecha, habilitándola nuevamente.
     * HU-1.8 SC-3: el sistema permite de nuevo el agendamiento en esa fecha.
     */
    void eliminar(Long id);

    /**
     * Verifica si una fecha está bloqueada a nivel global.
     * HU-1.8 SC-2: usado por CitaService al validar disponibilidad.
     */
    boolean esFechaNoDisponible(LocalDate fecha);
}
