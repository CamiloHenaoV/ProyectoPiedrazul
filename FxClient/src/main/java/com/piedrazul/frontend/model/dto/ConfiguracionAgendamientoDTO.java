package com.piedrazul.frontend.model.dto;

/** DTO de configuración global de ventana de agendamiento (HU-1.7). */
public class ConfiguracionAgendamientoDTO {
    private Long    id;
    private Integer semanasHabilitadas;

    public ConfiguracionAgendamientoDTO() {}
    public ConfiguracionAgendamientoDTO(Long id, Integer semanas) {
        this.id = id;
        this.semanasHabilitadas = semanas;
    }

    public Long    getId()                       { return id; }
    public void    setId(Long id)                { this.id = id; }
    public Integer getSemanasHabilitadas()        { return semanasHabilitadas; }
    public void    setSemanasHabilitadas(Integer v){ this.semanasHabilitadas = v; }
}
