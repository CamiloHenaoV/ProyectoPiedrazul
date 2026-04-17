package com.piedrazul.msnotifications.application.service.impl;

import com.piedrazul.msnotifications.application.service.interfaces.INotificacionService;
import com.piedrazul.msnotifications.domain.model.dto.NotificacionDTO;
import com.piedrazul.msnotifications.domain.model.entity.Notificacion;
import com.piedrazul.msnotifications.domain.model.entity.enums.CanalNotificacion;
import com.piedrazul.msnotifications.domain.model.entity.enums.EstadoNotificacion;
import com.piedrazul.msnotifications.domain.model.exceptions.NotificacionNoEncontradaException;
import com.piedrazul.msnotifications.domain.model.repository.NotificacionRepository;
import com.piedrazul.msnotifications.infra.messaging.EmailSenderAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificacionServiceImpl implements INotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final EmailSenderAdapter emailSenderAdapter;

    public NotificacionServiceImpl(NotificacionRepository notificacionRepository,
                                   EmailSenderAdapter emailSenderAdapter) {
        this.notificacionRepository = notificacionRepository;
        this.emailSenderAdapter = emailSenderAdapter;
    }

    @Override
    public NotificacionDTO enviar(String destinatario, String asunto, String cuerpo,
                                  CanalNotificacion canal, String eventoOrigen) {

        Notificacion notificacion = Notificacion.crear(destinatario, asunto, cuerpo, canal, eventoOrigen);
        notificacionRepository.save(notificacion);

        try {
            if (canal == CanalNotificacion.EMAIL) {
                emailSenderAdapter.enviar(notificacion);
            }
            notificacion.marcarEnviada();
            log.info("Notificación enviada a {} por evento {}", destinatario, eventoOrigen);
        } catch (Exception ex) {
            notificacion.marcarFallida(ex.getMessage());
            log.error("Error al enviar notificación a {}: {}", destinatario, ex.getMessage());
        }

        return toDTO(notificacionRepository.save(notificacion));
    }

    @Override
    public List<NotificacionDTO> listarTodas() {
        return notificacionRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificacionDTO> listarPorEstado(EstadoNotificacion estado) {
        return notificacionRepository.findByEstado(estado)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public NotificacionDTO buscarPorId(Long id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new NotificacionNoEncontradaException(id.toString()));
        return toDTO(notificacion);
    }

    private NotificacionDTO toDTO(Notificacion n) {
        return NotificacionDTO.builder()
                .id(n.getId())
                .destinatario(n.getDestinatario())
                .asunto(n.getAsunto())
                .cuerpo(n.getCuerpo())
                .canal(n.getCanal())
                .estado(n.getEstado())
                .eventoOrigen(n.getEventoOrigen())
                .mensajeError(n.getMensajeError())
                .creadoEn(n.getCreadoEn())
                .enviadoEn(n.getEnviadoEn())
                .build();
    }
}
