package com.piedrazul.gestioncitasmedicas.app;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class GestionCitasMedicasApplication {

    public static void main(String[] args) {
        Application.launch(JavaFxApplication.class, args);
    }

}
