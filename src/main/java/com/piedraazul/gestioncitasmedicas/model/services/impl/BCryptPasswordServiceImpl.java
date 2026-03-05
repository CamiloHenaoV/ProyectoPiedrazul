package com.piedraazul.gestioncitasmedicas.model.services.impl;

import com.piedraazul.gestioncitasmedicas.model.services.interfaces.IPasswordService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class BCryptPasswordServiceImpl implements IPasswordService {

    private final BCryptPasswordEncoder encoder;

    public BCryptPasswordServiceImpl() {
        this.encoder = new BCryptPasswordEncoder();
    }

    @Override
    public String encriptar(String passwordPlano) {
        return encoder.encode(passwordPlano);
    }

    @Override
    public boolean verificar(String passwordPlano, String hash) {
        return encoder.matches(passwordPlano, hash);
    }
}