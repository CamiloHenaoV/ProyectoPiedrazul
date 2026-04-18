package com.piedrazul.frontend.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.piedrazul.frontend.http.ApiClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.model.dto.PacienteDTO;
import com.piedrazul.frontend.model.dto.ProfesionalDTO;
import com.piedrazul.frontend.model.dto.RegistroPacienteRequestDTO;
import com.piedrazul.frontend.model.dto.RegistroProfesionalRequestDTO;
import com.piedrazul.frontend.model.dto.UsuarioDTO;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

/**
 * Cliente HTTP para el user-service (gestión de usuarios).
 *
 * Rutas esperadas en el API Gateway:
 *   GET    /usuarios              → lista todos
 *   GET    /usuarios/{id}         → busca por id
 *   GET    /usuarios/{id}/paciente-id → devuelve el UUID del paciente asociado
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
        HttpResponse<String> r = api.get("/usuarios");
        validar(r);
        return api.mapper.readValue(r.body(), new TypeReference<>() {});
    }

    public long contarActivos() throws Exception {
        HttpResponse<String> r = api.get("/usuarios/count/activos");
        validar(r);
        return Long.parseLong(r.body().trim());
    }

    // ── Búsqueda ─────────────────────────────────────────────────

    public UsuarioDTO buscarPorId(UUID id) throws Exception {
        HttpResponse<String> r = api.get("/usuarios/" + id);
        validar(r);
        return api.mapper.readValue(r.body(), UsuarioDTO.class);
    }

    /**
     * Devuelve el UUID del Paciente asociado a un usuario.
     * Necesario para agendar/listar citas.
     */
    public UUID buscarPacienteIdPorUsuarioId(UUID usuarioId) throws Exception {
        HttpResponse<String> r = api.get("/usuarios/" + usuarioId + "/paciente-id");
        validar(r);
        // El endpoint devuelve el UUID como string plano o JSON "\"uuid\""
        String raw = r.body().trim().replace("\"", "");
        return UUID.fromString(raw);
    }

    // ── Creación ─────────────────────────────────────────────────

    /** Crea un usuario con rol administrador (sin perfil adicional). */
    public UsuarioDTO crearUsuario(UsuarioDTO dto) throws Exception {
        HttpResponse<String> r = api.post("/usuarios", dto);
        if (r.statusCode() == 409)
            throw new HttpException(409, "El login ya está en uso.");
        validar(r);
        return api.mapper.readValue(r.body(), UsuarioDTO.class);
    }

    /** Crea usuario + datos de paciente en una sola petición. */
    public void registrarPaciente(UsuarioDTO usuario, PacienteDTO paciente) throws Exception {
        RegistroPacienteRequestDTO body =
                new RegistroPacienteRequestDTO(usuario, paciente);
        HttpResponse<String> r = api.post("/usuarios/pacientes", body);
        if (r.statusCode() == 409)
            throw new HttpException(409, "El login ya está en uso.");
        validar(r);
    }

    /** Crea usuario + datos de profesional en una sola petición. */
    public void registrarProfesional(UsuarioDTO usuario,
                                     ProfesionalDTO profesional) throws Exception {
        RegistroProfesionalRequestDTO body =
                new RegistroProfesionalRequestDTO(usuario, profesional);
        HttpResponse<String> r = api.post("/usuarios/profesionales", body);
        if (r.statusCode() == 409)
            throw new HttpException(409, "El login ya está en uso.");
        validar(r);
    }

    // ── Edición ──────────────────────────────────────────────────

    public UsuarioDTO actualizarUsuario(UUID id, UsuarioDTO dto) throws Exception {
        HttpResponse<String> r = api.put("/usuarios/" + id, dto);
        validar(r);
        return api.mapper.readValue(r.body(), UsuarioDTO.class);
    }

    public void activarUsuario(UUID id) throws Exception {
        HttpResponse<String> r = api.patch("/usuarios/" + id + "/activar", null);
        validar(r);
    }

    public void desactivarUsuario(UUID id) throws Exception {
        HttpResponse<String> r = api.patch("/usuarios/" + id + "/desactivar", null);
        validar(r);
    }

    // ── Interno ──────────────────────────────────────────────────

    private void validar(HttpResponse<String> r) {
        if (!api.isSuccess(r))
            throw new HttpException(r.statusCode(),
                    "Error en user-service: " + r.statusCode());
    }
}
