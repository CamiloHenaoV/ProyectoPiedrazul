package com.piedrazul.msauthservice.domain.model.dto.response;

import com.piedrazul.msauthservice.domain.model.entity.enums.RolUsuario;
import lombok.Data;

/**
 * Respuesta del Feign client hacia ms-usuario-service.
 * Mapea solo los campos que auth-service necesita.
 */
@Data
public class UsuarioClientResponse {
    private Long id;
    private String login;
    private String nombreCompleto;
    private RolUsuario rol;
    private Boolean activo;
}
