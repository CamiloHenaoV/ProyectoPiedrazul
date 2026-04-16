package com.piedrazul.msusermanagement.domain.model.dto;

import com.piedrazul.msusermanagement.domain.model.entity.enums.RolUsuario;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {
    private Long id;
    private String nombreCompleto;
    private String login;
    private String password;
    private RolUsuario rol;
    private Boolean activo;
}
