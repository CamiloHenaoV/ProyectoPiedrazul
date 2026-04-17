package com.piedrazul.msauthservice.application.service.impl;

import com.piedrazul.msauthservice.application.service.IAuthService;
import com.piedrazul.msauthservice.application.service.IPasswordService;
import com.piedrazul.msauthservice.domain.model.dto.request.*;
import com.piedrazul.msauthservice.domain.model.dto.response.AuthResponse;
import com.piedrazul.msauthservice.domain.model.dto.response.TokenValidationResponse;
import com.piedrazul.msauthservice.domain.model.dto.response.UsuarioClientResponse;
import com.piedrazul.msauthservice.domain.model.entity.Credencial;
import com.piedrazul.msauthservice.domain.model.entity.RefreshToken;
import com.piedrazul.msauthservice.domain.model.repository.CredencialRepository;
import com.piedrazul.msauthservice.domain.model.repository.RefreshTokenRepository;
import com.piedrazul.msauthservice.infra.client.UsuarioClient;
import com.piedrazul.msauthservice.infra.exception.*;
import com.piedrazul.msauthservice.infra.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final CredencialRepository credencialRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final IPasswordService passwordService;
    private final JwtUtil jwtUtil;
    private final UsuarioClient usuarioClient;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 1. Buscar credenciales por login
        Credencial credencial = credencialRepository.findByLogin(request.getLogin())
                .orElseThrow(CredencialesInvalidasException::new);

        // 2. Verificar que la cuenta esté activa
        if (!credencial.getActivo()) {
            throw new CredencialesInvalidasException();
        }

        // 3. Verificar contraseña
        if (!passwordService.verificar(request.getPassword(), credencial.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        // 4. Obtener rol desde usuario-service (solo en login, no en cada request)
        UsuarioClientResponse usuario = usuarioClient.buscarPorId(credencial.getUsuarioId());

        // 5. Generar tokens
        String accessToken = jwtUtil.generarAccessToken(
                credencial.getUsuarioId(),
                credencial.getLogin(),
                usuario.getRol().name()
        );
        String refreshTokenValor = generarYPersistirRefreshToken(credencial.getUsuarioId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValor)
                .tipo("Bearer")
                .expiresIn(accessExpiration)
                .usuarioId(credencial.getUsuarioId())
                .login(credencial.getLogin())
                .rol(usuario.getRol().name())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(RefreshTokenInvalidoException::new);

        if (!refreshToken.esValido()) {
            throw new RefreshTokenInvalidoException();
        }

        // Rotación: marcar el token actual como usado
        refreshToken.setUsado(true);
        refreshTokenRepository.save(refreshToken);

        // Obtener credencial y rol actualizado
        Credencial credencial = credencialRepository.findByUsuarioId(refreshToken.getUsuarioId())
                .orElseThrow(CredencialesInvalidasException::new);

        if (!credencial.getActivo()) {
            throw new CredencialesInvalidasException();
        }

        UsuarioClientResponse usuario = usuarioClient.buscarPorId(credencial.getUsuarioId());

        String nuevoAccessToken = jwtUtil.generarAccessToken(
                credencial.getUsuarioId(),
                credencial.getLogin(),
                usuario.getRol().name()
        );
        String nuevoRefreshToken = generarYPersistirRefreshToken(credencial.getUsuarioId());

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

        // Revocar todos los tokens activos para forzar re-login en otros dispositivos
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


    private String generarYPersistirRefreshToken(Long usuarioId) {
        String valor = UUID.randomUUID().toString();
        RefreshToken rt = RefreshToken.builder()
                .usuarioId(usuarioId)
                .token(valor)
                .expiraEn(ZonedDateTime.now().plusSeconds(refreshExpiration / 1000))
                .build();
        refreshTokenRepository.save(rt);
        return valor;
    }
}
