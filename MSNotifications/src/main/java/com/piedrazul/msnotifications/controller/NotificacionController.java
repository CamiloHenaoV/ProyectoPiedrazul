package com.piedrazul.msnotifications.controller;

import com.piedrazul.msnotifications.application.service.interfaces.INotificacionService;
import com.piedrazul.msnotifications.domain.model.dto.NotificacionDTO;
import com.piedrazul.msnotifications.domain.model.entity.enums.EstadoNotificacion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final INotificacionService notificacionService;

    public NotificacionController(INotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping
    public ResponseEntity<List<NotificacionDTO>> listarTodas() {
        return ResponseEntity.ok(notificacionService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificacionDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(notificacionService.buscarPorId(id));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<NotificacionDTO>> listarPorEstado(@PathVariable EstadoNotificacion estado) {
        return ResponseEntity.ok(notificacionService.listarPorEstado(estado));
    }
}
