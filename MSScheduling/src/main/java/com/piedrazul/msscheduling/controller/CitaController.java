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

    // ── Agendar ──────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<CitaDTO> agendar(@RequestBody CitaDTO dto) {
        return ResponseEntity.ok(citaService.agendarCita(dto));
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

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

    /**
     * HU-6.1 – Citas de un profesional filtradas por fecha.
     * GET /api/scheduling/citas/profesional/{profesionalId}/fecha?fecha=YYYY-MM-DD
     *
     * Devuelve el listado de citas y, en el header X-Total-Count, la cantidad total.
     * El cliente puede leer ese header para mostrar el contador de citas del día.
     */
    @GetMapping("/profesional/{profesionalId}/fecha")
    public ResponseEntity<List<CitaDTO>> listarPorProfesionalYFecha(
            @PathVariable Long profesionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        List<CitaDTO> citas = citaService.listarPorProfesionalYFecha(profesionalId, fecha);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(citas.size()))
                .body(citas);
    }

    @GetMapping("/profesional/{profesionalId}/disponibilidad")
    public ResponseEntity<List<ZonedDateTime>> obtenerHorariosDisponibles(
            @PathVariable Long profesionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(citaService.obtenerHorariosDisponibles(profesionalId, fecha));
    }

    // ── Transiciones de estado ────────────────────────────────────────────────

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    @PatchMapping("/{id}/completar")
    public ResponseEntity<CitaDTO> completar(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.completarCita(id));
    }

    /**
     * HU-6.3 – Reprogramar una cita.
     * PUT /api/scheduling/citas/{id}
     *
     * Solo acepta cambio de fechaHora. El cuerpo debe contener el CitaDTO
     * con el campo fechaHora actualizado. Devuelve 409 si el horario está ocupado
     * y 422 si la cita no está en estado programada.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CitaDTO> actualizar(
            @PathVariable Long id,
            @RequestBody CitaDTO dto) {
        return ResponseEntity.ok(citaService.actualizarCita(id, dto));
    }

    // ── Conteo ────────────────────────────────────────────────────────────────

    @GetMapping("/contar")
    public ResponseEntity<Long> contarPorEstado(@RequestParam EstadoCita estado) {
        return ResponseEntity.ok(citaService.contarCitasPorEstado(estado));
    }
}
