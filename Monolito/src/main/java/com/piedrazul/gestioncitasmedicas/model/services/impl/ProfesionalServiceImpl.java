package com.piedrazul.gestioncitasmedicas.model.services.impl;

import com.piedrazul.gestioncitasmedicas.model.dto.ProfesionalDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.DisponibilidadSemanal;
import com.piedrazul.gestioncitasmedicas.model.entities.Profesional;
import com.piedrazul.gestioncitasmedicas.model.entities.Usuario;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.TipoProfesional;
import com.piedrazul.gestioncitasmedicas.model.repositories.DisponibilidadSemanalRepository;
import com.piedrazul.gestioncitasmedicas.model.repositories.EspecialidadRepository;
import com.piedrazul.gestioncitasmedicas.model.repositories.ProfesionalRepository;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IProfesionalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfesionalServiceImpl implements IProfesionalService {

    private final ProfesionalRepository  profesionalRepository;
    private final EspecialidadRepository especialidadRepository;
    private final DisponibilidadSemanalRepository disponibilidadRepository;

    public ProfesionalServiceImpl(ProfesionalRepository profesionalRepository,
                                  EspecialidadRepository especialidadRepository,
                                  DisponibilidadSemanalRepository disponibilidadRepository) {
        this.profesionalRepository  = profesionalRepository;
        this.especialidadRepository=especialidadRepository;
        this.disponibilidadRepository=disponibilidadRepository;
    }

    @Override
    @Transactional
    public Profesional crearProfesional(Usuario usuario, ProfesionalDTO dto) {

        var especialidad = especialidadRepository
                .findByNombre(dto.getEspecialidadNombre())
                .orElseThrow();

        Profesional profesional = profesionalRepository.save(
                com.piedrazul.gestioncitasmedicas.model.entities.Profesional.builder()
                        .usuario(usuario)
                        .tipo(dto.getTipo())
                        .especialidad(especialidad)
                        .licenciaProfesional(dto.getLicenciaProfesional())
                        .activo(true)
                        .build()
        );

        crearDisponibilidadPorDefecto(profesional);

        return profesional;
    }
    private static final int[] DIAS_HABILES = {1, 2, 3, 4, 5};

    private void crearDisponibilidadPorDefecto(Profesional profesional) {
        int duracion = profesional.getTipo() == TipoProfesional.medico ? 5 : 20;

        for (int dia : DIAS_HABILES) {
            disponibilidadRepository.save(DisponibilidadSemanal.builder()
                    .profesional(profesional)
                    .diaSemana(dia)
                    .horaInicio(LocalTime.of(7, 0))
                    .horaFin(LocalTime.of(14, 0))
                    .duracionCitaMinutos(duracion)
                    .build());
        }
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