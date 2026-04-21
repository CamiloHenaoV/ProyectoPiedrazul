package com.piedrazul.frontend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class RegistroPacienteRequestDTO {
    private UsuarioDTO usuario;
    private PacienteDTO paciente;
}
