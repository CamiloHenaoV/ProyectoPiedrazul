package com.piedrazul.frontend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PacienteDTO {
    private Long id;
    private String    nombreCompleto;
    private String    cedulaIdentidad;
    private LocalDate fechaNacimiento;
    private String    telefono;
    private String    email;
    private String    direccion;
}
