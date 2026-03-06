package com.piedraazul.gestioncitasmedicas.controller;

import com.piedraazul.gestioncitasmedicas.model.dto.CitaDTO;
import com.piedraazul.gestioncitasmedicas.model.dto.PacienteDTO;
import com.piedraazul.gestioncitasmedicas.model.services.interfaces.ICitaService;
import com.piedraazul.gestioncitasmedicas.observer.AppEvent;
import com.piedraazul.gestioncitasmedicas.observer.EventBus;
import com.piedraazul.gestioncitasmedicas.observer.Observer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListaCitasController implements Observer<CitaDTO> {

    @FXML
    private TableView<CitaDTO> tablaCitas;

    @Autowired
    private ICitaService citaService;
    @Autowired private EventBus eventBus;

    private PacienteDTO pacienteActual;

    @FXML
    public void initialize() {
        eventBus.subscribe(AppEvent.CITA_AGENDADA,  this);
        eventBus.subscribe(AppEvent.CITA_CANCELADA, this);
        cargarCitas();
    }

    @Override
    public void onEvent(AppEvent event, CitaDTO data) {
        Platform.runLater(this::cargarCitas);
    }

    private void cargarCitas() {
        tablaCitas.setItems(
                FXCollections.observableArrayList(
                        citaService.listarPorPaciente(pacienteActual.getId())
                )
        );
    }
}
