package com.piedrazul.msauthservice.application.service.impl;

import com.piedrazul.msauthservice.application.service.IPasswordService;
import com.piedrazul.msauthservice.domain.model.dto.request.LoginRequest;
import com.piedrazul.msauthservice.domain.model.entity.Credencial;
import com.piedrazul.msauthservice.domain.model.entity.RefreshToken;
import com.piedrazul.msauthservice.domain.model.repository.CredencialRepository;
import com.piedrazul.msauthservice.domain.model.repository.RefreshTokenRepository;
import com.piedrazul.msauthservice.infra.exception.CredencialesInvalidasException;
import com.piedrazul.msauthservice.infra.exception.RefreshTokenInvalidoException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Encapsulates every database-only unit of work for the login and refresh flows.
 *
 * WHY THIS CLASS EXISTS
 * ─────────────────────
 * AuthServiceImpl.login() and .refresh() need to (a) read/write the DB and
 * (b) call an external service (Feign → MSUserManagement) to fetch the user role.
 *
 * Putting both inside a single @Transactional method is a connection-pool
 * exhaustion bug: the transaction holds a DB connection open for the entire
 * duration of the network call.  If MSUserManagement is slow or unavailable
 * the connection is never released, and under any real load the pool drains.
 *
 * The fix is to break each flow into short, DB-only transactions that release
 * the connection *before* the Feign call is made:
 *
 *   ┌─ TX 1 (read, DB only) ──────────┐
 *   │  validate credentials / token   │  → connection released
 *   └─────────────────────────────────┘
 *           ↓
 *   [ Feign call – no DB connection held ]
 *           ↓
 *   ┌─ TX 2 (write, DB only) ─────────┐
 *   │  persist new refresh token      │  → connection released
 *   └─────────────────────────────────┘
 *
 * Because Spring @Transactional works through a proxy, self-invocation inside
 * the same bean bypasses the proxy and the annotation is silently ignored.
 * Extracting the transactional steps into a separate Spring-managed component
 * (this class) ensures every @Transactional boundary is honoured correctly.
 */
@Component
@RequiredArgsConstructor
public class AuthTransactionHelper {

    private final CredencialRepository credencialRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final IPasswordService passwordService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // -------------------------------------------------------------------------
    // LOGIN flow – step 1 of 2
    // -------------------------------------------------------------------------

    /**
     * Validates login credentials inside a short read-only transaction.
     * The DB connection is released as soon as this method returns.
     *
     * @throws CredencialesInvalidasException if login, active flag, or password fails.
     */
    @Transactional(readOnly = true)
    public Credencial verificarCredencialLogin(LoginRequest request) {
        Credencial credencial = credencialRepository.findByLogin(request.getLogin())
                .orElseThrow(CredencialesInvalidasException::new);

        if (!credencial.getActivo()) {
            throw new CredencialesInvalidasException();
        }

        if (!passwordService.verificar(request.getPassword(), credencial.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        return credencial;
    }

    // -------------------------------------------------------------------------
    // REFRESH flow – step 1 of 2
    // -------------------------------------------------------------------------

    /**
     * Validates and rotates (marks as used) the given refresh token in a single
     * read-write transaction.  Returns the associated Credencial so the caller
     * can issue new tokens.  The DB connection is released when this returns.
     *
     * @throws RefreshTokenInvalidoException  if the token is unknown, expired, or already used.
     * @throws CredencialesInvalidasException if the associated account is inactive.
     */
    @Transactional
    public Credencial rotarRefreshToken(String tokenValor) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenValor)
                .orElseThrow(RefreshTokenInvalidoException::new);

        if (!refreshToken.esValido()) {
            throw new RefreshTokenInvalidoException();
        }

        // Rotate: mark current token as used
        refreshToken.setUsado(true);
        refreshTokenRepository.save(refreshToken);

        Credencial credencial = credencialRepository.findByUsuarioId(refreshToken.getUsuarioId())
                .orElseThrow(CredencialesInvalidasException::new);

        if (!credencial.getActivo()) {
            throw new CredencialesInvalidasException();
        }

        return credencial;
    }

    // -------------------------------------------------------------------------
    // Shared – step 2 of 2 for both flows
    // -------------------------------------------------------------------------

    /**
     * Persists a new refresh token for the given user in a short write transaction.
     * Called *after* the Feign call has already completed and the role is known,
     * so no DB connection is held during the network round-trip.
     *
     * @return the plain-text UUID value of the newly created token.
     */
    @Transactional
    public String persistirRefreshToken(Long usuarioId) {
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
