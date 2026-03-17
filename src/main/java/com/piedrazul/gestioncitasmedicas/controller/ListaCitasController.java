package com.piedrazul.gestioncitasmedicas.controller;

import com.piedrazul.gestioncitasmedicas.model.dto.CitaDTO;
import com.piedrazul.gestioncitasmedicas.model.dto.PacienteDTO;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.ICitaService;
import com.piedrazul.gestioncitasmedicas.observer.AppEvent;
import com.piedrazul.gestioncitasmedicas.observer.EventBus;
import com.piedrazul.gestioncitasmedicas.observer.Observer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import org.springframework.stereotype.Component;

@Component
public class ListaCitasController implements Observer<CitaDTO> {

    @FXML
    private TableView<CitaDTO> tablaCitas;

    private final ICitaService citaService;
    private final EventBus eventBus;
    public ListaCitasController(ICitaService citaService,EventBus eventBus){
        this.citaService= citaService;
        this.eventBus=eventBus;
    }
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
                )
        );
    }
}
