package com.piedrazul.msusermanagement.controller;

import com.piedrazul.msusermanagement.application.service.interfaces.IProfesionalService;
import com.piedrazul.msusermanagement.domain.model.dto.ProfesionalDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/profesionales")
public class ProfesionalController {

    private final IProfesionalService profesionalService;

    public ProfesionalController(IProfesionalService profesionalService) {
        this.profesionalService = profesionalService;
    }

    @GetMapping
    public ResponseEntity<List<ProfesionalDTO>> listar(
            @RequestParam(required = false) String especialidad) {
        if (especialidad != null) {
            return ResponseEntity.ok(profesionalService.listarActivosPorEspecialidad(especialidad));
        }
        return ResponseEntity.ok(profesionalService.listarActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfesionalDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(profesionalService.buscarPorId(id));
    }
}