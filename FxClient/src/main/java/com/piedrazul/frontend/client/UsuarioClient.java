package com.piedrazul.frontend.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.piedrazul.frontend.http.ApiClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.model.dto.*;

import java.net.http.HttpResponse;
import java.util.List;

/**
 * Cliente HTTP para el user-service (gestión de usuarios).
 *
 * Rutas esperadas en el APi-Gateway:
 *   GET    /usuarios              → lista todos
 *   GET    /usuarios/{id}         → busca por id
 *   GET    /usuarios/{id}/paciente-id → devuelve el Long del paciente asociado
 *   POST   /usuarios              → crea usuario base (admin)
 *   POST   /usuarios/pacientes    → crea usuario + paciente
 *   POST   /usuarios/profesionales→ crea usuario + profesional
 *   PUT    /usuarios/{id}         → actualiza
 *   PATCH  /usuarios/{id}/activar
 *   PATCH  /usuarios/{id}/desactivar
 *   GET    /usuarios/count/activos
 *
 * Reemplaza IUsuarioService + IRegistroService del monolito.
 */
public class UsuarioClient {

    private final ApiClient api;

    public UsuarioClient(ApiClient api) {
        this.api = api;
    }

    // ── Listado ──────────────────────────────────────────────────

    public List<UsuarioDTO> listarTodos() throws Exception {
        HttpResponse<String> r = api.get("/api/users/usuarios");
        validar(r);
        return api.mapper.readValue(r.body(), new TypeReference<>() {});
    }

    public long contarActivos() throws Exception {
        HttpResponse<String> r = api.get("/api/users/usuarios/activos/count"); // path correcto
        validar(r);
        JsonNode node = api.mapper.readTree(r.body());
        return node.get("total").asLong();
    }

    // ── Búsqueda ─────────────────────────────────────────────────

    public UsuarioDTO buscarPorId(Long id) throws Exception {
        HttpResponse<String> r = api.get("/api/users/usuarios/" + id);
        validar(r);
        return api.mapper.readValue(r.body(), UsuarioDTO.class);
    }

    /**
     * Devuelve el Long del Paciente asociado a un usuario.
     * Necesario para agendar/listar citas.
     */
    public Long buscarPacienteIdPorUsuarioId(Long usuarioId) throws Exception {
        HttpResponse<String> r = api.get("/api/users/usuarios/" + usuarioId + "/paciente-id");
        validar(r);
        JsonNode node = api.mapper.readTree(r.body());
        return node.get("pacienteId").asLong();
    }

    // ── Creación ─────────────────────────────────────────────────

    /** Crea un usuario con rol administrador (sin perfil adicional). */
    public UsuarioDTO crearUsuario(UsuarioDTO dto) throws Exception {
        HttpResponse<String> r = api.post("/api/users/registro/usuario", dto);
        if (r.statusCode() == 409)
            throw new HttpException(409, "El login ya está en uso.");
        validar(r);
        return api.mapper.readValue(r.body(), UsuarioDTO.class);
    }

    /** Crea usuario + datos de paciente en una sola petición. */
    public UsuarioDTO registrarPaciente(UsuarioDTO usuario, PacienteDTO paciente) throws Exception {
        RegistroPacienteRequestDTO body = new RegistroPacienteRequestDTO(usuario, paciente);
        HttpResponse<String> r = api.post("/api/users/registro/paciente", body);
        if (r.statusCode() == 409)
            throw new HttpException(409, "El login ya está en uso.");
        validar(r);
        return api.mapper.readValue(r.body(), UsuarioDTO.class);
    }

    /** Crea usuario + datos de profesional en una sola petición. */
    public UsuarioDTO registrarProfesional(UsuarioDTO usuario, ProfesionalDTO profesional) throws Exception {
        RegistroProfesionalRequestDTO body = new RegistroProfesionalRequestDTO(usuario, profesional);
        HttpResponse<String> r = api.post("/api/users/registro/profesional", body);
        if (r.statusCode() == 409)
            throw new HttpException(409, "El login ya está en uso.");
        validar(r);
        return api.mapper.readValue(r.body(), UsuarioDTO.class);
    }

    // ── Edición ──────────────────────────────────────────────────

    public UsuarioDTO actualizarUsuario(Long id, UsuarioDTO dto) throws Exception {
        HttpResponse<String> r = api.put("/api/users/usuarios/" + id, dto);
        validar(r);
        return api.mapper.readValue(r.body(), UsuarioDTO.class);
    }

    public void activarUsuario(Long id) throws Exception {
        HttpResponse<String> r = api.patch("/api/users/usuarios/" + id + "/activar", null);
        validar(r);
    }

    public void desactivarUsuario(Long id) throws Exception {
        HttpResponse<String> r = api.patch("/api/users/usuarios/" + id + "/desactivar", null);
        validar(r);
    }

    // ── Interno ──────────────────────────────────────────────────

    private void validar(HttpResponse<String> r) {
        if (!api.isSuccess(r))
            throw new HttpException(r.statusCode(),
                    "Error en user-service: " + r.statusCode());
    }
}
