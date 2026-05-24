package com.piedrazul.msauthservice.infra.client;

import com.piedrazul.msauthservice.application.service.IUsuarioService;
import com.piedrazul.msauthservice.domain.model.dto.response.UsuarioClientResponse;
import org.springframework.stereotype.Component;

/**
 * Patrón Adapter — «Adapter»
 *
 * Adapta la interfaz de UsuarioClient (Feign, contrato HTTP externo)
 * a la interfaz IUsuarioService que espera el dominio de auth.
 *
 * Estructura:
 *   Target    → IUsuarioService
 *   Adaptee   → UsuarioClient  (Feign, ya existente, no se modifica)
 *   Adapter   → esta clase
 *   Client    → AuthServiceImpl
 *
 * AuthServiceImpl no sabe que existe Feign ni HTTP; solo conoce IUsuarioService.
 */
@Component
public class UsuarioFeignAdapter implements IUsuarioService {

    private final UsuarioClient usuarioClient;

    public UsuarioFeignAdapter(UsuarioClient usuarioClient) {
        this.usuarioClient = usuarioClient;
    }

    /**
     * Llama a Feign y mapea la respuesta HTTP al DTO de dominio.
     * Si mañana el servicio de usuarios cambia su contrato,
     * el cambio queda confinado aquí.
     */
    @Override
    public UsuarioInfo buscarPorId(Long usuarioId) {
        UsuarioClientResponse response = usuarioClient.buscarPorId(usuarioId);
        return new UsuarioInfo(
                response.getId(),
                response.getNombreCompleto(),
                response.getRol().name()
        );
    }
}
