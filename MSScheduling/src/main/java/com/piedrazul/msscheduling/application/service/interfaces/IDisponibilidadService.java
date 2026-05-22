package com.piedrazul.msscheduling.application.service.interfaces;

import com.piedrazul.msscheduling.domain.model.dto.DisponibilidadSemanalDTO;

import java.util.List;

/**
 * Contrato del servicio de disponibilidad semanal de profesionales.
 * HU-1.5: configurar días habilitados y franjas horarias.
 * HU-1.6: definir intervalos de atención entre citas.
 */
public interface IDisponibilidadService {

    /**
     * Crea una nueva configuración de disponibilidad.
     * HU-1.5 SC-1: almacena la configuración correctamente.
     * HU-1.5 SC-2: valida campos obligatorios.
     * HU-1.6 SC-2: valida que el intervalo sea consistente con la franja horaria.
     */
    DisponibilidadSemanalDTO crear(DisponibilidadSemanalDTO dto);

    /**
     * Actualiza una configuración de disponibilidad existente.
     * HU-1.6 SC-3: recalcula disponibilidad respetando citas futuras.
     *              Si citas futuras quedan fuera del nuevo horario,
     *              se lanza una advertencia en la respuesta.
     */
    DisponibilidadSemanalDTO actualizar(Long id, DisponibilidadSemanalDTO dto);

    /**
     * Lista todas las disponibilidades de un profesional.
     */
    List<DisponibilidadSemanalDTO> listarPorProfesional(Long profesionalId);

    /**
     * Elimina una configuración de disponibilidad.
     */
    void eliminar(Long id);
}
