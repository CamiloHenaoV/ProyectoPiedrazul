package com.piedrazul.gestioncitasmedicas.model.repositories;

import com.piedrazul.gestioncitasmedicas.model.entities.Profesional;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.TipoProfesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfesionalRepository extends JpaRepository<Profesional, Integer> {
    List<Profesional> findByActivoTrue();
    List<Profesional> findByTipoAndActivoTrue(TipoProfesional tipo);
    List<Profesional> findByEspecialidadId(Integer especialidadId);
    boolean           existsByLicenciaProfesional(String licencia);
}