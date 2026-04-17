package com.piedrazul.msnotifications.domain.model.dto;

import com.piedrazul.msnotifications.domain.model.entity.enums.CanalNotificacion;
import com.piedrazul.msnotifications.domain.model.entity.enums.EstadoNotificacion;
import lombok.*;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionDTO {

    private Long id;
    private String destinatario;
    private String asunto;
    private String cuerpo;
    private CanalNotificacion canal;
    private EstadoNotificacion estado;
    private String eventoOrigen;
    private String mensajeError;
    private ZonedDateTime creadoEn;
    private ZonedDateTime enviadoEn;
}
