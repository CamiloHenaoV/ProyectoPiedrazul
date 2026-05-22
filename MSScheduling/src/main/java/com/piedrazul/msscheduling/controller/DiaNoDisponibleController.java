package com.piedrazul.msscheduling.controller;

import com.piedrazul.msscheduling.application.service.interfaces.IDiaNoDisponibleService;
import com.piedrazul.msscheduling.domain.model.dto.DiaNoDisponibleDTO;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller para la gestión de días no disponibles y festivos.
 * HU-1.8: el administrador gestiona fechas que impiden el agendamiento global.
 * Rutas:
 *   POST   /api/scheduling/dias-no-disponibles             → registrar fecha bloqueada (SC-1)
 *   GET    /api/scheduling/dias-no-disponibles             → listar todos
 *   GET    /api/scheduling/dias-no-disponibles/rango       → listar en rango de fechas
 *   DELETE /api/scheduling/dias-no-disponibles/{id}        → eliminar restricción (SC-3)
 */
@RestController
@RequestMapping("/api/scheduling/dias-no-disponibles")
public class DiaNoDisponibleController {

    private final IDiaNoDisponibleService diaNoDisponibleService;

    public DiaNoDisponibleController(IDiaNoDisponibleService diaNoDisponibleService) {
        this.diaNoDisponibleService = diaNoDisponibleService;
    }

    /**
     * HU-1.8 SC-1: registra un día no disponible.
     * El sistema bloquea el agendamiento en esa fecha para todos los profesionales.
     */
    @PostMapping
    public ResponseEntity<DiaNoDisponibleDTO> registrar(
            @Valid @RequestBody DiaNoDisponibleDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(diaNoDisponibleService.registrar(dto));
    }

    /**
     * Lista todos los días no disponibles registrados.
     */
    @GetMapping
    public ResponseEntity<List<DiaNoDisponibleDTO>> listarTodos() {
        return ResponseEntity.ok(diaNoDisponibleService.listarTodos());
    }

    /**
     * Lista días no disponibles dentro de un rango (útil para el calendario UI).
     * Ejemplo: GET /api/scheduling/dias-no-disponibles/rango?desde=2025-12-01&hasta=2025-12-31
     */
    @GetMapping("/rango")
    public ResponseEntity<List<DiaNoDisponibleDTO>> listarEnRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(diaNoDisponibleService.listarEnRango(desde, hasta));
    }

    /**
     * HU-1.8 SC-3: elimina la restricción de una fecha (la habilita nuevamente).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        diaNoDisponibleService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
