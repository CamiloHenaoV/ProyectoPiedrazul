package com.piedrazul.gestioncitasmedicas.model.services.impl;

import com.piedrazul.gestioncitasmedicas.model.dto.PacienteDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.Paciente;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IPacienteService;
import org.springframework.stereotype.Service;


@Service
public class PacienteServiceImpl implements IPacienteService {

    private PacienteDTO toDTO(Paciente p) {
        return PacienteDTO.builder()
                .id(p.getId())
                .nombreCompleto(p.getNombreCompleto())
                .cedulaIdentidad(p.getCedulaIdentidad())
                .fechaNacimiento(p.getFechaNacimiento())
                .telefono(p.getTelefono())
                .email(p.getEmail())
                .direccion(p.getDireccion())
                .build();
    }
}