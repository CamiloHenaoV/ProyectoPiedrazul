package com.piedrazul.msauthservice.application.service.impl;

import com.piedrazul.msauthservice.application.service.IAuthService;
import com.piedrazul.msauthservice.application.service.IPasswordService;
import com.piedrazul.msauthservice.domain.model.dto.request.CambioPasswordRequest;
import com.piedrazul.msauthservice.domain.model.dto.request.LoginRequest;
import com.piedrazul.msauthservice.domain.model.dto.request.RefreshTokenRequest;
import com.piedrazul.msauthservice.domain.model.dto.request.RegistroCredencialRequest;
import com.piedrazul.msauthservice.domain.model.dto.response.AuthResponse;
import com.piedrazul.msauthservice.domain.model.dto.response.TokenValidationResponse;
import com.piedrazul.msauthservice.domain.model.dto.response.UsuarioClientResponse;
import com.piedrazul.msauthservice.domain.model.entity.Credencial;
import com.piedrazul.msauthservice.domain.model.repository.CredencialRepository;
import com.piedrazul.msauthservice.domain.model.repository.RefreshTokenRepository;
import com.piedrazul.msauthservice.infra.client.UsuarioClient;
import com.piedrazul.msauthservice.infra.exception.CredencialDuplicadaException;
import com.piedrazul.msauthservice.infra.exception.CredencialesInvalidasException;
import com.piedrazul.msauthservice.infra.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final CredencialRepository credencialRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final IPasswordService passwordService;
    private final JwtUtil jwtUtil;
    private final UsuarioClient usuarioClient;

    // FIX: AuthTransactionHelper owns the @Transactional DB boundaries for login
    // and refresh, so we can call Feign *between* transactions instead of inside one.
    private final AuthTransactionHelper txHelper;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // -------------------------------------------------------------------------
    // LOGIN
    // No longer @Transactional at this level. The two DB units of work are
    // delegated to AuthTransactionHelper, each in its own short transaction.
    // The Feign call executes between them, holding zero DB connections.
    //
    // Flow:
    //   TX-1 (read-only)  : validate credentials          → connection released
    //   [network]         : fetch role from user-service  → no connection held
    //   TX-2 (write)      : persist refresh token         → connection released
    // -------------------------------------------------------------------------
    @Override
    public AuthResponse login(LoginRequest request) {
        // TX-1: pure DB work, connection freed on return
        Credencial credencial = txHelper.verificarCredencialLogin(request);

        // Network call – no transaction / no DB connection open
        UsuarioClientResponse usuario = usuarioClient.buscarPorId(credencial.getUsuarioId());

        // TX-2: pure DB write, connection freed on return
        String refreshTokenValor = txHelper.persistirRefreshToken(credencial.getUsuarioId());

        String accessToken = jwtUtil.generarAccessToken(
                credencial.getUsuarioId(),
                credencial.getLogin(),
                usuario.getRol().name()
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValor)
                .tipo("Bearer")
                .expiresIn(accessExpiration)
                .usuarioId(credencial.getUsuarioId())
                .login(credencial.getLogin())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(usuario.getRol().name())
                .build();
    }

    // -------------------------------------------------------------------------
    // REFRESH
    // Same pattern: short DB transactions around the Feign call.
    //
    // Flow:
    //   TX-1 (read-write) : validate + rotate old token   → connection released
    //   [network]         : fetch updated role             → no connection held
    //   TX-2 (write)      : persist new refresh token     → connection released
    // -------------------------------------------------------------------------
    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        // TX-1: validate old token, mark it used, return the associated Credencial
        Credencial credencial = txHelper.rotarRefreshToken(request.getRefreshToken());

        // Network call – no transaction / no DB connection open
        UsuarioClientResponse usuario = usuarioClient.buscarPorId(credencial.getUsuarioId());

        // TX-2: persist the new refresh token
        String nuevoRefreshToken = txHelper.persistirRefreshToken(credencial.getUsuarioId());

        String nuevoAccessToken = jwtUtil.generarAccessToken(
                credencial.getUsuarioId(),
                credencial.getLogin(),
                usuario.getRol().name()
        );

        return AuthResponse.builder()
                .accessToken(nuevoAccessToken)
                .refreshToken(nuevoRefreshToken)
                .tipo("Bearer")
                .expiresIn(accessExpiration)
                .usuarioId(credencial.getUsuarioId())
                .login(credencial.getLogin())
                .rol(usuario.getRol().name())
                .build();
    }

    // -------------------------------------------------------------------------
    // The methods below are purely DB operations with no external calls,
    // so a single @Transactional per method is correct and safe.
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
