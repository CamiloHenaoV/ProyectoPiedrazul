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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

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
    private final ApplicationContext context;

    private ObservableList<UsuarioDTO> todosLosUsuarios = FXCollections.observableArrayList();

    public ListaUsuariosController(IUsuarioService usuarioService,
                                   StageInitializer stageInitializer,
                                   EventBus eventBus,
                                   ApplicationContext context) {
        this.usuarioService   = usuarioService;
        this.stageInitializer = stageInitializer;
        this.eventBus         = eventBus;
        this.context          = context;
    }

    @FXML
    public void initialize() {
        configurarColumnas();
        configurarSeleccion();

        eventBus.subscribe(AppEvent.USUARIO_CREADO,      this);
        eventBus.subscribe(AppEvent.USUARIO_ACTUALIZADO, this);
        eventBus.subscribe(AppEvent.USUARIO_DESACTIVADO, this);

        cargarUsuarios();
    }

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

    @Override
    public void onEvent(AppEvent event, UsuarioDTO data) {
        Platform.runLater(this::cargarUsuarios);
    }

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

    @FXML
    private void handleNuevo() {
        abrirFormulario(null);
    }

    @FXML
    private void handleEditar() {
        UsuarioDTO seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado != null) abrirFormulario(seleccionado);
    }

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
        cargarUsuarios();
    }

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

    private void cargarUsuarios() {
        todosLosUsuarios = FXCollections.observableArrayList(
                usuarioService.listarTodos()
        );
        tablaUsuarios.setItems(todosLosUsuarios);
        lblEstado.setText("Total: " + todosLosUsuarios.size() + " usuarios");
    }

    private void abrirFormulario(UsuarioDTO usuario) {
        try {
            URL url = getClass().getResource("/view/fxml/usuarios/form-usuarios.fxml");
            FXMLLoader loader = new FXMLLoader(url);
            loader.setControllerFactory(context::getBean);

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle(usuario == null ? "Nuevo Usuario" : "Editar Usuario");
            modal.setScene(new Scene(loader.load(), 500, 450));

            FormUsuarioController controller = loader.getController();
            controller.setUsuario(usuario);
            modal.show();

        } catch (IOException e) {
            lblEstado.setText("Error al abrir el formulario.");
            e.printStackTrace();
        }
    }
}