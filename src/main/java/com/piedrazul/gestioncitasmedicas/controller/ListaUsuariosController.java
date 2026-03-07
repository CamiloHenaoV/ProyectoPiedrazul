package com.piedrazul.gestioncitasmedicas.controller;

import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IUsuarioService;
import com.piedrazul.gestioncitasmedicas.observer.AppEvent;
import com.piedrazul.gestioncitasmedicas.observer.EventBus;
import com.piedrazul.gestioncitasmedicas.observer.Observer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListaUsuariosController implements Observer<UsuarioDTO> {

    @FXML
    private TableView<UsuarioDTO> tablaUsuarios;

    @Autowired
    private IUsuarioService usuarioService;
    @Autowired private EventBus eventBus;

    @FXML
    public void initialize() {
        eventBus.subscribe(AppEvent.USUARIO_CREADO,       this);
        eventBus.subscribe(AppEvent.USUARIO_ACTUALIZADO,  this);
        eventBus.subscribe(AppEvent.USUARIO_DESACTIVADO,  this);
        cargarUsuarios();
    }

    @Override
    public void onEvent(AppEvent event, UsuarioDTO data) {
        Platform.runLater(this::cargarUsuarios);
    }

    private void cargarUsuarios() {
        tablaUsuarios.setItems(
                FXCollections.observableArrayList(usuarioService.listarTodos())
        );
    }
}
