package com.piedrazul.msscheduling.infra.sync;

import com.piedrazul.msscheduling.domain.model.entity.UsuarioLocal;
import com.piedrazul.msscheduling.domain.model.entity.enums.RolUsuario;
import com.piedrazul.msscheduling.domain.model.repository.UsuarioLocalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class UserSyncService {

    private final UsuarioLocalRepository usuarioLocalRepository;
    private final WebClient webClient;

    public UserSyncService(UsuarioLocalRepository usuarioLocalRepository,
                           WebClient.Builder webClientBuilder,
                           @Value("${user-management.url}") String userManagementUrl) {
        this.usuarioLocalRepository = usuarioLocalRepository;
        this.webClient = webClientBuilder
                .baseUrl(userManagementUrl)
                .build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void sincronizarUsuarios() {
        log.info("Iniciando sincronización de usuarios desde UserManagement...");
        try {
            webClient.get()
                    .uri("/api/users/usuarios")
                    .retrieve()
                    .bodyToFlux(UsuarioSyncDTO.class)
                    .doOnNext(this::guardarOActualizar)
                    .doOnComplete(() -> log.info("Sincronización de usuarios completada"))
                    .doOnError(e -> log.error("Error en sincronización de usuarios: {}", e.getMessage()))
                    .subscribe(usuario -> log.debug("Sincronizado: {}", usuario.getId()),
                            error -> log.error("Error en sincronización inicial: {}", error.getMessage()));
        } catch (Exception e) {
            log.error("No se pudo conectar con UserManagement para sincronización inicial: {}", e.getMessage());
        }
    }

    private void guardarOActualizar(UsuarioSyncDTO dto) {
        UsuarioLocal usuario = usuarioLocalRepository.findById(dto.getId())
                .orElse(new UsuarioLocal());

        usuario.setId(dto.getId());
        usuario.setNombreCompleto(dto.getNombreCompleto());
        usuario.setLogin(dto.getLogin());
        usuario.setRol(RolUsuario.valueOf(dto.getRol()));
        usuario.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        usuarioLocalRepository.save(usuario);
        log.debug("Usuario sincronizado: id={} nombre={}", dto.getId(), dto.getNombreCompleto());
    }
}
