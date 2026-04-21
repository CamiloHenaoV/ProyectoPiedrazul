package com.piedrazul.msusermanagement.application.service.impl;

import com.piedrazul.msusermanagement.application.service.interfaces.IPacienteService;
import com.piedrazul.msusermanagement.domain.model.dto.PacienteDTO;
import com.piedrazul.msusermanagement.domain.model.entity.Paciente;
import com.piedrazul.msusermanagement.domain.model.entity.Usuario;
import com.piedrazul.msusermanagement.domain.model.repository.PacienteRepository;
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