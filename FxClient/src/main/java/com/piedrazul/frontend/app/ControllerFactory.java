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
 * Si añades un nuevo controlador, registrarlo aquí.
 */
public class ControllerFactory implements Callback<Class<?>, Object> {

    private final AppContext ctx;

    public ControllerFactory(AppContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Object call(Class<?> controllerClass) {

        if (controllerClass == LoginController.class)
            return new LoginController(
                    ctx.getAuthClient(),
                    ctx.getStageInitializer(),
                    ctx.getSessionManager()
            );

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
                    ctx.getAuthClient()
            );

        if (controllerClass == FormProfesionalController.class)
            return new FormProfesionalController(
                    ctx.getUsuarioClient(),
                    ctx.getAuthClient(),
                    ctx.getEspecialidadClient()
            );

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

        throw new IllegalArgumentException(
                "Controlador no registrado en ControllerFactory: " + controllerClass.getName()
        );
    }
}
