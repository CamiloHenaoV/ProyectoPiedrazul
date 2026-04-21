package com.piedrazul.frontend.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.piedrazul.frontend.config.AppConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Cliente HTTP centralizado para comunicarse con el API Gateway.
 *
 * Todos los microservicios se acceden a través de este cliente.
 * El JWT se inyecta automáticamente desde SessionManager en cada petición.
 *
 * Uso desde un *Client:
 *   String json = apiClient.get("/usuarios");
 *   apiClient.post("/auth/login", body);
 */
public class ApiClient {

    private final String         baseUrl;
    private final HttpClient     http;
    private final SessionManager session;
    public  final ObjectMapper   mapper;

    public ApiClient(AppConfig config, SessionManager session) {
        this.baseUrl = config.getGatewayUrl();
        this.session = session;
        this.http    = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getConnectTimeout()))
                .build();
        this.mapper  = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // ── GET ──────────────────────────────────────────────────────

    public HttpResponse<String> get(String path) throws Exception {
        HttpRequest req = baseRequest(path).GET().build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }

    // ── POST ─────────────────────────────────────────────────────

    public HttpResponse<String> post(String path, Object body) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest req = baseRequest(path)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }

    // ── POST sin body (para login con form params, etc.) ─────────

    public HttpResponse<String> postRaw(String path, String rawBody,
                                        String contentType) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", contentType)
                .header("Accept", "application/json")
                .header("Authorization", bearerToken())
                .POST(HttpRequest.BodyPublishers.ofString(rawBody))
                .build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }

    // ── PUT ──────────────────────────────────────────────────────

    public HttpResponse<String> put(String path, Object body) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest req = baseRequest(path)
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }

    // ── DELETE ───────────────────────────────────────────────────

    public HttpResponse<String> delete(String path) throws Exception {
        HttpRequest req = baseRequest(path).DELETE().build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }

    // ── PATCH ────────────────────────────────────────────────────

    public HttpResponse<String> patch(String path, Object body) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest req = baseRequest(path)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                .build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }

    // ── Utilidades ───────────────────────────────────────────────

    /** Verifica si el status HTTP indica éxito (2xx). */
    public boolean isSuccess(HttpResponse<?> response) {
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }

    private HttpRequest.Builder baseRequest(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", bearerToken());
    }

    private String bearerToken() {
        String token = session.getToken();
        return (token != null && !token.isBlank()) ? "Bearer " + token : "";
    }
}