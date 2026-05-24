// ─── UsuarioDTO.java ────────────────────────────────────────────────────────
package com.piedrazul.frontend.model.dto;

import com.piedrazul.frontend.model.enums.RolUsuario;
import lombok.*;

@Getter
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UsuarioDTO {
    private Long     id;
    private String    nombreCompleto;
    private String    login;
    private RolUsuario rol;
    private Boolean   activo;
}
