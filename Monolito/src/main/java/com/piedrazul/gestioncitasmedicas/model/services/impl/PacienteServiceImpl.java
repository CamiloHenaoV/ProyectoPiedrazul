package com.piedrazul.gestioncitasmedicas.model.services.impl;

import com.piedrazul.gestioncitasmedicas.model.dto.PacienteDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.Paciente;
import com.piedrazul.gestioncitasmedicas.model.entities.Usuario;
import com.piedrazul.gestioncitasmedicas.model.repositories.PacienteRepository;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IPacienteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;


@Service
public class PacienteServiceImpl implements IPacienteService {
    private final PacienteRepository pacienteRepository;

    public PacienteServiceImpl(PacienteRepository pacienteRepository){
        this.pacienteRepository=pacienteRepository;
    }

    @Override
    @Transactional
    public void crearPaciente(Usuario usuario, PacienteDTO dto) {
        pacienteRepository.save(Paciente.builder()
                .usuario(usuario)
                .nombreCompleto(dto.getNombreCompleto())
                .cedulaIdentidad(dto.getCedulaIdentidad())
                .fechaNacimiento(dto.getFechaNacimiento())
                .telefono(dto.getTelefono())
                .email(dto.getEmail())
                .direccion(dto.getDireccion())
                .creadoEn(ZonedDateTime.now())
                .build());
    }
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