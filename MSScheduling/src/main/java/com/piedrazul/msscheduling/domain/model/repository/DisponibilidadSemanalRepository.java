package com.piedrazul.msscheduling.domain.model.repository;

import com.piedrazul.msscheduling.domain.model.entity.DisponibilidadSemanal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisponibilidadSemanalRepository extends JpaRepository<DisponibilidadSemanal, Long> {
    List<DisponibilidadSemanal> findByProfesionalId(Long profesionalId);
    List<DisponibilidadSemanal> findByProfesionalIdAndDiaSemana(Long profesionalId, Integer diaSemana);
}
