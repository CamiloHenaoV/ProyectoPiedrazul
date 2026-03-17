package com.piedrazul.gestioncitasmedicas.model.services.impl;

import com.piedrazul.gestioncitasmedicas.model.dto.ProfesionalDTO;
import com.piedrazul.gestioncitasmedicas.model.repositories.EspecialidadRepository;
import com.piedrazul.gestioncitasmedicas.model.repositories.ProfesionalRepository;
import com.piedrazul.gestioncitasmedicas.model.repositories.UsuarioRepository;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IProfesionalService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProfesionalServiceImpl implements IProfesionalService {

    private final ProfesionalRepository  profesionalRepository;
    private final UsuarioRepository      usuarioRepository;
    private final EspecialidadRepository especialidadRepository;

    public ProfesionalServiceImpl(ProfesionalRepository profesionalRepository,
                                  UsuarioRepository      usuarioRepository,
                                  EspecialidadRepository especialidadRepository) {
        this.profesionalRepository  = profesionalRepository;
        this.usuarioRepository      = usuarioRepository;
        this.especialidadRepository = especialidadRepository;
    }

    @Override
    public List<ProfesionalDTO> listarActivos() {
        return profesionalRepository.findByActivoTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProfesionalDTO buscarPorId(Integer id) {
        return profesionalRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow();
    }

    @Override
    public List<ProfesionalDTO> listarActivosPorEspecialidad(String especialidadNombre) {
        return profesionalRepository.findByEspecialidadNombreAndActivoTrue(especialidadNombre)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProfesionalDTO crearProfesional(UUID usuarioId, ProfesionalDTO dto) {
        var usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        var especialidad = especialidadRepository
                .findByNombre(dto.getEspecialidadNombre())
                .orElseThrow();

        com.piedrazul.gestioncitasmedicas.model.entities.Profesional profesional =
                com.piedrazul.gestioncitasmedicas.model.entities.Profesional.builder()
                        .usuario(usuario)
                        .tipo(dto.getTipo())
                        .especialidad(especialidad)
                        .licenciaProfesional(dto.getLicenciaProfesional())
                        .activo(true)
                        .build();

        return toDTO(profesionalRepository.save(profesional));
    }

    private ProfesionalDTO toDTO(com.piedrazul.gestioncitasmedicas.model.entities.Profesional p) {
        return ProfesionalDTO.builder()
                .id(p.getId())
                .nombreCompleto(p.getUsuario().getNombreCompleto())
                .tipo(p.getTipo())
                .especialidadNombre(p.getEspecialidad() != null ? p.getEspecialidad().getNombre() : "")
                .licenciaProfesional(p.getLicenciaProfesional())
                .activo(p.getActivo())
                .build();
    }
}