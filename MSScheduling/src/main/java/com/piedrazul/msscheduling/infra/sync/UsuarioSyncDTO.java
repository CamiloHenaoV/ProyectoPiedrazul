package com.piedrazul.msscheduling.infra.sync;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioSyncDTO {
    private Long id;
    private String nombreCompleto;
    private String login;
    private String rol;
    private Boolean activo;
}
