package com.piedrazul.msscheduling.application.service.interfaces;

import com.piedrazul.msscheduling.domain.model.dto.DisponibilidadSemanalDTO;

import java.util.List;

public interface IDisponibilidadService {
    DisponibilidadSemanalDTO crear(DisponibilidadSemanalDTO dto);
    List<DisponibilidadSemanalDTO> listarPorProfesional(Long profesionalId);
    void eliminar(Long id);
}
