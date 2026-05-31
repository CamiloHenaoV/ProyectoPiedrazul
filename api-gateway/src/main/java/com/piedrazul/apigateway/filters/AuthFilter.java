package com.piedrazul.apigateway.filters;

import com.piedrazul.apigateway.dto.TokenValidationResponse;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private final WebClient webClient;

    public AuthFilter(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8082").build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // 🔹 Rutas públicas (no validar token)
        if (path.contains("/api/auth/login")
                || path.contains("/api/auth/registro")   // registro de credenciales en MSAuth
                || path.contains("/api/auth/refresh")
                // FIX ALTO: logout debe ser público en el gateway.
                // Con el access token expirado el gateway devolvía 401 antes de
                // llegar a MSAuth, dejando el refresh token (válido 7 días) sin
                // revocar. MSAuth valida el refresh token internamente; no
                // necesita un JWT válido para procesar el logout.
                || path.contains("/api/auth/logout")
                || path.contains("/api/auth/logout-all")
                || path.startsWith("/api/users/registro")) { // registro de perfil (paciente/profesional)
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        // 🔹 Llamada al servicio de auth
        return webClient.get()
                .uri("/api/auth/validate")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(TokenValidationResponse.class)
                .flatMap(response -> {
                    if (!response.isValido()) {
                        return unauthorized(exchange);
                    }

                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", response.getUsuarioId().toString())
                            .header("X-User-Role", response.getRol())
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .onErrorResume(e -> unauthorized(exchange));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
