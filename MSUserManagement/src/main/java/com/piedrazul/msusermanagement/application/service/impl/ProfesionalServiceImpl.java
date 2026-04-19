package com.piedrazul.msusermanagement.application.service.impl;


import com.piedrazul.msusermanagement.application.service.interfaces.IProfesionalService;
import com.piedrazul.msusermanagement.domain.model.dto.ProfesionalDTO;
import com.piedrazul.msusermanagement.domain.model.entity.Profesional;
import com.piedrazul.msusermanagement.domain.model.entity.Usuario;
import com.piedrazul.msusermanagement.domain.model.repository.EspecialidadRepository;
import com.piedrazul.msusermanagement.domain.model.repository.ProfesionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfesionalServiceImpl implements IProfesionalService {

    private final ProfesionalRepository profesionalRepository;
    private final EspecialidadRepository especialidadRepository;

    public ProfesionalServiceImpl(ProfesionalRepository profesionalRepository,
                                  EspecialidadRepository especialidadRepository) {
        this.profesionalRepository  = profesionalRepository;
        this.especialidadRepository=especialidadRepository;
    }

    @Override
    @Transactional
    public Profesional crearProfesional(Usuario usuario, ProfesionalDTO dto) {

        var especialidad = especialidadRepository
                .findByNombre(dto.getEspecialidadNombre())
                .orElseThrow();

        return profesionalRepository.save(
                        Profesional.builder()
                        .usuario(usuario)
                        .tipo(dto.getTipo())
                        .especialidad(especialidad)
                        .licenciaProfesional(dto.getLicenciaProfesional())
                        .activo(true)
                        .build()
        );
    }

    @Override
    public List<ProfesionalDTO> listarActivos() {
        return profesionalRepository.findByActivoTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProfesionalDTO buscarPorId(Long id) {
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


    private ProfesionalDTO toDTO(Profesional p) {
        return ProfesionalDTO.builder()
                .id(p.getId())
                .nombreCompleto(p.getUsuario().getNombreCompleto())
                .tipo(p.getTipo())
                .especialidadNombre(p.getEspecialidad() != null ? p.getEspecialidad().getNombre() : "")
                .licenciaProfesional(p.getLicenciaProfesional())
                .activo(p.getActivo())
                .duracionCitaMinutos(p.getDuracionCitaMinutos())
                .build();
    }
}