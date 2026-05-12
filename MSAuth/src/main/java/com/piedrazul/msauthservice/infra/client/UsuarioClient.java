package com.piedrazul.msauthservice.infra.client;

import com.piedrazul.msauthservice.domain.model.dto.response.UsuarioClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente Feign hacia ms-usuario-service.
 * Solo se usa al hacer login para obtener el rol
 * y construir el JWT con los claims correctos.
 *
 * El nombre "ms-usuario-service" debe coincidir exactamente
 * con spring.application.name del servicio de usuarios en Eureka.
 */
@FeignClient(name = "user-service", url = "${user-management.url}")
public interface UsuarioClient {

    @GetMapping("/api/users/usuarios/{id}")
    UsuarioClientResponse buscarPorId(@PathVariable("id") Long id);
}
