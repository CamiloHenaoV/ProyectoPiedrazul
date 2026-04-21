package com.piedrazul.msscheduling.application.service.impl;

import com.piedrazul.msscheduling.application.service.interfaces.IDisponibilidadService;
import com.piedrazul.msscheduling.domain.model.dto.DisponibilidadSemanalDTO;
import com.piedrazul.msscheduling.domain.model.entity.DisponibilidadSemanal;
import com.piedrazul.msscheduling.domain.model.repository.DisponibilidadSemanalRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DisponibilidadServiceImpl implements IDisponibilidadService {

    private final DisponibilidadSemanalRepository disponibilidadRepository;

    public DisponibilidadServiceImpl(DisponibilidadSemanalRepository disponibilidadRepository) {
        this.disponibilidadRepository = disponibilidadRepository;
    }

    @Override
    public DisponibilidadSemanalDTO crear(DisponibilidadSemanalDTO dto) {
        DisponibilidadSemanal entidad = DisponibilidadSemanal.builder()
                .profesionalId(dto.getProfesionalId())
                .diaSemana(dto.getDiaSemana())
                .horaInicio(dto.getHoraInicio())
                .horaFin(dto.getHoraFin())
                .duracionCitaMinutos(dto.getDuracionCitaMinutos() != null ? dto.getDuracionCitaMinutos() : 30)
                .build();
        return toDTO(disponibilidadRepository.save(entidad));
    }

    @Override
    public List<DisponibilidadSemanalDTO> listarPorProfesional(Long profesionalId) {
        return disponibilidadRepository.findByProfesionalId(profesionalId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        disponibilidadRepository.deleteById(id);
    }

    private DisponibilidadSemanalDTO toDTO(DisponibilidadSemanal d) {
        return DisponibilidadSemanalDTO.builder()
                .id(d.getId())
                .profesionalId(d.getProfesionalId())
                .diaSemana(d.getDiaSemana())
                .horaInicio(d.getHoraInicio())
                .horaFin(d.getHoraFin())
                .duracionCitaMinutos(d.getDuracionCitaMinutos())
                .build();
    }
}
