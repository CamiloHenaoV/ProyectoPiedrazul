package com.piedrazul.frontend.app;

import com.piedrazul.frontend.controller.*;
import javafx.util.Callback;

/**
 * Fábrica de controladores para FXMLLoader.
 *
 * Reemplaza el uso de {@code context::getBean} del monolito Spring.
 * JavaFX llama a {@code call(Class)} para instanciar cada controlador;
 * aquí inyectamos las dependencias desde AppContext en lugar de Spring.
 *
 * Controladores añadidos para HU-1.5 / HU-1.6 / HU-1.7 / HU-1.8:
 *   - ConfiguracionDisponibilidadController  (HU-1.5, HU-1.6)
 *   - ConfiguracionAgendamientoController    (HU-1.7)
 *   - DiasNoDisponiblesController            (HU-1.8)
 */
public class ControllerFactory implements Callback<Class<?>, Object> {

    private final AppContext ctx;

    public ControllerFactory(AppContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Object call(Class<?> controllerClass) {

        // ── Auth ──────────────────────────────────────────────────────────────
        if (controllerClass == LoginController.class)
            return new LoginController(
                    ctx.getAuthClient(),
                    ctx.getStageInitializer(),
                    ctx.getSessionManager()
            );

        // ── Dashboards ────────────────────────────────────────────────────────
        if (controllerClass == DashboardAdminController.class)
            return new DashboardAdminController(
                    ctx.getUsuarioClient(),
                    ctx.getStageInitializer(),
                    ctx.getEventBus()
            );

        if (controllerClass == DashboardPacienteController.class)
            return new DashboardPacienteController(
                    ctx.getStageInitializer()
            );

        if (controllerClass == DashboardAgendadorController.class)
            return new DashboardAgendadorController(
                    ctx.getStageInitializer()
            );

        // ── Usuarios ──────────────────────────────────────────────────────────
        if (controllerClass == ListaUsuariosController.class)
            return new ListaUsuariosController(
                    ctx.getUsuarioClient(),
                    ctx.getStageInitializer(),
                    ctx.getEventBus()
            );

        if (controllerClass == FormUsuarioController.class)
            return new FormUsuarioController(
                    ctx.getUsuarioClient(),
                    ctx.getAuthClient(),
                    ctx.getStageInitializer(),
                    ctx.getEventBus()
            );

        if (controllerClass == FormPacienteController.class)
            return new FormPacienteController(
                    ctx.getUsuarioClient(),
                    ctx.getAuthClient(),
                    ctx.getEventBus()
            );

        if (controllerClass == FormProfesionalController.class)
            return new FormProfesionalController(
                    ctx.getUsuarioClient(),
                    ctx.getAuthClient(),
                    ctx.getEspecialidadClient(),
                    ctx.getEventBus()
            );

        // ── Citas (paciente) ──────────────────────────────────────────────────
        if (controllerClass == AgendarCitaController.class)
            return new AgendarCitaController(
                    ctx.getEspecialidadClient(),
                    ctx.getCitaClient(),
                    ctx.getUsuarioClient(),
                    ctx.getStageInitializer(),
                    ctx.getSessionManager()
            );

        if (controllerClass == ListaCitasController.class)
            return new ListaCitasController(
                    ctx.getCitaClient(),
                    ctx.getUsuarioClient(),
                    ctx.getEventBus(),
                    ctx.getStageInitializer(),
                    ctx.getSessionManager()
            );

        // ── Citas (agendador) ─────────────────────────────────────────────────
        if (controllerClass == GestionCitasAgendadorController.class)
            return new GestionCitasAgendadorController(
                    ctx.getCitaClient(),
                    ctx.getEspecialidadClient(),
                    ctx.getStageInitializer(),
                    ctx.getEventBus()
            );

        if (controllerClass == RegistroCitaManualController.class)
            return new RegistroCitaManualController(
                    ctx.getCitaClient(),
                    ctx.getEspecialidadClient(),
                    ctx.getUsuarioClient(),
                    ctx.getStageInitializer(),
                    ctx.getEventBus()
            );

        if (controllerClass == ReprogramarCitaController.class)
            return new ReprogramarCitaController(
                    ctx.getCitaClient(),
                    ctx.getStageInitializer(),
                    ctx.getEventBus()
            );

        // ── Configuración de disponibilidad (HU-1.5 / HU-1.6) ────────────────
        if (controllerClass == ConfiguracionDisponibilidadController.class)
            return new ConfiguracionDisponibilidadController(
                    ctx.getDisponibilidadClient(),
                    ctx.getUsuarioClient(),
                    ctx.getStageInitializer()
            );

        // ── Ventana de agendamiento (HU-1.7) ──────────────────────────────────
        if (controllerClass == ConfiguracionAgendamientoController.class)
            return new ConfiguracionAgendamientoController(
                    ctx.getDisponibilidadClient(),
                    ctx.getStageInitializer()
            );

        // ── Días no disponibles / festivos (HU-1.8) ───────────────────────────
        if (controllerClass == DiasNoDisponiblesController.class)
            return new DiasNoDisponiblesController(
                    ctx.getDisponibilidadClient(),
                    ctx.getStageInitializer()
            );

        throw new IllegalArgumentException(
                "Controlador no registrado en ControllerFactory: " + controllerClass.getName()
        );
    }
}
