package com.piedrazul.msscheduling.application.service.interfaces;

import com.piedrazul.msscheduling.domain.model.dto.ConfiguracionAgendamientoDTO;

/**
 * Contrato del servicio de configuración global de agendamiento.
 * HU-1.7: ventana de tiempo disponible para agendamiento.
 */
public interface IConfiguracionAgendamientoService {

    /**
     * Obtiene la configuración actual.
     * Si no existe se crea con el valor por defecto (4 semanas).
     */
    ConfiguracionAgendamientoDTO obtener();

    /**
     * Actualiza la ventana de tiempo habilitada para agendamiento.
     * HU-1.7 SC-1 y SC-3.
     */
    ConfiguracionAgendamientoDTO actualizar(ConfiguracionAgendamientoDTO dto);

    /**
     * Retorna la fecha máxima permitida para agendar una cita,
     * calculada a partir de hoy + semanasHabilitadas.
     */
    java.time.LocalDate obtenerFechaMaximaAgendamiento();
}
