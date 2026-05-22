package com.piedrazul.msscheduling.domain.model.repository;

import com.piedrazul.msscheduling.domain.model.entity.ConfiguracionAgendamiento;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para la configuración global de agendamiento.
 *
 * Solo existe un registro (id = 1). Se usa findById(1L) para obtenerlo
 * y save() para actualizarlo.
 */
public interface ConfiguracionAgendamientoRepository
        extends JpaRepository<ConfiguracionAgendamiento, Long> {
}
