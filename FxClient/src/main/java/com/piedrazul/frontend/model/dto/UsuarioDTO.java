// ─── UsuarioDTO.java ────────────────────────────────────────────────────────
package com.piedrazul.frontend.model.dto;

import com.piedrazul.frontend.model.enums.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UsuarioDTO {
    private Long     id;
    private String    nombreCompleto;
    private String    login;
    private String    password;
    private RolUsuario rol;
    private Boolean   activo;
}
