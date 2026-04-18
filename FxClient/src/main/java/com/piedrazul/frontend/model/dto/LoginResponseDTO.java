package com.piedrazul.frontend.model.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String tipo;
    private Long   expiresIn;
    private Long   usuarioId;
    private String login;
    private String nombreCompleto;
    private String rol;
}
