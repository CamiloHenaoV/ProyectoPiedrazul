package com.piedrazul.msscheduling.domain.model.repository;

import com.piedrazul.msscheduling.domain.model.entity.UsuarioLocal;
import com.piedrazul.msscheduling.domain.model.entity.enums.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioLocalRepository extends JpaRepository<UsuarioLocal, Long> {
    List<UsuarioLocal> findByRol(RolUsuario rol);
    List<UsuarioLocal> findByActivoTrue();
}
