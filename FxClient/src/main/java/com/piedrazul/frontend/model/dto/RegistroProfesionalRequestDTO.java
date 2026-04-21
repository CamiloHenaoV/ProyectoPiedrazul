package com.piedrazul.frontend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class RegistroProfesionalRequestDTO {
    private UsuarioDTO usuario;
    private ProfesionalDTO profesional;
}
