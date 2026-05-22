package com.piedrazul.msscheduling.application.service.impl;

import com.piedrazul.msscheduling.application.service.interfaces.IDiaNoDisponibleService;
import com.piedrazul.msscheduling.domain.model.dto.DiaNoDisponibleDTO;
import com.piedrazul.msscheduling.domain.model.entity.DiaNoDisponible;
import com.piedrazul.msscheduling.domain.model.repository.DiaNoDisponibleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de días no disponibles.
 * HU-1.8:
 *   SC-1: registrar() bloquea el agendamiento en la fecha seleccionada.
 *   SC-2: esFechaNoDisponible() es consultado por CitaService antes de agendar.
 *   SC-3: eliminar() vuelve a habilitar la fecha.
 */
@Service
@Slf4j
public class DiaNoDisponibleServiceImpl implements IDiaNoDisponibleService {

    private final DiaNoDisponibleRepository repository;

    public DiaNoDisponibleServiceImpl(DiaNoDisponibleRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public DiaNoDisponibleDTO registrar(DiaNoDisponibleDTO dto) {
        if (repository.existsByFecha(dto.getFecha())) {
            throw new IllegalArgumentException(
                    "La fecha " + dto.getFecha() + " ya está registrada como no disponible.");
        }

        DiaNoDisponible entidad = DiaNoDisponible.builder()
                .fecha(dto.getFecha())
                .motivo(dto.getMotivo())
                .tipo(dto.getTipo())
                .build();

        DiaNoDisponible guardado = repository.save(entidad);
        log.info("Día no disponible registrado: {} ({})", guardado.getFecha(), guardado.getTipo());
        return toDTO(guardado);
    }

    @Override
    public List<DiaNoDisponibleDTO> listarTodos() {
        return repository.findAll().stream()
                .sorted((a, b) -> a.getFecha().compareTo(b.getFecha()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DiaNoDisponibleDTO> listarEnRango(LocalDate desde, LocalDate hasta) {
        return repository.findByFechaBetween(desde, hasta).stream()
                .sorted((a, b) -> a.getFecha().compareTo(b.getFecha()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        repository.findById(id).ifPresentOrElse(
                d -> {
                    repository.deleteById(id);
                    log.info("Restricción eliminada para fecha: {}", d.getFecha());
                },
                () -> { throw new IllegalArgumentException("Día no disponible no encontrado: " + id); }
        );
    }

    @Override
    public boolean esFechaNoDisponible(LocalDate fecha) {
        return repository.existsByFecha(fecha);
    }

    // ── Utilidades privadas ───────────────────────────────────────────────────

    private DiaNoDisponibleDTO toDTO(DiaNoDisponible d) {
        return DiaNoDisponibleDTO.builder()
                .id(d.getId())
                .fecha(d.getFecha())
                .motivo(d.getMotivo())
                .tipo(d.getTipo())
                .build();
    }
}
