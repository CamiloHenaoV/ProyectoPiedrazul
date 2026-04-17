package com.piedrazul.msauthservice.domain.model.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Usada por el API Gateway para validar tokens
 * antes de enrutar requests a otros servicios.
 */
@Data
@Builder
public class TokenValidationResponse {
    private boolean valido;
    private Long usuarioId;
    private String login;
    private String rol;
}
