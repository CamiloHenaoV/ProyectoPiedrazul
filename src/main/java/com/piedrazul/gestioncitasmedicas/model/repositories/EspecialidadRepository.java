package com.piedrazul.gestioncitasmedicas.model.repositories;

import com.piedrazul.gestioncitasmedicas.model.entities.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Integer> {
}