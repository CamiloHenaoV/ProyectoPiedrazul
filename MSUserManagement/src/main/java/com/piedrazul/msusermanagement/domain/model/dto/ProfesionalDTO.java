package com.piedrazul.msusermanagement.domain.model.dto;


import com.piedrazul.msusermanagement.domain.model.entity.enums.TipoProfesional;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfesionalDTO {
    private Long id;
    private String nombreCompleto;
    private TipoProfesional tipo;
    private String especialidadNombre;
    private String licenciaProfesional;
    private Boolean activo;
    private Integer         duracionCitaMinutos;
}