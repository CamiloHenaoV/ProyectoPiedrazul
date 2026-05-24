package com.piedrazul.msauthservice.application.service.impl;

import com.piedrazul.msauthservice.application.service.IAuthService;
import com.piedrazul.msauthservice.application.service.IPasswordService;
import com.piedrazul.msauthservice.application.service.IUsuarioService;                   // NUEVO
import com.piedrazul.msauthservice.application.service.IUsuarioService.UsuarioInfo;      // NUEVO
import com.piedrazul.msauthservice.domain.model.dto.request.CambioPasswordRequest;
import com.piedrazul.msauthservice.domain.model.dto.request.LoginRequest;
import com.piedrazul.msauthservice.domain.model.dto.request.RefreshTokenRequest;
import com.piedrazul.msauthservice.domain.model.dto.request.RegistroCredencialRequest;
import com.piedrazul.msauthservice.domain.model.dto.response.AuthResponse;
import com.piedrazul.msauthservice.domain.model.dto.response.TokenValidationResponse;
import com.piedrazul.msauthservice.domain.model.entity.Credencial;
import com.piedrazul.msauthservice.domain.model.repository.CredencialRepository;
import com.piedrazul.msauthservice.domain.model.repository.RefreshTokenRepository;
import com.piedrazul.msauthservice.infra.exception.CredencialDuplicadaException;
import com.piedrazul.msauthservice.infra.exception.CredencialesInvalidasException;
import com.piedrazul.msauthservice.infra.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Patrón Adapter — «Client»
 *
 * CAMBIOS respecto al original:
 *   - Se elimina la dependencia directa de UsuarioClient (Feign)
 *   - Se inyecta IUsuarioService en su lugar
 *   - Los dos métodos que llamaban a usuarioClient ahora llaman a usuarioService
 *   - El resto del archivo es idéntico al original
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final CredencialRepository      credencialRepository;
    private final RefreshTokenRepository    refreshTokenRepository;
    private final IPasswordService          passwordService;
    private final JwtUtil                   jwtUtil;
    private final IUsuarioService           usuarioService;   // ← CAMBIADO: era UsuarioClient
    private final AuthTransactionHelper     txHelper;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // -------------------------------------------------------------------------
    // LOGIN
    // -------------------------------------------------------------------------
    @Override
    public AuthResponse login(LoginRequest request) {
        Credencial credencial = txHelper.verificarCredencialLogin(request);

        // CAMBIADO: antes era usuarioClient.buscarPorId(...)
        // Ahora habla con la abstracción; no sabe que existe Feign
        UsuarioInfo usuario = usuarioService.buscarPorId(credencial.getUsuarioId());

        String refreshTokenValor = txHelper.persistirRefreshToken(credencial.getUsuarioId());

        // CAMBIADO: antes era usuario.getRol().name() — ahora usuario.rol() (record)
        String accessToken = jwtUtil.generarAccessToken(
                credencial.getUsuarioId(),
                credencial.getLogin(),
                usuario.rol()
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValor)
                .tipo("Bearer")
                .expiresIn(accessExpiration)
                .usuarioId(credencial.getUsuarioId())
                .login(credencial.getLogin())
                .nombreCompleto(usuario.nombreCompleto())  // CAMBIADO: era .getNombreCompleto()
                .rol(usuario.rol())                        // CAMBIADO: era .getRol().name()
                .build();
    }

    // -------------------------------------------------------------------------
    // REFRESH
    // -------------------------------------------------------------------------
    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        Credencial credencial = txHelper.rotarRefreshToken(request.getRefreshToken());

        // CAMBIADO: antes era usuarioClient.buscarPorId(...)
        UsuarioInfo usuario = usuarioService.buscarPorId(credencial.getUsuarioId());

        String nuevoRefreshToken = txHelper.persistirRefreshToken(credencial.getUsuarioId());

        String nuevoAccessToken = jwtUtil.generarAccessToken(
                credencial.getUsuarioId(),
                credencial.getLogin(),
                usuario.rol()                              // CAMBIADO
        );

        return AuthResponse.builder()
                .accessToken(nuevoAccessToken)
                .refreshToken(nuevoRefreshToken)
                .tipo("Bearer")
                .expiresIn(accessExpiration)
                .usuarioId(credencial.getUsuarioId())
                .login(credencial.getLogin())
                .rol(usuario.rol())                        // CAMBIADO
                .build();
    }

    // -------------------------------------------------------------------------
    // Los métodos de abajo no cambian en absoluto — se copian del original
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(rt -> {
                    rt.setRevocado(true);
                    refreshTokenRepository.save(rt);
                });
    }

    @Override
    @Transactional
    public void logoutAll(Long usuarioId) {
        refreshTokenRepository.revocarTodosPorUsuarioId(usuarioId);
    }

    @Override
    @Transactional
    public void registrarCredencial(RegistroCredencialRequest request) {
        if (credencialRepository.existsByUsuarioId(request.getUsuarioId())) {
            throw new CredencialDuplicadaException(
                    "Ya existen credenciales para el usuario: " + request.getUsuarioId());
        }
        if (credencialRepository.existsByLogin(request.getLogin())) {
            throw new CredencialDuplicadaException(
                    "El login ya está en uso: " + request.getLogin());
        }

        passwordService.validarFormato(request.getPassword());

        Credencial credencial = Credencial.builder()
                .usuarioId(request.getUsuarioId())
                .login(request.getLogin())
                .passwordHash(passwordService.encriptar(request.getPassword()))
                .activo(true)
                .build();

        credencialRepository.save(credencial);
    }

    @Override
    @Transactional
    public void cambiarPassword(Long usuarioId, CambioPasswordRequest request) {
        Credencial credencial = credencialRepository.findByUsuarioId(usuarioId)
                .orElseThrow(CredencialesInvalidasException::new);

        if (!passwordService.verificar(request.getPasswordActual(), credencial.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        passwordService.validarFormato(request.getPasswordNuevo());

        credencial.setPasswordHash(passwordService.encriptar(request.getPasswordNuevo()));
        credencialRepository.save(credencial);

        refreshTokenRepository.revocarTodosPorUsuarioId(usuarioId);
    }

    @Override
    public TokenValidationResponse validarToken(String token) {
        if (!jwtUtil.esValido(token)) {
            return TokenValidationResponse.builder().valido(false).build();
        }
        return TokenValidationResponse.builder()
                .valido(true)
                .usuarioId(jwtUtil.extraerUsuarioId(token))
                .login(jwtUtil.extraerLogin(token))
                .rol(jwtUtil.extraerRol(token))
                .build();
    }
}
