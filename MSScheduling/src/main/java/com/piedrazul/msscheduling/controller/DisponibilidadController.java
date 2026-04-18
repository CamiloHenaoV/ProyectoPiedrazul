package com.piedrazul.msscheduling.controller;

import com.piedrazul.msscheduling.application.service.interfaces.IDisponibilidadService;
import com.piedrazul.msscheduling.domain.model.dto.DisponibilidadSemanalDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduling/disponibilidad")
public class DisponibilidadController {

    private final IDisponibilidadService disponibilidadService;

    public DisponibilidadController(IDisponibilidadService disponibilidadService) {
        this.disponibilidadService = disponibilidadService;
    }

    @PostMapping
    public ResponseEntity<DisponibilidadSemanalDTO> crear(@RequestBody DisponibilidadSemanalDTO dto) {
        return ResponseEntity.ok(disponibilidadService.crear(dto));
    }

    @GetMapping("/profesional/{profesionalId}")
    public ResponseEntity<List<DisponibilidadSemanalDTO>> listarPorProfesional(@PathVariable Long profesionalId) {
        return ResponseEntity.ok(disponibilidadService.listarPorProfesional(profesionalId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        disponibilidadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
