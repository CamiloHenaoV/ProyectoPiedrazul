package com.piedrazul.msusermanagement.application.service.template.impl;

import com.piedrazul.msusermanagement.application.service.interfaces.IUsuarioService;
import com.piedrazul.msusermanagement.application.service.template.RegistroTemplate;
import org.springframework.stereotype.Service;

/**
 * Subclase concreta del patrón Template Method para el registro de usuarios
 * con rol <b>ADMINISTRADOR</b>.
 *
 * <p>En este tipo de registro no se crea ningún perfil clínico adicional:
 * solo se persiste el usuario base. Por ello, <strong>no sobrescribe</strong>
 * {@code vincularPerfil} — hereda el no-op definido en {@link RegistroTemplate},
 * que es exactamente el comportamiento correcto.
 *
 * <p>Esto ilustra una característica clave del hook en Template Method: cuando
 * el comportamiento por defecto ya es el correcto, la subclase no necesita
 * agregar ningún código.
 */
@Service
public class RegistroAdminService extends RegistroTemplate {

    public RegistroAdminService(IUsuarioService usuarioService) {
        super(usuarioService);
    }

    /*
     * vincularPerfil — no se sobrescribe intencionalmente.
     * El no-op heredado de RegistroTemplate es el comportamiento correcto
     * para un administrador (no tiene perfil clínico asociado).
     */
}
