package com.piedrazul.msauthservice.application.service;

import com.piedrazul.msauthservice.domain.model.dto.request.CambioPasswordRequest;
import com.piedrazul.msauthservice.domain.model.dto.request.LoginRequest;
import com.piedrazul.msauthservice.domain.model.dto.request.RegistroCredencialRequest;
import com.piedrazul.msauthservice.domain.model.dto.request.RefreshTokenRequest;
import com.piedrazul.msauthservice.domain.model.dto.response.AuthResponse;
import com.piedrazul.msauthservice.domain.model.dto.response.TokenValidationResponse;

public interface IAuthService {
    /** Autentica al usuario y devuelve access + refresh token */
    AuthResponse login(LoginRequest request);
    /** Renueva el access token usando un refresh token válido */
    AuthResponse refresh(RefreshTokenRequest request);
    /** Revoca el refresh token del usuario (logout de sesión actual) */
    void logout(RefreshTokenRequest request);
    /** Revoca todos los refresh tokens del usuario (logout de todas las sesiones) */
    void logoutAll(Long usuarioId);
    /** Registra credenciales para un usuario recién creado */
    void registrarCredencial(RegistroCredencialRequest request);
    /** Cambia la contraseña verificando la actual */
    void cambiarPassword(Long usuarioId, CambioPasswordRequest request);
    /** Valida un JWT — usado principalmente por el API Gateway */
    TokenValidationResponse validarToken(String token);
}
