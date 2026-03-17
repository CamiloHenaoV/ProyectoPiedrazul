package com.piedrazul.gestioncitasmedicas.model.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacienteDTO {
    private UUID id;
    private String nombreCompleto;
    private String cedulaIdentidad;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String email;
    private String direccion;
}
