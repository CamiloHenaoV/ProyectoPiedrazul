package com.piedrazul.msusermanagement.domain.model.repository;


import com.piedrazul.msusermanagement.domain.model.entity.Profesional;
import com.piedrazul.msusermanagement.domain.model.entity.enums.TipoProfesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfesionalRepository extends JpaRepository<Profesional, Long> {
    List<Profesional> findByActivoTrue();
    List<Profesional> findByTipoAndActivoTrue(TipoProfesional tipo);
    boolean           existsByLicenciaProfesional(String licencia);
    List<Profesional> findByEspecialidadNombreAndActivoTrue(String nombre);
}