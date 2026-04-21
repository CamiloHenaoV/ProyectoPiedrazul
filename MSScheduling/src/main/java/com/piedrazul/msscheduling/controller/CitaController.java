package com.piedrazul.msscheduling.controller;

import com.piedrazul.msscheduling.application.service.interfaces.ICitaService;
import com.piedrazul.msscheduling.domain.model.dto.CitaDTO;
import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/scheduling/citas")
public class CitaController {

    private final ICitaService citaService;

    public CitaController(ICitaService citaService) {
        this.citaService = citaService;
    }

    @PostMapping
    public ResponseEntity<CitaDTO> agendar(@RequestBody CitaDTO dto) {
        return ResponseEntity.ok(citaService.agendarCita(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CitaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.buscarPorId(id));
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<CitaDTO>> listarPorPaciente(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(citaService.listarPorPaciente(pacienteId));
    }

    @GetMapping("/profesional/{profesionalId}")
    public ResponseEntity<List<CitaDTO>> listarPorProfesional(@PathVariable Long profesionalId) {
        return ResponseEntity.ok(citaService.listarPorProfesional(profesionalId));
    }

    @GetMapping("/profesional/{profesionalId}/disponibilidad")
    public ResponseEntity<List<ZonedDateTime>> obtenerHorariosDisponibles(
            @PathVariable Long profesionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(citaService.obtenerHorariosDisponibles(profesionalId, fecha));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    @PatchMapping("/{id}/completar")
    public ResponseEntity<CitaDTO> completar(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.completarCita(id));
    }

    @GetMapping("/contar")
    public ResponseEntity<Long> contarPorEstado(@RequestParam EstadoCita estado) {
        return ResponseEntity.ok(citaService.contarCitasPorEstado(estado));
    }
}
