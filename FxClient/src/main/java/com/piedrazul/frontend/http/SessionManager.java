package com.piedrazul.frontend.http;

import com.piedrazul.frontend.model.dto.UsuarioDTO;

/**
 * Almacena el estado de la sesión activa (singleton).
 *
 * Reemplaza el manejo de sesión que Spring Security o el contexto
 * de Spring hacían implícitamente en el monolito.
 *
 * El JWT se añade a cada petición HTTP en ApiClient.
 */
public class SessionManager {

    private static SessionManager instance;

    private String     jwtToken;
    private UsuarioDTO usuarioActual;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    // ── Token ────────────────────────────────────────────────────

    public void setToken(String token) {
        this.jwtToken = token;
    }

    public String getToken() {
        return jwtToken;
    }

    public boolean hasSesion() {
        return jwtToken != null && !jwtToken.isBlank();
    }

    // ── Usuario ──────────────────────────────────────────────────

    public void setUsuarioActual(UsuarioDTO usuario) {
        this.usuarioActual = usuario;
    }

    public UsuarioDTO getUsuarioActual() {
        return usuarioActual;
    }

    /** Limpia la sesión al cerrar sesión. */
    public void cerrarSesion() {
        jwtToken      = null;
        usuarioActual = null;
    }
}
