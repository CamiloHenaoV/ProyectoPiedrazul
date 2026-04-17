package com.piedrazul.msusermanagement.controller;

import com.piedrazul.msusermanagement.application.service.interfaces.IUsuarioService;
import com.piedrazul.msusermanagement.domain.model.dto.UsuarioDTO;
import com.piedrazul.msusermanagement.domain.model.entity.enums.RolUsuario;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/usuarios")
public class UsuarioController {

    private final IUsuarioService usuarioService;

    public UsuarioController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listar(@RequestParam(required = false) RolUsuario rol) {
        if (rol != null) {
            return ResponseEntity.ok(usuarioService.listarPorRol(rol));
        }
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/activos/count")
    public ResponseEntity<Map<String, Long>> contarActivos() {
        return ResponseEntity.ok(Map.of("total", usuarioService.contarUsuariosActivos()));
    }

    @GetMapping("/{usuarioId}/paciente-id")
    public ResponseEntity<Map<String, Long>> getPacienteId(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(Map.of("pacienteId", usuarioService.buscarPacienteIdPorUsuarioId(usuarioId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizar(@PathVariable Long id, @RequestBody UsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, dto));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        usuarioService.desactivarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        usuarioService.activarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}