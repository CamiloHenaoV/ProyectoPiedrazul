package com.piedrazul.frontend.model.dto;

import com.piedrazul.frontend.model.enums.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRegistroDTO {
        private String nombreCompleto;
        private String login;
        private String password;
        private RolUsuario rol;
}
