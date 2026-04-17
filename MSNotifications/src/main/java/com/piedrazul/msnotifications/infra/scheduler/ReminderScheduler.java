package com.piedrazul.msnotifications.infra.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler de recordatorios de citas.
 * Se activa completamente cuando el BC de Scheduling esté integrado.
 */
@Component
@Slf4j
public class ReminderScheduler {

    // TODO: inyectar cliente de Scheduling cuando ese BC esté listo

    @Scheduled(cron = "0 0 8 * * *")
    public void enviarRecordatorios24h() {
        log.info("ReminderScheduler ejecutado — pendiente integración con Scheduling BC");
        // Aquí consultarás citas del día siguiente vía evento o REST interno
    }
}
