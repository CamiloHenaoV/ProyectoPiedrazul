package com.piedrazul.gestioncitasmedicas.model.dto;

import com.piedrazul.gestioncitasmedicas.model.entities.enums.RolUsuario;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {
    private UUID id;
    private String nombreCompleto;
    private String login;
    private String password;        // solo en creación
    private RolUsuario rol;
    private Boolean activo;
}
