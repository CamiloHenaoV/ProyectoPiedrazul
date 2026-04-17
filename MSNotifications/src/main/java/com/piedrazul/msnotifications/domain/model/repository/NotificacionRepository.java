package com.piedrazul.msnotifications.domain.model.repository;

import com.piedrazul.msnotifications.domain.model.entity.Notificacion;
import com.piedrazul.msnotifications.domain.model.entity.enums.EstadoNotificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByEstado(EstadoNotificacion estado);

    List<Notificacion> findByEventoOrigen(String eventoOrigen);
}
