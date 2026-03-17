package com.piedrazul.gestioncitasmedicas.model.services.interfaces;

import com.piedrazul.gestioncitasmedicas.model.dto.ProfesionalDTO;
import java.util.List;
import java.util.UUID;

public interface IProfesionalService {
    List<ProfesionalDTO> listarActivos();
    ProfesionalDTO       buscarPorId(Integer id);
    List<ProfesionalDTO> listarActivosPorEspecialidad(String especialidadNombre);
}