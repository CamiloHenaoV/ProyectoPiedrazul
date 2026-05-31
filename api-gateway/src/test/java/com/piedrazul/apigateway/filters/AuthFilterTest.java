package com.piedrazul.apigateway.filters;

import com.piedrazul.apigateway.dto.TokenValidationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthFilter — Seguridad del API Gateway")
class AuthFilterTest {

    // ── Mocks de la cadena WebClient ─────────────────────────────────────
    // Se declaran como raw types para que Mockito pueda resolver el tipo
    // sin conflictos entre <?> y los raw types que usa thenReturn internamente.
    @Mock private WebClient.Builder              webClientBuilder;
    @Mock private WebClient                      webClient;
    @SuppressWarnings("rawtypes")
    @Mock private WebClient.RequestHeadersUriSpec uriSpec;
    @SuppressWarnings("rawtypes")
    @Mock private WebClient.RequestHeadersSpec    headersSpec;
    @Mock private WebClient.ResponseSpec          responseSpec;

    private AuthFilter filter;

    /** Chain que siempre avanza (simula el siguiente filtro exitoso). */
    private final org.springframework.cloud.gateway.filter.GatewayFilterChain chainOk =
            exchange -> Mono.empty();

    @BeforeEach
    void setUp() {
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        filter = new AuthFilter(webClientBuilder);
    }

    // ═════════════════════════════════════════════════════════════════════
    // Rutas públicas — pasan sin validar token
    // ═════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("Rutas públicas — deben pasar sin validar token")
    class RutasPublicasTest {

        @ParameterizedTest(name = "ruta=''{0}''")
        @ValueSource(strings = {
            "/api/auth/login",
            "/api/auth/registro",
            "/api/auth/refresh",
            "/api/users/registro/paciente",
            "/api/users/registro/profesional",
            "/api/users/registro/usuario"
        })
        @DisplayName("Rutas públicas pasan la cadena sin contactar al auth-service")
        void rutaPublica_pasaSinValidarToken(String path) {
            ServerWebExchange exchange = exchange(HttpMethod.POST, path);

            StepVerifier.create(filter.filter(exchange, chainOk))
                    .verifyComplete();

            verifyNoInteractions(webClient);
        }

        @Test
        @DisplayName("Ruta /api/auth/login sin Authorization header pasa igualmente")
        void loginSinHeader_pasaSinError() {
            ServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.post("/api/auth/login").build());

            StepVerifier.create(filter.filter(exchange, chainOk))
                    .verifyComplete();
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // Rutas protegidas — sin header Authorization
    // ═════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("Rutas protegidas — sin header Authorization")
    class SinHeaderAuthorizationTest {

        @Test
        @DisplayName("Retorna 401 cuando no hay header Authorization")
        void sinHeader_retorna401() {
            ServerWebExchange exchange = exchange(HttpMethod.GET, "/api/scheduling/citas");

            StepVerifier.create(filter.filter(exchange, chainOk))
                    .verifyComplete();

            assertThat(exchange.getResponse().getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Retorna 401 cuando el header Authorization está vacío")
        void headerVacio_retorna401() {
            ServerWebExchange exchange = exchangeConHeader(
                    HttpMethod.GET, "/api/users/usuarios", "");

            StepVerifier.create(filter.filter(exchange, chainOk))
                    .verifyComplete();

            assertThat(exchange.getResponse().getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @ParameterizedTest(name = "header=''{0}''")
        @ValueSource(strings = {
            "Basic dXNlcjpwYXNz",
            "Token abc123",
            "bearer token",
            "Bearer",
        })
        @DisplayName("Retorna 401 cuando el header no empieza con 'Bearer '")
        void headerSinPrefijoCorrecto_retorna401(String headerValue) {
            ServerWebExchange exchange = exchangeConHeader(
                    HttpMethod.GET, "/api/users/usuarios", headerValue);

            StepVerifier.create(filter.filter(exchange, chainOk))
                    .verifyComplete();

            assertThat(exchange.getResponse().getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
            verifyNoInteractions(webClient);
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // Rutas protegidas — con token válido
    // ═════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("Rutas protegidas — con token válido")
    class TokenValidoTest {

        @BeforeEach
        void stubWebClientValido() {
            TokenValidationResponse resp = tokenResponse(true, 1L, "jperez", "paciente");
            stubWebClient(resp);
        }

        @Test
        @DisplayName("Pasa la cadena cuando el auth-service confirma el token como válido")
        void tokenValido_pasaCadena() {
            ServerWebExchange exchange = exchangeConBearer(
                    HttpMethod.GET, "/api/scheduling/citas/paciente/1", "jwt-valido");

            StepVerifier.create(filter.filter(exchange, chainOk))
                    .verifyComplete();

            assertThat(exchange.getResponse().getStatusCode())
                    .isNotEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Inyecta X-User-Id en la request mutada")
        void tokenValido_inyectaXUserId() {
            var capturedId = new String[1];
            org.springframework.cloud.gateway.filter.GatewayFilterChain inspectChain =
                    ex -> {
                        capturedId[0] = ex.getRequest().getHeaders().getFirst("X-User-Id");
                        return Mono.empty();
                    };

            ServerWebExchange exchange = exchangeConBearer(
                    HttpMethod.GET, "/api/users/usuarios", "jwt-valido");

            StepVerifier.create(filter.filter(exchange, inspectChain))
                    .verifyComplete();

            assertThat(capturedId[0]).isEqualTo("1");
        }

        @Test
        @DisplayName("Inyecta X-User-Role en la request mutada")
        void tokenValido_inyectaXUserRole() {
            var capturedRole = new String[1];
            org.springframework.cloud.gateway.filter.GatewayFilterChain inspectChain =
                    ex -> {
                        capturedRole[0] = ex.getRequest().getHeaders().getFirst("X-User-Role");
                        return Mono.empty();
                    };

            ServerWebExchange exchange = exchangeConBearer(
                    HttpMethod.GET, "/api/users/usuarios", "jwt-valido");

            StepVerifier.create(filter.filter(exchange, inspectChain))
                    .verifyComplete();

            assertThat(capturedRole[0]).isEqualTo("paciente");
        }

        @Test
        @DisplayName("X-User-Id refleja el usuarioId del response del auth-service")
        void tokenValido_xUserIdCoincideConUsuarioId() {
            TokenValidationResponse resp = tokenResponse(true, 99L, "admin", "administrador");
            stubWebClient(resp);

            var capturedId = new String[1];
            org.springframework.cloud.gateway.filter.GatewayFilterChain inspectChain =
                    ex -> {
                        capturedId[0] = ex.getRequest().getHeaders().getFirst("X-User-Id");
                        return Mono.empty();
                    };

            ServerWebExchange exchange = exchangeConBearer(
                    HttpMethod.GET, "/api/scheduling/citas", "jwt-admin");

            StepVerifier.create(filter.filter(exchange, inspectChain))
                    .verifyComplete();

            assertThat(capturedId[0]).isEqualTo("99");
        }

        @Test
        @DisplayName("X-User-Role refleja el rol del response del auth-service")
        void tokenValido_xUserRoleCoincideConRol() {
            TokenValidationResponse resp = tokenResponse(true, 2L, "prof1", "profesional");
            stubWebClient(resp);

            var capturedRole = new String[1];
            org.springframework.cloud.gateway.filter.GatewayFilterChain inspectChain =
                    ex -> {
                        capturedRole[0] = ex.getRequest().getHeaders().getFirst("X-User-Role");
                        return Mono.empty();
                    };

            ServerWebExchange exchange = exchangeConBearer(
                    HttpMethod.GET, "/api/scheduling/disponibilidad", "jwt-prof");

            StepVerifier.create(filter.filter(exchange, inspectChain))
                    .verifyComplete();

            assertThat(capturedRole[0]).isEqualTo("profesional");
        }

        @Test
        @DisplayName("Reenvía el header Authorization al auth-service intacto")
        void tokenValido_reenviaAuthHeaderAlAuthService() {
            ServerWebExchange exchange = exchangeConBearer(
                    HttpMethod.GET, "/api/users/usuarios", "mi-jwt-token");

            StepVerifier.create(filter.filter(exchange, chainOk))
                    .verifyComplete();

            verify(headersSpec).header(HttpHeaders.AUTHORIZATION, "Bearer mi-jwt-token");
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // Rutas protegidas — con token inválido (auth-service responde valido=false)
    // ═════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("Rutas protegidas — con token inválido")
    class TokenInvalidoTest {

        @BeforeEach
        void stubWebClientInvalido() {
            TokenValidationResponse resp = tokenResponse(false, null, null, null);
            stubWebClient(resp);
        }

        @Test
        @DisplayName("Retorna 401 cuando el auth-service responde valido=false")
        void tokenInvalido_retorna401() {
            ServerWebExchange exchange = exchangeConBearer(
                    HttpMethod.GET, "/api/scheduling/citas/paciente/1", "jwt-expirado");

            StepVerifier.create(filter.filter(exchange, chainOk))
                    .verifyComplete();

            assertThat(exchange.getResponse().getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("No pasa la cadena de filtros cuando el token es inválido")
        void tokenInvalido_noAvanzaLaCadena() {
            boolean[] chainCalled = {false};
            org.springframework.cloud.gateway.filter.GatewayFilterChain guardChain =
                    ex -> {
                        chainCalled[0] = true;
                        return Mono.empty();
                    };

            ServerWebExchange exchange = exchangeConBearer(
                    HttpMethod.GET, "/api/scheduling/citas", "jwt-invalido");

            StepVerifier.create(filter.filter(exchange, guardChain))
                    .verifyComplete();

            assertThat(chainCalled[0]).isFalse();
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // Error de red al contactar el auth-service
    // ═════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("Error de red al contactar el auth-service")
    class ErrorDeRedTest {

        @Test
        @DisplayName("Retorna 401 cuando el auth-service lanza una excepción (timeout, conexión rechazada…)")
        void errorDeRed_retorna401() {
            stubWebClientConError(new RuntimeException("Connection refused"));

            ServerWebExchange exchange = exchangeConBearer(
                    HttpMethod.GET, "/api/users/usuarios", "jwt-cualquiera");

            StepVerifier.create(filter.filter(exchange, chainOk))
                    .verifyComplete();

            assertThat(exchange.getResponse().getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("No propaga la excepción al caller cuando el auth-service falla")
        void errorDeRed_noPropagaExcepcion() {
            stubWebClientConError(new java.net.ConnectException("Timeout"));

            ServerWebExchange exchange = exchangeConBearer(
                    HttpMethod.GET, "/api/scheduling/disponibilidad", "jwt");

            StepVerifier.create(filter.filter(exchange, chainOk))
                    .verifyComplete();
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // getOrder()
    // ═════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getOrder()")
    class OrderTest {

        @Test
        @DisplayName("getOrder() retorna -1 (ejecuta antes que filtros de orden 0)")
        void getOrder_retornaMinusUno() {
            assertThat(filter.getOrder()).isEqualTo(-1);
        }

        @Test
        @DisplayName("AuthFilter tiene prioridad menor que LoggingFilter (order -1 > -2)")
        void authFilter_tieneOrdenMayorQueLoggingFilter() {
            LoggingFilter logging = new LoggingFilter();
            assertThat(filter.getOrder()).isGreaterThan(logging.getOrder());
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // Helpers
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Stub de la cadena WebClient que devuelve una respuesta de validación.
     * Los mocks se declaran como raw types (@Mock sin <?>), por lo que
     * no es necesario ningún cast explícito aquí.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubWebClient(TokenValidationResponse response) {
        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.header(anyString(), anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TokenValidationResponse.class))
                .thenReturn(Mono.just(response));
    }

    /** Stub de la cadena WebClient que simula un error de red. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubWebClientConError(Throwable error) {
        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.header(anyString(), anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TokenValidationResponse.class))
                .thenReturn(Mono.error(error));
    }

    private TokenValidationResponse tokenResponse(boolean valido, Long userId,
                                                   String login, String rol) {
        TokenValidationResponse r = new TokenValidationResponse();
        r.setValido(valido);
        r.setUsuarioId(userId);
        r.setLogin(login);
        r.setRol(rol);
        return r;
    }

    private ServerWebExchange exchange(HttpMethod method, String path) {
        return MockServerWebExchange.from(
                MockServerHttpRequest.method(method, path).build());
    }

    private ServerWebExchange exchangeConHeader(HttpMethod method, String path, String header) {
        return MockServerWebExchange.from(
                MockServerHttpRequest.method(method, path)
                        .header(HttpHeaders.AUTHORIZATION, header)
                        .build());
    }

    private ServerWebExchange exchangeConBearer(HttpMethod method, String path, String token) {
        return exchangeConHeader(method, path, "Bearer " + token);
    }
}
