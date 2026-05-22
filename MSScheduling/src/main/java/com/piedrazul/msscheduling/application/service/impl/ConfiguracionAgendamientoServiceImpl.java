package com.piedrazul.msscheduling.application.service.impl;

import com.piedrazul.msscheduling.application.service.interfaces.IConfiguracionAgendamientoService;
import com.piedrazul.msscheduling.domain.model.dto.ConfiguracionAgendamientoDTO;
import com.piedrazul.msscheduling.domain.model.entity.ConfiguracionAgendamiento;
import com.piedrazul.msscheduling.domain.model.repository.ConfiguracionAgendamientoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Implementación del servicio de configuración global de agendamiento.
 * HU-1.7: ventana de tiempo disponible para agendamiento.
 *   SC-1: el administrador define semanas habilitadas → se aplica el rango.
 *   SC-2: al consultar disponibilidad, CitaService usa obtenerFechaMaximaAgendamiento().
 *   SC-3: actualizar() persiste el nuevo rango.
 * Patrón singleton en BD: siempre id = 1.
 * Si no existe el registro se crea con valor por defecto (4 semanas).
 */
@Service
@Slf4j
public class ConfiguracionAgendamientoServiceImpl implements IConfiguracionAgendamientoService {

    private static final Long CONFIG_ID = 1L;
    private static final int  DEFAULT_SEMANAS = 4;

    private final ConfiguracionAgendamientoRepository repository;

    public ConfiguracionAgendamientoServiceImpl(ConfiguracionAgendamientoRepository repository) {
        this.repository = repository;
    }

    @Override
    public ConfiguracionAgendamientoDTO obtener() {
        ConfiguracionAgendamiento config = repository.findById(CONFIG_ID)
                .orElseGet(this::crearConfiguracionPorDefecto);
        return toDTO(config);
    }

    @Override
    @Transactional
    public ConfiguracionAgendamientoDTO actualizar(ConfiguracionAgendamientoDTO dto) {
        ConfiguracionAgendamiento config = repository.findById(CONFIG_ID)
                .orElseGet(this::crearConfiguracionPorDefecto);

        config.setSemanasHabilitadas(dto.getSemanasHabilitadas());
        ConfiguracionAgendamiento guardada = repository.save(config);

        log.info("Configuración de agendamiento actualizada: {} semanas habilitadas",
                guardada.getSemanasHabilitadas());
        return toDTO(guardada);
    }

    @Override
    public LocalDate obtenerFechaMaximaAgendamiento() {
        int semanas = repository.findById(CONFIG_ID)
                .map(ConfiguracionAgendamiento::getSemanasHabilitadas)
                .orElse(DEFAULT_SEMANAS);
        return LocalDate.now().plusWeeks(semanas);
    }

    // ── Utilidades privadas ───────────────────────────────────────────────────

    private ConfiguracionAgendamiento crearConfiguracionPorDefecto() {
        log.info("Creando configuración de agendamiento por defecto ({} semanas)", DEFAULT_SEMANAS);
        ConfiguracionAgendamiento config = ConfiguracionAgendamiento.builder()
                .id(CONFIG_ID)
                .semanasHabilitadas(DEFAULT_SEMANAS)
                .build();
        return repository.save(config);
    }

    private ConfiguracionAgendamientoDTO toDTO(ConfiguracionAgendamiento c) {
        return ConfiguracionAgendamientoDTO.builder()
                .id(c.getId())
                .semanasHabilitadas(c.getSemanasHabilitadas())
                .build();
    }
}
