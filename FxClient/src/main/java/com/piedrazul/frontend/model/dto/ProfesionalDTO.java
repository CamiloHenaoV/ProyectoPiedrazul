package com.piedrazul.frontend.model.dto;

import com.piedrazul.frontend.model.enums.TipoProfesional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProfesionalDTO {
    private Long    id;
    private Boolean activo;
    private String          nombreCompleto;
    private String          especialidadNombre;
    private String          licenciaProfesional;
    private TipoProfesional tipo;
    private Integer         duracionCitaMinutos;
}
