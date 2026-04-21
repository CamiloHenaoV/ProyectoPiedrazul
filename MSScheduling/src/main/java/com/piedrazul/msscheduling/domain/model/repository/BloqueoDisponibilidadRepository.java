package com.piedrazul.msscheduling.domain.model.repository;

import com.piedrazul.msscheduling.domain.model.entity.BloqueoDisponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface BloqueoDisponibilidadRepository extends JpaRepository<BloqueoDisponibilidad, UUID> {
    List<BloqueoDisponibilidad> findByProfesionalId(Long profesionalId);

    @Query("SELECT COUNT(b) > 0 FROM BloqueoDisponibilidad b " +
           "WHERE b.profesionalId = :profesionalId " +
           "AND :fechaHora BETWEEN b.fechaInicio AND b.fechaFin")
    boolean existeBloqueoEnFecha(@Param("profesionalId") Long profesionalId,
                                  @Param("fechaHora") ZonedDateTime fechaHora);
}
