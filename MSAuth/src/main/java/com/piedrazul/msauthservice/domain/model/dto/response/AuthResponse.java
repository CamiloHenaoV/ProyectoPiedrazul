package com.piedrazul.msauthservice.domain.model.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Respuesta de login y refresh.
 * El cliente debe guardar ambos tokens de forma segura.
 */
@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tipo;
    private Long expiresIn;       // milisegundos hasta expiración del access token
    private Long usuarioId;
    private String login;
    private String nombreCompleto;
    private String rol;
}
