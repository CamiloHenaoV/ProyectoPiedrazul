package com.piedrazul.frontend.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.piedrazul.frontend.http.ApiClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.model.dto.LoginRequestDTO;
import com.piedrazul.frontend.model.dto.LoginResponseDTO;

import java.net.http.HttpResponse;

/**
 * Cliente HTTP para el auth-service.
 *
 * Rutas esperadas en el API Gateway:
 *   POST /auth/login  → devuelve { token, usuario }
 *
 * Reemplaza IAuthService del monolito.
 */
public class AuthClient {

    private final ApiClient api;

    public AuthClient(ApiClient api) {
        this.api = api;
    }

    /**
     * Autentica al usuario y devuelve el token JWT junto con los datos del usuario.
     *
     * @throws HttpException con 401 si las credenciales son inválidas.
     */
    public LoginResponseDTO login(String login, String password) throws Exception {
        LoginRequestDTO body = new LoginRequestDTO(login, password);
        HttpResponse<String> response = api.post("/auth/login", body);

        if (response.statusCode() == 401)
            throw new HttpException(401, "Credenciales inválidas.");

        if (!api.isSuccess(response))
            throw new HttpException(response.statusCode(),
                    "Error al autenticar: " + response.statusCode());

        return api.mapper.readValue(response.body(), LoginResponseDTO.class);
    }
}
