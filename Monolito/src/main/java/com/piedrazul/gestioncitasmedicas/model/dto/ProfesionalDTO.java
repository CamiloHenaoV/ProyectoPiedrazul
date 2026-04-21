package com.piedrazul.gestioncitasmedicas.model.dto;

import com.piedrazul.gestioncitasmedicas.model.entities.enums.TipoProfesional;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfesionalDTO {
    private Integer id;
    private String nombreCompleto;
    private TipoProfesional tipo;
    private String especialidadNombre;
    private String licenciaProfesional;
    private Boolean activo;
}