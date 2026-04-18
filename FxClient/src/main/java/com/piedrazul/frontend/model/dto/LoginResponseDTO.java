package com.piedrazul.frontend.model.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String    token;
    private UsuarioDTO usuario;
}
