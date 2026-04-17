package com.piedrazul.msnotifications.application.service.interfaces;

import com.piedrazul.msnotifications.domain.model.dto.NotificacionDTO;
import com.piedrazul.msnotifications.domain.model.entity.enums.CanalNotificacion;
import com.piedrazul.msnotifications.domain.model.entity.enums.EstadoNotificacion;

import java.util.List;

public interface INotificacionService {

    NotificacionDTO enviar(String destinatario, String asunto, String cuerpo,
                           CanalNotificacion canal, String eventoOrigen);

    List<NotificacionDTO> listarTodas();

    List<NotificacionDTO> listarPorEstado(EstadoNotificacion estado);

    NotificacionDTO buscarPorId(Long id);
}
