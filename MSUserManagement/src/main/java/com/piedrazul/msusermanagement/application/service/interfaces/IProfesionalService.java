package com.piedrazul.msusermanagement.application.service.interfaces;


import com.piedrazul.msusermanagement.domain.model.dto.ProfesionalDTO;
import com.piedrazul.msusermanagement.domain.model.entity.Profesional;
import com.piedrazul.msusermanagement.domain.model.entity.Usuario;

import java.util.List;

public interface IProfesionalService {
    Profesional crearProfesional(Usuario usuario, ProfesionalDTO dto);
    List<ProfesionalDTO> listarActivos();
    ProfesionalDTO buscarPorId(Long id);
    List<ProfesionalDTO> listarActivosPorEspecialidad(String especialidadNombre);
}