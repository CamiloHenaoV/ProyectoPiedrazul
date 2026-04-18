package com.piedrazul.frontend.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.piedrazul.frontend.http.ApiClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.model.dto.CitaDTO;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Cliente HTTP para el scheduling-service (gestión de citas).
 *
 * Rutas esperadas en el API Gateway:
 *   POST /citas                              → agenda una cita
 *   GET  /citas/paciente/{pacienteId}        → citas de un paciente
 *   GET  /citas/horarios?profesionalId=&fecha=  → horarios disponibles
 *   PATCH /citas/{id}/cancelar               → cancela una cita
 *
 * Reemplaza ICitaService del monolito.
 */
public class CitaClient {

    private final ApiClient api;

    public CitaClient(ApiClient api) {
        this.api = api;
    }

    public CitaDTO agendarCita(CitaDTO dto) throws Exception {
        HttpResponse<String> r = api.post("/citas", dto);
        // 409 = horario ya ocupado
        if (r.statusCode() == 409)
            throw new HttpException(409, "El horario ya no está disponible.");
        validar(r);
        return api.mapper.readValue(r.body(), CitaDTO.class);
    }

    public List<CitaDTO> listarPorPaciente(UUID pacienteId) throws Exception {
        HttpResponse<String> r = api.get("/citas/paciente/" + pacienteId);
        validar(r);
        return api.mapper.readValue(r.body(), new TypeReference<>() {});
    }

    public List<String> obtenerHorariosDisponibles(UUID profesionalId,
                                                    LocalDate fecha) throws Exception {
        String path = "/citas/horarios?profesionalId=" + profesionalId
                + "&fecha=" + fecha;
        HttpResponse<String> r = api.get(path);
        validar(r);
        // El servicio devuelve strings ISO-8601; los controladores los parsearán a ZonedDateTime
        return api.mapper.readValue(r.body(), new TypeReference<>() {});
    }

    public void cancelarCita(UUID citaId) throws Exception {
        HttpResponse<String> r = api.patch("/citas/" + citaId + "/cancelar", null);
        validar(r);
    }

    private void validar(HttpResponse<String> r) {
        if (!api.isSuccess(r))
            throw new HttpException(r.statusCode(),
                    "Error en scheduling-service: " + r.statusCode());
    }
}
