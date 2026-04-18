package com.piedrazul.frontend.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.piedrazul.frontend.http.ApiClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.model.dto.ProfesionalDTO;

import java.net.http.HttpResponse;
import java.util.List;

/**
 * Cliente HTTP para consultas de especialidades y profesionales.
 *
 * Rutas esperadas en el API Gateway:
 *   GET /especialidades           → lista de nombres (strings)
 *   GET /profesionales/activos?especialidad={nombre}
 *
 * Ajusta las rutas según cómo las hayas definido en tu gateway/microservicios.
 */
public class EspecialidadClient {

    private final ApiClient api;

    public EspecialidadClient(ApiClient api) {
        this.api = api;
    }

    public List<String> listarNombres() throws Exception {
        HttpResponse<String> r = api.get("/api/scheduling/especialidades");
        validar(r);
        return api.mapper.readValue(r.body(), new TypeReference<>() {});
    }

    public List<ProfesionalDTO> listarActivosPorEspecialidad(String especialidad) throws Exception {
        String path = "/api/scheduling/profesionales/activos?especialidad="
                + java.net.URLEncoder.encode(especialidad, java.nio.charset.StandardCharsets.UTF_8);
        HttpResponse<String> r = api.get(path);
        validar(r);
        return api.mapper.readValue(r.body(), new TypeReference<>() {});
    }

    private void validar(HttpResponse<String> r) {
        if (!api.isSuccess(r))
            throw new HttpException(r.statusCode(),
                    "Error al consultar especialidades: " + r.statusCode());
    }
}
