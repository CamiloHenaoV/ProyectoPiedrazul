package com.piedraazul.gestioncitasmedicas.model.repositories;

import com.piedraazul.gestioncitasmedicas.model.entities.BloqueoDisponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BloqueoDisponibilidadRepository extends JpaRepository<BloqueoDisponibilidad, UUID> {
    List<BloqueoDisponibilidad> findByProfesionalId(Integer profesionalId);

    @Query("SELECT COUNT(b) > 0 FROM BloqueoDisponibilidad b " +
            "WHERE b.profesional.id = :profesionalId " +
            "AND :fechaHora BETWEEN b.fechaInicio AND b.fechaFin")
    boolean existeBloqueoEnFecha(
            @Param("profesionalId") Integer profesionalId,
            @Param("fechaHora") ZonedDateTime fechaHora
    );
}