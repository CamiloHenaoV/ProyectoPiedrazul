package com.piedrazul.msscheduling.domain.model.repository;

import com.piedrazul.msscheduling.domain.model.entity.DiaNoDisponible;
import com.piedrazul.msscheduling.domain.model.entity.enums.TipoDiaNoDisponible;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para días no disponibles (festivos y bloqueos manuales).
 *
 * HU-1.8: gestión de fechas que impiden el agendamiento a nivel global.
 */
public interface DiaNoDisponibleRepository extends JpaRepository<DiaNoDisponible, Long> {

    /** Busca un día no disponible por fecha exacta. */
    Optional<DiaNoDisponible> findByFecha(LocalDate fecha);

    /** Verifica si una fecha está bloqueada (cualquier tipo). */
    boolean existsByFecha(LocalDate fecha);

    /** Lista todos los días de un tipo específico (FESTIVO o BLOQUEO_MANUAL). */
    List<DiaNoDisponible> findByTipo(TipoDiaNoDisponible tipo);

    /** Lista todos los días no disponibles dentro de un rango de fechas. */
    List<DiaNoDisponible> findByFechaBetween(LocalDate desde, LocalDate hasta);
}
