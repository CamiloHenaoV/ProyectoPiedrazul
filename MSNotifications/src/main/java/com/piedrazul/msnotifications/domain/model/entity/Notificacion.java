package com.piedrazul.msnotifications.domain.model.entity;

import com.piedrazul.msnotifications.domain.model.entity.enums.CanalNotificacion;
import com.piedrazul.msnotifications.domain.model.entity.enums.EstadoNotificacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String destinatario;

    @Column(nullable = false, length = 200)
    private String asunto;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String cuerpo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private CanalNotificacion canal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private EstadoNotificacion estado;

    @Column(name = "evento_origen", nullable = false, length = 100)
    private String eventoOrigen;

    @Column(name = "mensaje_error", columnDefinition = "TEXT")
    private String mensajeError;

    @Column(name = "creado_en", nullable = false)
    private ZonedDateTime creadoEn;

    @Column(name = "enviado_en")
    private ZonedDateTime enviadoEn;

    public static Notificacion crear(String destinatario, String asunto, String cuerpo,
                                     CanalNotificacion canal, String eventoOrigen) {
        return Notificacion.builder()
                .destinatario(destinatario)
                .asunto(asunto)
                .cuerpo(cuerpo)
                .canal(canal)
                .eventoOrigen(eventoOrigen)
                .estado(EstadoNotificacion.PENDIENTE)
                .creadoEn(ZonedDateTime.now())
                .build();
    }

    public void marcarEnviada() {
        this.estado = EstadoNotificacion.ENVIADA;
        this.enviadoEn = ZonedDateTime.now();
    }

    public void marcarFallida(String razon) {
        this.estado = EstadoNotificacion.FALLIDA;
        this.mensajeError = razon;
    }
}
