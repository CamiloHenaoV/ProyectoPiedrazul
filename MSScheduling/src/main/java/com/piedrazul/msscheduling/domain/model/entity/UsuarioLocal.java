package com.piedrazul.msscheduling.domain.model.entity;

import com.piedrazul.msscheduling.domain.model.entity.enums.RolUsuario;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioLocal {

    @Id
    private Long id;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(nullable = false, length = 50)
    private String login;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    private RolUsuario rol;

    @Column(nullable = false)
    private Boolean activo = true;
}
