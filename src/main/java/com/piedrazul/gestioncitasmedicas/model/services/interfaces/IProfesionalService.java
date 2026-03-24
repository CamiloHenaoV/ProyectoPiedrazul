package com.piedrazul.gestioncitasmedicas.model.services.interfaces;

import com.piedrazul.gestioncitasmedicas.model.dto.ProfesionalDTO;
import java.util.List;

public interface IProfesionalService {
    List<ProfesionalDTO> listarActivos();
    ProfesionalDTO       buscarPorId(Integer id);
    List<ProfesionalDTO> listarActivosPorEspecialidad(String especialidadNombre);
}