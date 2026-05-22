package com.piedrazul.msscheduling.controller;

import com.piedrazul.msscheduling.application.service.interfaces.IConfiguracionAgendamientoService;
import com.piedrazul.msscheduling.domain.model.dto.ConfiguracionAgendamientoDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller para la configuración de la ventana de tiempo de agendamiento.
 *
 * HU-1.7: el administrador define cuántas semanas hacia adelante pueden
 *         reservarse citas.
 *
 * Rutas:
 *   GET  /api/scheduling/configuracion          → obtener configuración actual
 *   PUT  /api/scheduling/configuracion          → actualizar semanas habilitadas
 *   GET  /api/scheduling/configuracion/fecha-maxima → fecha máxima de agendamiento
 */
@RestController
@RequestMapping("/api/scheduling/configuracion")
public class ConfiguracionAgendamientoController {

    private final IConfiguracionAgendamientoService configuracionService;

    public ConfiguracionAgendamientoController(
            IConfiguracionAgendamientoService configuracionService) {
        this.configuracionService = configuracionService;
    }

    /**
     * HU-1.7: obtiene la configuración actual de la ventana de agendamiento.
     */
    @GetMapping
    public ResponseEntity<ConfiguracionAgendamientoDTO> obtener() {
        return ResponseEntity.ok(configuracionService.obtener());
    }

    /**
     * HU-1.7 SC-1 y SC-3: actualiza la ventana de tiempo.
     * Valida que semanasHabilitadas esté entre 1 y 52.
     */
    @PutMapping
    public ResponseEntity<ConfiguracionAgendamientoDTO> actualizar(
            @Valid @RequestBody ConfiguracionAgendamientoDTO dto) {
        return ResponseEntity.ok(configuracionService.actualizar(dto));
    }

    /**
     * HU-1.7 SC-2: endpoint para que el cliente consulte la fecha máxima
     * permitida para reservar citas (usado para bloquear el DatePicker).
     */
    @GetMapping("/fecha-maxima")
    public ResponseEntity<LocalDate> obtenerFechaMaxima() {
        return ResponseEntity.ok(configuracionService.obtenerFechaMaximaAgendamiento());
    }
}
