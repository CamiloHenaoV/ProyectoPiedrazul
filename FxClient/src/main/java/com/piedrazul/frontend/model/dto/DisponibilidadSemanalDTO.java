package com.piedrazul.frontend.model.dto;

import java.time.LocalTime;

/** DTO de disponibilidad semanal para el frontend (HU-1.5, HU-1.6). */
public class DisponibilidadSemanalDTO {
    private Long      id;
    private Long      profesionalId;
    private Integer   diaSemana;          // 0=Dom … 6=Sáb
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer   duracionCitaMinutos;

    public DisponibilidadSemanalDTO() {}

    public Long      getId()                    { return id; }
    public void      setId(Long id)             { this.id = id; }
    public Long      getProfesionalId()          { return profesionalId; }
    public void      setProfesionalId(Long v)    { this.profesionalId = v; }
    public Integer   getDiaSemana()              { return diaSemana; }
    public void      setDiaSemana(Integer v)     { this.diaSemana = v; }
    public LocalTime getHoraInicio()             { return horaInicio; }
    public void      setHoraInicio(LocalTime v)  { this.horaInicio = v; }
    public LocalTime getHoraFin()                { return horaFin; }
    public void      setHoraFin(LocalTime v)     { this.horaFin = v; }
    public Integer   getDuracionCitaMinutos()    { return duracionCitaMinutos; }
    public void      setDuracionCitaMinutos(Integer v) { this.duracionCitaMinutos = v; }

    /** Nombre legible del día de semana para mostrar en la UI. */
    public String getNombreDia() {
        if (diaSemana == null) return "";
        String[] dias = {"Domingo","Lunes","Martes","Miércoles","Jueves","Viernes","Sábado"};
        return diaSemana >= 0 && diaSemana < dias.length ? dias[diaSemana] : "?";
    }

    @Override
    public String toString() {
        return getNombreDia() + "  " + horaInicio + "–" + horaFin
               + "  (" + duracionCitaMinutos + " min)";
    }
}
