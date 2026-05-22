package com.piedrazul.frontend.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.piedrazul.frontend.http.ApiClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.model.dto.CitaDTO;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

/**
 * Cliente HTTP para el scheduling-service (gestión de citas).
 *
 * Rutas del API Gateway:
 *   POST   /api/scheduling/citas                                  → agenda cita
 *   GET    /api/scheduling/citas/paciente/{id}                    → citas de paciente
 *   GET    /api/scheduling/citas/profesional/{id}                 → todas las citas del profesional
 *   GET    /api/scheduling/citas/profesional/{id}/fecha?fecha=    → citas por profesional y fecha (HU-6.1)
 *   GET    /api/scheduling/citas/profesional/{id}/disponibilidad?fecha= → horarios libres
 *   PATCH  /api/scheduling/citas/{id}/cancelar                   → cancela cita (HU-6.4)
 *   PUT    /api/scheduling/citas/{id}                             → reprograma cita (HU-6.3)
 */
public class CitaClient {

    private final ApiClient api;

    public CitaClient(ApiClient api) {
        this.api = api;
    }

    // ── HU-6.2: Agendar (paciente propio o agendador manual) ─────────────────

    public CitaDTO agendarCita(CitaDTO dto) throws Exception {
        HttpResponse<String> r = api.post("/api/scheduling/citas", dto);
        if (r.statusCode() == 409)
            throw new HttpException(409, "El horario ya no está disponible.");
        if (r.statusCode() == 422)
            throw new HttpException(422, "Usuario no sincronizado en el servicio de agendamiento.");
        validar(r);
        return api.mapper.readValue(r.body(), CitaDTO.class);
    }

    // ── HU-6.1: Consulta de agenda de un profesional ──────────────────────────

    /** Todas las citas de un profesional en una fecha. Usado por el agendador. */
    public List<CitaDTO> listarPorProfesionalYFecha(Long profesionalId,
                                                     LocalDate fecha) throws Exception {
        String path = "/api/scheduling/citas/profesional/" + profesionalId
                    + "/fecha?fecha=" + fecha;
        HttpResponse<String> r = api.get(path);
        validar(r);
        return api.mapper.readValue(r.body(), new TypeReference<>() {});
    }

    /** Todas las citas de un profesional (sin filtro de fecha). */
    public List<CitaDTO> listarPorProfesional(Long profesionalId) throws Exception {
        HttpResponse<String> r = api.get("/api/scheduling/citas/profesional/" + profesionalId);
        validar(r);
        return api.mapper.readValue(r.body(), new TypeReference<>() {});
    }

    // ── Listado para paciente ─────────────────────────────────────────────────

    public List<CitaDTO> listarPorPaciente(Long pacienteId) throws Exception {
        HttpResponse<String> r = api.get("/api/scheduling/citas/paciente/" + pacienteId);
        validar(r);
        return api.mapper.readValue(r.body(), new TypeReference<>() {});
    }

    // ── Horarios disponibles ──────────────────────────────────────────────────

    public List<String> obtenerHorariosDisponibles(Long profesionalId,
                                                    LocalDate fecha) throws Exception {
        String path = "/api/scheduling/citas/profesional/" + profesionalId
                    + "/disponibilidad?fecha=" + fecha;
        HttpResponse<String> r = api.get(path);
        validar(r);
        return api.mapper.readValue(r.body(), new TypeReference<>() {});
    }

    // ── HU-6.4: Cancelar ──────────────────────────────────────────────────────

    public CitaDTO cancelarCita(Long citaId) throws Exception {
        HttpResponse<String> r = api.patch("/api/scheduling/citas/" + citaId + "/cancelar", null);
        if (r.statusCode() == 422)
            throw new HttpException(422, "La cita ya fue atendida o cancelada y no se puede cancelar.");
        validar(r);
        return api.mapper.readValue(r.body(), CitaDTO.class);
    }

    // ── HU-6.3: Reprogramar ───────────────────────────────────────────────────

    /**
     * Actualiza la fecha/hora de una cita existente.
     * El backend rechaza con 409 si el nuevo horario está ocupado
     * y con 422 si la cita no está en estado programada.
     */
    public CitaDTO actualizarCita(Long citaId, CitaDTO dto) throws Exception {
        HttpResponse<String> r = api.put("/api/scheduling/citas/" + citaId, dto);
        if (r.statusCode() == 409)
            throw new HttpException(409, "El nuevo horario ya está ocupado.");
        if (r.statusCode() == 422)
            throw new HttpException(422, "Solo se pueden reprogramar citas en estado programada.");
        validar(r);
        return api.mapper.readValue(r.body(), CitaDTO.class);
    }

    // ── Utilidad ──────────────────────────────────────────────────────────────

    private void validar(HttpResponse<String> r) {
        if (!api.isSuccess(r))
            throw new HttpException(r.statusCode(),
                    "Error en scheduling-service: " + r.statusCode());
    }
}
