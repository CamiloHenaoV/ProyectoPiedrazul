package com.piedrazul.gestioncitasmedicas.service;

import com.piedrazul.gestioncitasmedicas.model.services.impl.BCryptPasswordServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BCryptPasswordServiceImplTest {

    private final BCryptPasswordServiceImpl passwordService = new BCryptPasswordServiceImpl();

    @Test
    void shouldEncryptPassword() {
        String hash = passwordService.encriptar("12345678");

        assertNotNull(hash);
        assertNotEquals("12345678", hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"));
    }

    @Test
    void shouldVerifyCorrectPassword() {
        String hash = passwordService.encriptar("12345678");

        assertTrue(passwordService.verificar("12345678", hash));
    }

    @Test
    void shouldRejectWrongPassword() {
        String hash = passwordService.encriptar("12345678");

        assertFalse(passwordService.verificar("incorrecta", hash));
    }
}