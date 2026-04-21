package com.piedrazul.msusermanagement.domain.model.dto.request;

import com.piedrazul.msusermanagement.domain.model.dto.ProfesionalDTO;
import com.piedrazul.msusermanagement.domain.model.dto.UsuarioDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegistroProfesionalRequest {
    private UsuarioDTO usuario;
    private ProfesionalDTO profesional;
}