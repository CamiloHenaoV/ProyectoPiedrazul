package com.piedraazul.gestioncitasmedicas;

public enum FxmlView {
    MAIN("/views/main.fxml", "Gestión de Citas - Inicio"),
    CITA_FORM("/views/cita-form.fxml", "Nueva Cita Médica");

    private final String path;
    private final String title;

    FxmlView(String path, String title) {
        this.path = path;
        this.title = title;
    }

    public String getPath() { return path; }
    public String getTitle() { return title; }
}
