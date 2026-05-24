package com.piedrazul.msauthservice.application.service;

/**
 * Abstracción del dominio para consultar datos de usuario.
 *
 * Patrón Adapter — «Target»
 *
 * AuthServiceImpl depende de esta interfaz, NO de UsuarioClient (Feign).
 * Si el microservicio de usuarios cambia su protocolo (REST → gRPC, otro
 * endpoint, etc.), solo se modifica UsuarioFeignAdapter; el servicio de
 * negocio no se toca.
 */
public interface IUsuarioService {

    /**
     * Devuelve la información básica de un usuario dado su ID.
     *
     * @param usuarioId identificador del usuario en ms-usuario-service
     * @return datos del usuario que auth-service necesita para generar el JWT
     */
    UsuarioInfo buscarPorId(Long usuarioId);

    /**
     * DTO interno que representa los datos de usuario
     * que AuthServiceImpl necesita. Independiente del contrato HTTP.
     */
    record UsuarioInfo(Long id, String nombreCompleto, String rol) {}
}
