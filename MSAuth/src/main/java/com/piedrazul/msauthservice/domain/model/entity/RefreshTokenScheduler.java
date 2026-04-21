package com.piedrazul.msauthservice.domain.model.entity;

import com.piedrazul.msauthservice.domain.model.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class RefreshTokenScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenScheduler(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(cron = "0 0 1 * * *") // todos los días a la 1 AM
    public void limpiarTokens() {
        refreshTokenRepository
                .deleteByExpiraEnBeforeOrUsadoTrueOrRevocadoTrue(ZonedDateTime.now());
    }
}
