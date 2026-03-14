package com.piedrazul.gestioncitasmedicas.controller;

import com.piedrazul.gestioncitasmedicas.app.StageInitializer;
import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IUsuarioService;
import com.piedrazul.gestioncitasmedicas.observer.AppEvent;
import com.piedrazul.gestioncitasmedicas.observer.EventBus;
import com.piedrazul.gestioncitasmedicas.observer.Observer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

    /**
     * Controlador JavaFX del dashboard del administrador
     * ({@code /view/fxml/dashboard/dashboard-admin.fxml}).
     *
     * <p>Muestra información de sesión del usuario autenticado y el contador total de
     * usuarios registrados en el sistema. Implementa {@link Observer}{@code <UsuarioDTO>}
     * para reaccionar automáticamente a cambios en la lista de usuarios sin necesidad de
     * recargar la vista manualmente.</p>
     *
     * <p>Eventos del {@link EventBus} a los que se suscribe en {@link #initialize()}:
     * <ul>
     *   <li>{@link AppEvent#USUARIO_CREADO}      → actualiza el contador.</li>
     *   <li>{@link AppEvent#USUARIO_ACTUALIZADO} → actualiza el contador.</li>
     *   <li>{@link AppEvent#USUARIO_DESACTIVADO} → actualiza el contador.</li>
     * </ul>
     * </p>
     *
     * <p>Las dependencias se inyectan por constructor. El DTO del usuario
     * autenticado se recibe desde {@link LoginController} mediante
     * {@link #setUsuarioActual(UsuarioDTO)} tras la carga del FXML.</p>
     *
     * @see Observer
     * @see EventBus
     */
@Component
public class DashboardAdminController implements Observer<UsuarioDTO> {

    @FXML private Label lblUsuario;
    @FXML private Label lblTotalUsuarios;

    private final IUsuarioService  usuarioService;
    private final StageInitializer stageInitializer;
    private final EventBus         eventBus;

    private UsuarioDTO usuarioActual;

    public DashboardAdminController(IUsuarioService usuarioService,
                                    StageInitializer stageInitializer,
                                    EventBus eventBus) {
        this.usuarioService   = usuarioService;
        this.stageInitializer = stageInitializer;
        this.eventBus         = eventBus;
    }

        /**
         * Inicializa el dashboard una vez que el FXML ha sido cargado.
         *
         * <p>Se suscribe a los eventos {@code USUARIO_CREADO}, {@code USUARIO_ACTUALIZADO}
         * y {@code USUARIO_DESACTIVADO} en el {@link EventBus}, y realiza la primera
         * carga del contador de usuarios.</p>
         *
         * <p><strong>Nota:</strong> {@code lblUsuario} no se actualiza aquí porque el
         * {@link UsuarioDTO} se recibe de forma diferida mediante
         * {@link #setUsuarioActual(UsuarioDTO)}, llamado por {@link LoginController}
         * después de {@code initialize()}.</p>
         */
    @FXML
    public void initialize() {
        eventBus.subscribe(AppEvent.USUARIO_CREADO,      this);
        eventBus.subscribe(AppEvent.USUARIO_ACTUALIZADO, this);
        eventBus.subscribe(AppEvent.USUARIO_DESACTIVADO, this);
        actualizarContador();
    }
        /**
         * Recibe el usuario autenticado y actualiza la etiqueta de bienvenida.
         *
         * <p>Es llamado por {@link LoginController} inmediatamente después de cargar
         * esta vista, garantizando que {@code lblUsuario} refleje el nombre del
         * administrador en sesión.</p>
         *
         * @param usuario DTO del administrador autenticado; no debe ser {@code null}
         */
    public void setUsuarioActual(UsuarioDTO usuario) {
        this.usuarioActual = usuario;
        lblUsuario.setText(usuario.getNombreCompleto());
    }
        /**
         * Responde a eventos de usuario publicados en el {@link EventBus}.
         *
         * <p>Actualiza el contador de {@code lblTotalUsuarios} cada vez que se crea,
         * modifica o desactiva un usuario. Este método es invocado en el hilo que
         * publicó el evento; si dicho hilo no es el JavaFX Application Thread, la
         * actualización de UI podría requerir {@link javafx.application.Platform#runLater(Runnable)}
         * en el publicador.</p>
         *
         * @param event tipo de evento recibido (USUARIO_CREADO, USUARIO_ACTUALIZADO, USUARIO_DESACTIVADO)
         * @param data  DTO del usuario involucrado en el evento (no utilizado directamente aquí)
         */
    @Override
    public void onEvent(AppEvent event, UsuarioDTO data) {
        actualizarContador();
    }

        /**
         * Cierra la sesión del administrador y navega a la pantalla de login.
         *
         * <p>La vista de destino es {@code /view/fxml/auth/login.fxml} (400 × 300).
         * No cancela suscripciones del {@link EventBus} de forma explícita; se
         * recomienda agregar el desregistro si el controlador puede reutilizarse.</p>
         */
    @FXML
    private void cerrarSesion() {
        stageInitializer.cambiarVista(
                "/view/fxml/auth/login.fxml",
                "Piedrazul - Iniciar Sesión",
                400, 300
        );
    }

        /**
         * Navega a la vista de gestión de usuarios ({@code lista-usuarios.fxml}, 1000 × 650).
         *
         * <p>La lista de usuarios se gestiona desde {@link ListaUsuariosController},
         * que tiene su propio ciclo de suscripción al {@link EventBus}.</p>
         */
    @FXML
    private void irAUsuarios() {
        stageInitializer.cambiarVista(
                "/view/fxml/usuarios/lista-usuarios.fxml",
                "Piedrazul - Gestión de Usuarios",
                1000, 650
        );
    }

        /**
         * Consulta el total de usuarios y actualiza {@code lblTotalUsuarios}.
         *
         * <p>Invoca {@link IUsuarioService#listarTodos()} y asigna el tamaño de la
         * lista resultante a la etiqueta. Llamado en {@link #initialize()} y cada
         * vez que {@link #onEvent(AppEvent, UsuarioDTO)} es disparado.</p>
         */
    private void actualizarContador() {
        int total = usuarioService.listarTodos().size();
        lblTotalUsuarios.setText(String.valueOf(total));
    }
}