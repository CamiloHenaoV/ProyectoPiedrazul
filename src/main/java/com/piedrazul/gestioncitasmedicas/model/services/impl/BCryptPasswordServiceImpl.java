package com.piedrazul.gestioncitasmedicas.model.services.impl;

import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IPasswordService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de encriptación usando el algoritmo BCrypt.
 * <p>
 * BCrypt es un algoritmo de hashing adaptativo
 */

@Service
public class BCryptPasswordServiceImpl implements IPasswordService {

    private final BCryptPasswordEncoder encoder;
    /**
     * Inicializa el encoder con la configuración por defecto de BCrypt
     * (factor de costo 10).
     */
    public BCryptPasswordServiceImpl() {
        this.encoder = new BCryptPasswordEncoder();
    }
    /**
     * Encripta una contraseña en texto plano usando BCrypt.
     * <p>
     * Cada llamada genera un hash diferente para la misma contraseña
     * gracias al salt aleatorio que BCrypt incorpora internamente.
     *
     * @param passwordPlano contraseña en texto plano a encriptar
     * @return hash BCrypt listo para almacenar en BD
     */
    @Override
    public String encriptar(String passwordPlano) {
        return encoder.encode(passwordPlano);
    }
    /**
     * Verifica si una contraseña en texto plano coincide con un hash BCrypt.
     * <p>
     * Extrae el salt del hash almacenado y lo usa para comparar
     * con la contraseña ingresada — no es necesario guardar el salt
     * por separado.
     *
     * @param passwordPlano contraseña ingresada por el usuario
     * @param hash          hash almacenado en la base de datos
     * @return {@code true} si la contraseña coincide, {@code false} si no
     */
    @Override
    public boolean verificar(String passwordPlano, String hash) {
        return encoder.matches(passwordPlano, hash);
    }
}