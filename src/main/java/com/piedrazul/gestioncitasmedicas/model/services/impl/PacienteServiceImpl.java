package com.piedrazul.gestioncitasmedicas.model.services.impl;

import com.piedrazul.gestioncitasmedicas.model.dto.PacienteDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.Paciente;
import com.piedrazul.gestioncitasmedicas.model.repositories.PacienteRepository;
import com.piedrazul.gestioncitasmedicas.model.repositories.UsuarioRepository;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IPacienteService;
import java.util.UUID;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
public class PacienteServiceImpl implements IPacienteService {

    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository  usuarioRepository;

    public PacienteServiceImpl(PacienteRepository pacienteRepository,
                               UsuarioRepository  usuarioRepository) {
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository  = usuarioRepository;
    }

    @Override
    public PacienteDTO crearPaciente(UUID usuarioId, PacienteDTO dto) {
        var usuario = usuarioRepository.findById(usuarioId).orElseThrow();

        Paciente paciente = Paciente.builder()
                .usuario(usuario)
                .nombreCompleto(dto.getNombreCompleto())
                .cedulaIdentidad(dto.getCedulaIdentidad())
                .fechaNacimiento(dto.getFechaNacimiento())
                .telefono(dto.getTelefono())
                .email(dto.getEmail())
                .direccion(dto.getDireccion())
                .creadoEn(ZonedDateTime.now())
                .build();

        Paciente guardado = pacienteRepository.save(paciente);
        return toDTO(guardado);
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