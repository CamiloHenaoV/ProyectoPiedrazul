package com.piedrazul.msusermanagement.domain.model.repository;


import com.piedrazul.msusermanagement.domain.model.entity.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Integer> {
    Optional<Especialidad> findByNombre(String nombre);
}