package com.paseape.apipaseape.infrastructure.scheduler;

import com.paseape.apipaseape.application.service.HeartbeatApplicationService;
import com.paseape.apipaseape.application.service.SolicitudPaseoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class AppTaskScheduler {

    private static final Logger log = LoggerFactory.getLogger(AppTaskScheduler.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String COMPONENTE_OCI = "BACKEND_OCI_PASEAPE";
    private static final String IP_ORIGEN_OCI = "157.151.203.121";

    private final HeartbeatApplicationService heartbeatApplicationService;
    private final SolicitudPaseoService solicitudPaseoService;

    /**
     * TAREA 1: Heartbeat preventivo anti-hibernación (Aiven MySQL).
     * Se ejecuta 30 segundos tras el arranque y luego cada 4 horas (14,400,000 ms).
     */
    @Scheduled(
            initialDelayString = "${app.scheduler.heartbeat.initial-delay-ms:30000}",
            fixedRateString = "${app.scheduler.heartbeat.interval-ms:14400000}"
    )
    public void executeDatabaseHeartbeat() {
        log.info("[SCHEDULER] Iniciando tarea: Database Heartbeat a las {}", LocalDateTime.now().format(FORMATTER));
        try {
            heartbeatApplicationService.procesarLatido(COMPONENTE_OCI, IP_ORIGEN_OCI);
            log.info("[SCHEDULER] Tarea Database Heartbeat finalizada exitosamente.");
        } catch (Exception ex) {
            log.error("[SCHEDULER] Error crítico en Database Heartbeat: {}", ex.getMessage(), ex);
        }
    }

    /**
     * TAREA 2: Expiración automática de solicitudes inmediatas no tomadas tras 5 minutos.
     * Se ejecuta cada 60 segundos (60,000 ms).
     */
    @Scheduled(
            initialDelayString = "${app.scheduler.solicitudes-inmediatas.initial-delay-ms:15000}",
            fixedRateString = "${app.scheduler.solicitudes-inmediatas.interval-ms:60000}"
    )
    public void expirarSolicitudesInmediatasVencidas() {
        try {
            int totalVencidas = solicitudPaseoService.expirarSolicitudesInmediatasVencidas();
            if (totalVencidas > 0) {
                log.info("[SCHEDULER] Se marcaron {} solicitudes inmediatas como VENCIDAS.", totalVencidas);
            }
        } catch (Exception ex) {
            log.error("[SCHEDULER] Error al expirar solicitudes inmediatas vencidas: {}", ex.getMessage(), ex);
        }
    }
}
