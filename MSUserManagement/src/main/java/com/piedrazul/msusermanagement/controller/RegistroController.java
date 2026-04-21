package com.piedrazul.msusermanagement.controller;

import com.piedrazul.msusermanagement.application.service.interfaces.IRegistroService;
import com.piedrazul.msusermanagement.domain.model.dto.UsuarioDTO;
import com.piedrazul.msusermanagement.domain.model.dto.request.RegistroPacienteRequest;
import com.piedrazul.msusermanagement.domain.model.dto.request.RegistroProfesionalRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/registro")
public class RegistroController {

    private final IRegistroService registroService;

    public RegistroController(IRegistroService registroService) {
        this.registroService = registroService;
    }

    @PostMapping("/usuario")
    public ResponseEntity<UsuarioDTO> registrarUsuario(@RequestBody UsuarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registroService.registrarUsuario(dto));
    }

    @PostMapping("/paciente")
    public ResponseEntity<UsuarioDTO> registrarPaciente(@RequestBody RegistroPacienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registroService.registrarPaciente(request.getUsuario(), request.getPaciente()));
    }

    @PostMapping("/profesional")
    public ResponseEntity<UsuarioDTO> registrarProfesional(@RequestBody RegistroProfesionalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registroService.registrarProfesional(request.getUsuario(), request.getProfesional()));
    }
}