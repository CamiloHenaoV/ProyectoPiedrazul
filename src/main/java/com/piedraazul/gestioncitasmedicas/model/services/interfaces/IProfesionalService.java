package com.piedraazul.gestioncitasmedicas.model.services.interfaces;

import com.piedraazul.gestioncitasmedicas.model.dto.ProfesionalDTO;
import java.util.List;

public interface IProfesionalService {
    List<ProfesionalDTO> listarActivos();
    ProfesionalDTO       buscarPorId(Integer id);
}