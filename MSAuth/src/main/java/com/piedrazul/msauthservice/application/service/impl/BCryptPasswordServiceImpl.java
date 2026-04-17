package com.piedrazul.msauthservice.application.service.impl;

import com.piedrazul.msauthservice.application.service.IPasswordService;
import com.piedrazul.msauthservice.infra.exception.PasswordInvalidaException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de encriptación usando BCrypt.
 * Migrado desde el monolito — misma lógica, nuevo paquete.
 */
@Service
public class BCryptPasswordServiceImpl implements IPasswordService {

    private static final int PASSWORD_MIN_LENGTH = 8;
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

    @Override
    public void validarFormato(String password) {
        if (password == null || password.length() < PASSWORD_MIN_LENGTH) {
            throw new PasswordInvalidaException(
                    "La contraseña debe tener al menos " + PASSWORD_MIN_LENGTH + " caracteres");
        }
        if (!password.matches("^(?=.*[A-Z])(?=.*\\d).+$")) {
            throw new PasswordInvalidaException(
                    "La contraseña debe contener al menos una mayúscula y un número");
        }
    }
}
