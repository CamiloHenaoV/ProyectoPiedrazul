package com.piedrazul.msscheduling.application.service.facade;

import com.piedrazul.msscheduling.application.service.interfaces.ICitaService;
import com.piedrazul.msscheduling.application.service.interfaces.IConfiguracionAgendamientoService;
import com.piedrazul.msscheduling.application.service.interfaces.IDiaNoDisponibleService;
import com.piedrazul.msscheduling.application.service.interfaces.IDisponibilidadService;
import com.piedrazul.msscheduling.domain.model.dto.CitaDTO;
import com.piedrazul.msscheduling.domain.model.dto.ConfiguracionAgendamientoDTO;
import com.piedrazul.msscheduling.domain.model.dto.DiaNoDisponibleDTO;
import com.piedrazul.msscheduling.domain.model.dto.DisponibilidadSemanalDTO;
import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Patrón Facade — «ConcreteSubsystemFacade»
 *
 * Coordina los cuatro servicios del subsistema de agendamiento
 * y expone una interfaz unificada a los controllers.
 *
 * Los controllers pasan de tener esto:
 *   @Autowired ICitaService citaService
 *   @Autowired IDisponibilidadService disponibilidadService
 *   @Autowired IConfiguracionAgendamientoService configuracionService
 *   @Autowired IDiaNoDisponibleService diaNoDisponibleService
 *
 * A tener esto:
 *   @Autowired IAgendamientoFacade agendamientoFacade
 *
 * Subsistema (los cuatro servicios internos) permanece intacto.
 */
@Service
public class AgendamientoFacadeImpl implements IAgendamientoFacade {

    // Los cuatro servicios del subsistema — ninguno se modifica
    private final ICitaService                      citaService;
    private final IDisponibilidadService            disponibilidadService;
    private final IConfiguracionAgendamientoService configuracionService;
    private final IDiaNoDisponibleService           diaNoDisponibleService;

    public AgendamientoFacadeImpl(
            ICitaService citaService,
            IDisponibilidadService disponibilidadService,
            IConfiguracionAgendamientoService configuracionService,
            IDiaNoDisponibleService diaNoDisponibleService) {
        this.citaService            = citaService;
        this.disponibilidadService  = disponibilidadService;
        this.configuracionService   = configuracionService;
        this.diaNoDisponibleService = diaNoDisponibleService;
    }

    // ── Operaciones de Cita ──────────────────────────────────────────────────

    @Override
    public CitaDTO agendarCita(CitaDTO dto) {
        return citaService.agendarCita(dto);
    }

    @Override
    public CitaDTO buscarCitaPorId(Long id) {
        return citaService.buscarPorId(id);
    }

    @Override
    public List<CitaDTO> listarCitasPorPaciente(Long pacienteId) {
        return citaService.listarPorPaciente(pacienteId);
    }

    @Override
    public List<CitaDTO> listarCitasPorProfesional(Long profesionalId) {
        return citaService.listarPorProfesional(profesionalId);
    }

    @Override
    public List<CitaDTO> listarCitasPorProfesionalYFecha(Long profesionalId, LocalDate fecha) {
        return citaService.listarPorProfesionalYFecha(profesionalId, fecha);
    }

    @Override
    public List<ZonedDateTime> obtenerHorariosDisponibles(Long profesionalId, LocalDate fecha) {
        return citaService.obtenerHorariosDisponibles(profesionalId, fecha);
    }

    @Override
    public CitaDTO cancelarCita(Long id) {
        return citaService.cancelarCita(id);
    }

    @Override
    public CitaDTO completarCita(Long id) {
        return citaService.completarCita(id);
    }

    @Override
    public CitaDTO actualizarCita(Long id, CitaDTO dto) {
        return citaService.actualizarCita(id, dto);
    }

    @Override
    public long contarCitasPorEstado(EstadoCita estado) {
        return citaService.contarCitasPorEstado(estado);
    }

    // ── Operaciones de Disponibilidad ────────────────────────────────────────

    @Override
    public DisponibilidadSemanalDTO crearDisponibilidad(DisponibilidadSemanalDTO dto) {
        return disponibilidadService.crear(dto);
    }

    @Override
    public DisponibilidadSemanalDTO actualizarDisponibilidad(Long id, DisponibilidadSemanalDTO dto) {
        return disponibilidadService.actualizar(id, dto);
    }

    @Override
    public List<DisponibilidadSemanalDTO> listarDisponibilidadPorProfesional(Long profesionalId) {
        return disponibilidadService.listarPorProfesional(profesionalId);
    }

    @Override
    public void eliminarDisponibilidad(Long id) {
        disponibilidadService.eliminar(id);
    }

    // ── Operaciones de Configuración ─────────────────────────────────────────

    @Override
    public ConfiguracionAgendamientoDTO obtenerConfiguracion() {
        return configuracionService.obtener();
    }

    @Override
    public ConfiguracionAgendamientoDTO actualizarConfiguracion(ConfiguracionAgendamientoDTO dto) {
        return configuracionService.actualizar(dto);
    }

    @Override
    public LocalDate obtenerFechaMaximaAgendamiento() {
        return configuracionService.obtenerFechaMaximaAgendamiento();
    }

    // ── Operaciones de Días No Disponibles ───────────────────────────────────

    @Override
    public DiaNoDisponibleDTO registrarDiaNoDisponible(DiaNoDisponibleDTO dto) {
        return diaNoDisponibleService.registrar(dto);
    }

    @Override
    public List<DiaNoDisponibleDTO> listarDiasNoDisponibles() {
        return diaNoDisponibleService.listarTodos();
    }

    @Override
    public List<DiaNoDisponibleDTO> listarDiasNoDisponiblesEnRango(
            LocalDate desde, LocalDate hasta) {
        return diaNoDisponibleService.listarEnRango(desde, hasta);
    }

    @Override
    public void eliminarDiaNoDisponible(Long id) {
        diaNoDisponibleService.eliminar(id);
    }
}
