package com.piedraazul.gestioncitasmedicas.model.services.interfaces;

public interface IPasswordService {
    String encriptar(String passwordPlano);
    boolean verificar(String passwordPlano, String hash);
}