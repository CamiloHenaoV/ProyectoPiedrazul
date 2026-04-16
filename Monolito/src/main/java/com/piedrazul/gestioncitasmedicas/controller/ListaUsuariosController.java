package com.piedrazul.gestioncitasmedicas.controller;

import com.piedrazul.gestioncitasmedicas.app.StageInitializer;
import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IUsuarioService;
import com.piedrazul.gestioncitasmedicas.observer.AppEvent;
import com.piedrazul.gestioncitasmedicas.observer.EventBus;
import com.piedrazul.gestioncitasmedicas.observer.Observer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Modality;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Controlador JavaFX para la vista de gestión de usuarios
 * ({@code /view/fxml/usuarios/lista-usuarios.fxml}).
 *
 * <p>Presenta la lista completa de usuarios del sistema en una {@link TableView} y
 * permite crear, editar, activar y desactivar usuarios. Implementa
 * {@link Observer}{@code <UsuarioDTO>} para que la tabla se refresque automáticamente
 * cada vez que otro controlador publica un evento de usuario en el {@link EventBus},
 * sin necesidad de actualización manual.</p>
 *
 * <p>Eventos del {@link EventBus} a los que este controlador se suscribe:
 * <ul>
 *   <li>{@link AppEvent#USUARIO_CREADO}      → recarga la tabla.</li>
 *   <li>{@link AppEvent#USUARIO_ACTUALIZADO} → recarga la tabla.</li>
 *   <li>{@link AppEvent#USUARIO_DESACTIVADO} → recarga la tabla.</li>
 * </ul>
 * Las suscripciones se cancelan explícitamente en {@link #volver()} para evitar
 * referencias colgantes al salir de la vista.</p>
 *
 * <p>El formulario de creación/edición se abre como un modal
 * ({@link Modality#APPLICATION_MODAL}) de 500 × 450 px. Su controlador
 * ({@link FormUsuarioController}) se instancia a través de {@code context::getBean}
 * para que Spring inyecte sus dependencias correctamente.</p>
 *
 * <p>Las dependencias se inyectan por constructor.</p>
 *
 * @see Observer
 * @see EventBus
 * @see FormUsuarioController
 * @see IUsuarioService
 */

@Component
public class ListaUsuariosController implements Observer<UsuarioDTO> {

    @FXML private TableView<UsuarioDTO>            tablaUsuarios;
    @FXML private TableColumn<UsuarioDTO, String>  colNombre;
    @FXML private TableColumn<UsuarioDTO, String>  colRol;
    @FXML private TableColumn<UsuarioDTO, Boolean> colActivo;
    @FXML private TextField                        txtBuscar;
    @FXML private Button                           btnEditar;
    @FXML private Button                           btnToggleActivo;
    @FXML private Label                            lblEstado;

    private final IUsuarioService    usuarioService;
    private final StageInitializer   stageInitializer;
    private final EventBus           eventBus;

    private ObservableList<UsuarioDTO> todosLosUsuarios = FXCollections.observableArrayList();

    /**
     * Construye el controlador con las dependencias requeridas.
     *
     * @param usuarioService   servicio de usuarios (listar, activar, desactivar)
     * @param stageInitializer gestor de navegación entre vistas JavaFX
     * @param eventBus         bus de eventos del patrón Observer
     */

    public ListaUsuariosController(IUsuarioService usuarioService,
                                   StageInitializer stageInitializer,
                                   EventBus eventBus) {
        this.usuarioService   = usuarioService;
        this.stageInitializer = stageInitializer;
        this.eventBus         = eventBus;
    }

    /**
     * Inicializa la vista una vez que el FXML ha sido cargado.
     *
     * <p>Secuencia de inicialización:
     * <ol>
     *   <li>Configura los {@code CellValueFactory} y el {@code CellFactory} de cada columna.</li>
     *   <li>Registra un {@code ChangeListener} sobre la selección de la tabla para
     *       habilitar/deshabilitar los botones y actualizar el texto de {@code btnToggleActivo}.</li>
     *   <li>Se suscribe a los eventos {@code USUARIO_CREADO}, {@code USUARIO_ACTUALIZADO}
     *       y {@code USUARIO_DESACTIVADO} en el {@link EventBus}.</li>
     *   <li>Carga la lista inicial de usuarios desde el servicio.</li>
     * </ol>
     * </p>
     */

    @FXML
    public void initialize() {
        configurarColumnas();
        configurarSeleccion();

        eventBus.subscribe(AppEvent.USUARIO_CREADO,      this);
        eventBus.subscribe(AppEvent.USUARIO_ACTUALIZADO, this);
        eventBus.subscribe(AppEvent.USUARIO_DESACTIVADO, this);

        cargarUsuarios();
    }
    /**
     * Responde a eventos de usuario publicados en el {@link EventBus}.
     *
     * <p>Recarga la tabla en el JavaFX Application Thread mediante
     * {@link Platform#runLater(Runnable)}, garantizando que la actualización de
     * UI sea segura independientemente del hilo en que se publique el evento.</p>
     *
     * @param event tipo de evento recibido
     * @param data  DTO del usuario involucrado (no usado directamente; la tabla se recarga completa)
     */

    @Override
    public void onEvent(AppEvent event, UsuarioDTO data) {
        Platform.runLater(this::cargarUsuarios);
    }

    /**
     * Filtra la tabla por el texto introducido en {@code txtBuscar}.
     *
     * <p>La búsqueda es insensible a mayúsculas y compara el texto contra el
     * nombre completo y el login del usuario. Si el campo está vacío, restaura
     * la lista completa {@code todosLosUsuarios}. El filtrado se realiza en
     * memoria sobre la lista maestra, sin consultar la base de datos.</p>
     */
    @FXML
    private void handleBuscar() {
        String filtro = txtBuscar.getText().trim().toLowerCase();
        if (filtro.isEmpty()) {
            tablaUsuarios.setItems(todosLosUsuarios);
            return;
        }
        List<UsuarioDTO> filtrados = todosLosUsuarios.stream()
                .filter(u ->
                        u.getNombreCompleto().toLowerCase().contains(filtro) ||
                                u.getLogin().toLowerCase().contains(filtro)
                )
                .collect(Collectors.toList());
        tablaUsuarios.setItems(FXCollections.observableArrayList(filtrados));
    }

    /**
     * Abre el formulario modal en modo creación (sin usuario preseleccionado).
     *
     * @see #abrirFormulario(UsuarioDTO)
     */
    @FXML
    private void handleNuevo() {
        abrirFormulario(null);
    }
    /**
     * Abre el formulario modal en modo edición con el usuario seleccionado en la tabla.
     *
     * <p>Si no hay ningún usuario seleccionado el método no hace nada, aunque el botón
     * ya debería estar deshabilitado por {@link #configurarSeleccion()}.</p>
     *
     * @see #abrirFormulario(UsuarioDTO)
     */
    @FXML
    private void handleEditar() {
        UsuarioDTO seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado != null) abrirFormulario(seleccionado);
    }

    /**
     * Activa o desactiva el usuario seleccionado en la tabla según su estado actual.
     *
     * <p>Si el usuario está activo, llama a {@link IUsuarioService#desactivarUsuario(UUID)};
     * de lo contrario llama a {@link IUsuarioService#activarUsuario(UUID)}.
     * Tras la operación recarga la tabla y actualiza {@code lblEstado} con el mensaje
     * correspondiente. Si no hay ningún usuario seleccionado, el método no hace nada.</p>
     */
    @FXML
    private void handleToggleActivo() {
        UsuarioDTO seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        if (seleccionado.getActivo()) {
            usuarioService.desactivarUsuario(seleccionado.getId());
            lblEstado.setText("Usuario desactivado correctamente.");
        } else {
            usuarioService.activarUsuario(seleccionado.getId());
            lblEstado.setText("Usuario activado correctamente.");
        }
    }
    /**
     * Cancela las suscripciones al {@link EventBus} y regresa al dashboard del administrador
     * ({@code /view/fxml/dashboard/dashboard-admin.fxml}, 900 × 600).
     *
     * <p>El desregistro explícito es necesario para liberar las referencias que el
     * {@link EventBus} mantiene sobre este controlador y evitar que {@link #onEvent}
     * sea invocado después de que la vista haya sido descartada.</p>
     */
    @FXML
    private void volver() {
        eventBus.unsubscribe(AppEvent.USUARIO_CREADO,      this);
        eventBus.unsubscribe(AppEvent.USUARIO_ACTUALIZADO, this);
        eventBus.unsubscribe(AppEvent.USUARIO_DESACTIVADO, this);
        stageInitializer.cambiarVista(
                "/view/fxml/dashboard/dashboard-admin.fxml",
                "Piedrazul - Dashboard",
                900, 600
        );
    }
    /**
     * Configura los {@code CellValueFactory} de cada columna y el {@code CellFactory}
     * personalizado de {@code colActivo}.
     *
     * <p>La columna {@code colActivo} usa un {@code CellFactory} que convierte el
     * booleano en el texto {@code "Activo"} o {@code "Inactivo"}, manejando
     * correctamente las celdas vacías.</p>
     */

    private void configurarColumnas() {
        colNombre.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getNombreCompleto()
                )
        );
        colRol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getRol().name()
                )
        );
        colActivo.setCellValueFactory(data ->
                new javafx.beans.property.SimpleBooleanProperty(
                        data.getValue().getActivo()
                )
        );
        colActivo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean activo, boolean empty) {
                if (empty) {
                    setText("");
                } else {
                    setText(activo ? "Activo" : "Inactivo");
                }
            }
        });
    }
    /**
     * Registra un {@code ChangeListener} sobre la propiedad de selección de la tabla.
     *
     * <p>Cuando hay un usuario seleccionado:
     * <ul>
     *   <li>Habilita {@code btnEditar} y {@code btnToggleActivo}.</li>
     *   <li>Actualiza el texto de {@code btnToggleActivo} a {@code "Desactivar"} o
     *       {@code "Activar"} según el estado actual del usuario.</li>
     * </ul>
     * Cuando no hay selección, ambos botones se deshabilitan.</p>
     */
    private void configurarSeleccion() {
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, actual) -> {
                    boolean haySeleccion = actual != null;
                    btnEditar.setDisable(!haySeleccion);
                    btnToggleActivo.setDisable(!haySeleccion);
                    if (haySeleccion) {
                        btnToggleActivo.setText(actual.getActivo() ? "Desactivar" : "Activar");
                    }
                }
        );
    }


    /**
     * Consulta todos los usuarios mediante {@link IUsuarioService#listarTodos()}, actualiza
     * la lista maestra {@code todosLosUsuarios}, asigna los ítems a la tabla y
     * muestra el total en {@code lblEstado}.
     *
     * <p>Llamado en {@link #initialize()}, desde {@link #onEvent(AppEvent, UsuarioDTO)}
     * (vía {@link Platform#runLater(Runnable)}) y directamente en
     * {@link #handleToggleActivo()} tras modificar el estado de un usuario.</p>
     */
    private void cargarUsuarios() {
        todosLosUsuarios = FXCollections.observableArrayList(
                usuarioService.listarTodos()
        );
        tablaUsuarios.setItems(todosLosUsuarios);
        lblEstado.setText("Total: " + todosLosUsuarios.size() + " usuarios");
    }
    /**
     * Abre el formulario de usuarios como ventana modal de 500 × 450 px.
     *
     * <p>El {@link FXMLLoader} utiliza {@code context::getBean} como
     * {@code controllerFactory} para que Spring inyecte las dependencias de
     * {@link FormUsuarioController}. Una vez cargado el FXML, se llama a
     * {@link FormUsuarioController#setUsuario(UsuarioDTO)} para precargar los datos
     * del usuario a editar o indicar que se trata de un nuevo registro.</p>
     *
     * <p>Si la carga del FXML falla, se captura la {@link IOException} y se muestra
     * el mensaje de error en {@code lblEstado}.</p>
     *
     * @param usuario {@link UsuarioDTO} a editar, o {@code null} para crear uno nuevo
     */
    private void abrirFormulario(UsuarioDTO usuario) {
        stageInitializer.abrirModal(
                "/view/fxml/usuarios/form-usuarios.fxml",
                usuario == null ? "Nuevo Usuario" : "Editar Usuario",
                500, 450,
                loader -> {
                    FormUsuarioController controller = loader.getController();
                    controller.setUsuario(usuario);
                }
        );
    }
}