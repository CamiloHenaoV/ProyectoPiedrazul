package com.piedraazul.gestioncitasmedicas.model.repositories;

import com.piedraazul.gestioncitasmedicas.model.entities.DisponibilidadSemanal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisponibilidadSemanalRepository extends JpaRepository<DisponibilidadSemanal, Integer> {
    List<DisponibilidadSemanal> findByProfesionalId(Integer profesionalId);
    List<DisponibilidadSemanal> findByProfesionalIdAndDiaSemana(Integer profesionalId, Integer diaSemana);
}