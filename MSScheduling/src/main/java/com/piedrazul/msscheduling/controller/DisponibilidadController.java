package com.piedrazul.msscheduling.controller;

import com.piedrazul.msscheduling.application.service.interfaces.IDisponibilidadService;
import com.piedrazul.msscheduling.domain.model.dto.DisponibilidadSemanalDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller para la gestión de disponibilidad semanal de profesionales.
 * HU-1.5: configurar días habilitados y franjas horarias.
 * HU-1.6: definir intervalos de atención entre citas.
 * Rutas:
 *   POST   /api/scheduling/disponibilidad                    → crear
 *   PUT    /api/scheduling/disponibilidad/{id}               → actualizar (HU-1.6 SC-3)
 *   GET    /api/scheduling/disponibilidad/profesional/{id}   → listar por profesional
 *   DELETE /api/scheduling/disponibilidad/{id}               → eliminar
 */
@RestController
@RequestMapping("/api/scheduling/disponibilidad")
public class DisponibilidadController {

    private final IDisponibilidadService disponibilidadService;

    public DisponibilidadController(IDisponibilidadService disponibilidadService) {
        this.disponibilidadService = disponibilidadService;
    }

    /**
     * HU-1.5 SC-1/SC-2/SC-3/SC-4: crear disponibilidad.
     * @Valid activa las validaciones de bean (campos obligatorios, rangos).
     */
    @PostMapping
    public ResponseEntity<DisponibilidadSemanalDTO> crear(
            @Valid @RequestBody DisponibilidadSemanalDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(disponibilidadService.crear(dto));
    }

    /**
     * HU-1.6 SC-3: actualizar disponibilidad (recalcula respetando citas futuras).
     */
    @PutMapping("/{id}")
    public ResponseEntity<DisponibilidadSemanalDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DisponibilidadSemanalDTO dto) {
        return ResponseEntity.ok(disponibilidadService.actualizar(id, dto));
    }

    /**
     * Lista todas las disponibilidades configuradas para un profesional.
     */
    @GetMapping("/profesional/{profesionalId}")
    public ResponseEntity<List<DisponibilidadSemanalDTO>> listarPorProfesional(
            @PathVariable Long profesionalId) {
        return ResponseEntity.ok(disponibilidadService.listarPorProfesional(profesionalId));
    }

    /**
     * Elimina una configuración de disponibilidad.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        disponibilidadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
