package com.piedrazul.msusermanagement.controller;

import com.piedrazul.msusermanagement.application.service.interfaces.IEspecialidadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users/especialidades")
public class EspecialidadController {

    private final IEspecialidadService especialidadService;

    public EspecialidadController(IEspecialidadService especialidadService) {
        this.especialidadService = especialidadService;
    }

    @GetMapping
    public ResponseEntity<List<String>> listar() {
        return ResponseEntity.ok(especialidadService.listarNombres());
    }
}