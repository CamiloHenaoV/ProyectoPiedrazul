package com.piedrazul.msusermanagement.domain.model.dto.request;

import com.piedrazul.msusermanagement.domain.model.dto.PacienteDTO;
import com.piedrazul.msusermanagement.domain.model.dto.UsuarioDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegistroPacienteRequest {
    private UsuarioDTO usuario;
    private PacienteDTO paciente;
}