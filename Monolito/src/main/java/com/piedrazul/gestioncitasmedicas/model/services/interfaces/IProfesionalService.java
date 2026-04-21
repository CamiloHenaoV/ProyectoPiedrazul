package com.piedrazul.gestioncitasmedicas.model.services.interfaces;

import com.piedrazul.gestioncitasmedicas.model.dto.ProfesionalDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.Profesional;
import com.piedrazul.gestioncitasmedicas.model.entities.Usuario;

import java.util.List;

public interface IProfesionalService {
    Profesional crearProfesional(Usuario usuario, ProfesionalDTO dto);
    List<ProfesionalDTO> listarActivos();
    ProfesionalDTO       buscarPorId(Integer id);
    List<ProfesionalDTO> listarActivosPorEspecialidad(String especialidadNombre);
}