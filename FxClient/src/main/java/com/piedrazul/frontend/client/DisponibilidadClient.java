package com.piedrazul.frontend.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.piedrazul.frontend.http.ApiClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.model.dto.ConfiguracionAgendamientoDTO;
import com.piedrazul.frontend.model.dto.DiaNoDisponibleDTO;
import com.piedrazul.frontend.model.dto.DisponibilidadSemanalDTO;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

/**
 * Cliente HTTP para los endpoints de configuración de disponibilidad.
 *
 * Cubre:
 *   HU-1.5 / HU-1.6 → /api/scheduling/disponibilidad
 *   HU-1.7           → /api/scheduling/configuracion
 *   HU-1.8           → /api/scheduling/dias-no-disponibles
 */
public class DisponibilidadClient {

    private final ApiClient api;

    public DisponibilidadClient(ApiClient api) {
        this.api = api;
    }

    // ── HU-1.5 / HU-1.6: Disponibilidad semanal ──────────────────────────────

    /**
     * HU-1.5 SC-1: crear nueva disponibilidad semanal.
     * HTTP 201 Created; 400 si campos inválidos; 422 si intervalo inconsistente.
     */
    public DisponibilidadSemanalDTO crearDisponibilidad(DisponibilidadSemanalDTO dto)
            throws Exception {
        HttpResponse<String> r = api.post("/api/scheduling/disponibilidad", dto);
        if (r.statusCode() == 400 || r.statusCode() == 422)
            throw new HttpException(r.statusCode(), extraerMensaje(r.body()));
        validar(r);
        return api.mapper.readValue(r.body(), DisponibilidadSemanalDTO.class);
    }

    /**
     * HU-1.6 SC-3: actualizar disponibilidad existente.
     */
    public DisponibilidadSemanalDTO actualizarDisponibilidad(Long id,
                                                              DisponibilidadSemanalDTO dto)
            throws Exception {
        HttpResponse<String> r = api.put("/api/scheduling/disponibilidad/" + id, dto);
        if (r.statusCode() == 400 || r.statusCode() == 422)
            throw new HttpException(r.statusCode(), extraerMensaje(r.body()));
        validar(r);
        return api.mapper.readValue(r.body(), DisponibilidadSemanalDTO.class);
    }

    /**
     * Lista todas las disponibilidades de un profesional.
     */
    public List<DisponibilidadSemanalDTO> listarPorProfesional(Long profesionalId)
            throws Exception {
        HttpResponse<String> r = api.get(
                "/api/scheduling/disponibilidad/profesional/" + profesionalId);
        validar(r);
        return api.mapper.readValue(r.body(), new TypeReference<>() {});
    }

    /**
     * Elimina una configuración de disponibilidad.
     */
    public void eliminarDisponibilidad(Long id) throws Exception {
        HttpResponse<String> r = api.delete("/api/scheduling/disponibilidad/" + id);
        validar(r);
    }

    // ── HU-1.7: Configuración de ventana de agendamiento ─────────────────────

    /**
     * HU-1.7: obtiene la configuración actual de la ventana de agendamiento.
     */
    public ConfiguracionAgendamientoDTO obtenerConfiguracion() throws Exception {
        HttpResponse<String> r = api.get("/api/scheduling/configuracion");
        validar(r);
        return api.mapper.readValue(r.body(), ConfiguracionAgendamientoDTO.class);
    }

    /**
     * HU-1.7 SC-1/SC-3: actualiza las semanas habilitadas para agendamiento.
     */
    public ConfiguracionAgendamientoDTO actualizarConfiguracion(
            ConfiguracionAgendamientoDTO dto) throws Exception {
        HttpResponse<String> r = api.put("/api/scheduling/configuracion", dto);
        if (r.statusCode() == 400)
            throw new HttpException(400, extraerMensaje(r.body()));
        validar(r);
        return api.mapper.readValue(r.body(), ConfiguracionAgendamientoDTO.class);
    }

    /**
     * HU-1.7 SC-2: obtiene la fecha máxima permitida para agendar citas.
     * El cliente la usa para limitar el DatePicker en el formulario de citas.
     */
    public LocalDate obtenerFechaMaximaAgendamiento() throws Exception {
        HttpResponse<String> r = api.get("/api/scheduling/configuracion/fecha-maxima");
        validar(r);
        // El backend devuelve una fecha ISO: "2025-07-15"
        String fecha = r.body().replace("\"", "");
        return LocalDate.parse(fecha);
    }

    // ── HU-1.8: Días no disponibles y festivos ────────────────────────────────

    /**
     * HU-1.8 SC-1: registra un día no disponible o festivo.
     */
    public DiaNoDisponibleDTO registrarDiaNoDisponible(DiaNoDisponibleDTO dto)
            throws Exception {
        HttpResponse<String> r = api.post("/api/scheduling/dias-no-disponibles", dto);
        if (r.statusCode() == 400)
            throw new HttpException(400, extraerMensaje(r.body()));
        validar(r);
        return api.mapper.readValue(r.body(), DiaNoDisponibleDTO.class);
    }

    /**
     * Lista todos los días no disponibles registrados.
     */
    public List<DiaNoDisponibleDTO> listarDiasNoDisponibles() throws Exception {
        HttpResponse<String> r = api.get("/api/scheduling/dias-no-disponibles");
        validar(r);
        return api.mapper.readValue(r.body(), new TypeReference<>() {});
    }

    /**
     * HU-1.8 SC-3: elimina la restricción de una fecha (la habilita de nuevo).
     */
    public void eliminarDiaNoDisponible(Long id) throws Exception {
        HttpResponse<String> r = api.delete("/api/scheduling/dias-no-disponibles/" + id);
        validar(r);
    }

    // ── Utilidades ─────────────────────────────────────────────────────────

    private void validar(HttpResponse<String> r) {
        if (!api.isSuccess(r))
            throw new HttpException(r.statusCode(),
                    "Error en scheduling-service: " + r.statusCode());
    }

    /** Extrae el campo 'detalle' del JSON de error del GlobalExceptionHandler. */
    private String extraerMensaje(String body) {
        try {
            var node = api.mapper.readTree(body);
            if (node.has("detalle")) return node.get("detalle").asText();
            if (node.has("mensaje")) return node.get("mensaje").asText();
        } catch (Exception ignored) {}
        return body;
    }
}
