package com.piedrazul.frontend.model.dto;

import java.time.LocalDate;

/** DTO de día no disponible (festivo o bloqueo manual) para el frontend. HU-1.8. */
public class DiaNoDisponibleDTO {
    private Long      id;
    private LocalDate fecha;
    private String    motivo;
    private String    tipo;   // "FESTIVO" | "BLOQUEO_MANUAL"

    public DiaNoDisponibleDTO() {}

    public Long      getId()              { return id; }
    public void      setId(Long id)       { this.id = id; }
    public LocalDate getFecha()           { return fecha; }
    public void      setFecha(LocalDate v){ this.fecha = v; }
    public String    getMotivo()          { return motivo; }
    public void      setMotivo(String v)  { this.motivo = v; }
    public String    getTipo()            { return tipo; }
    public void      setTipo(String v)    { this.tipo = v; }

    /** Etiqueta amigable para la tabla de la UI. */
    public String getTipoLabel() {
        return "FESTIVO".equals(tipo) ? "🎉 Festivo" : "🔒 Bloqueo";
    }

    @Override
    public String toString() {
        return fecha + " – " + (motivo != null ? motivo : tipo);
    }
}
