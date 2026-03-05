package com.piedraazul.gestioncitasmedicas.model.dto;

import com.piedraazul.gestioncitasmedicas.model.entities.enums.TipoProfesional;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProfesionalDTO {
    private Integer         id;
    private String          nombreCompleto;
    private TipoProfesional tipo;
    private String          especialidadNombre;
    private String          licenciaProfesional;
    private Boolean         activo;
}