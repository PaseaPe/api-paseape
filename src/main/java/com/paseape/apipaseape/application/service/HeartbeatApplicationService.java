package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.ISistemaHeartbeatRepository;
import com.paseape.apipaseape.domain.entity.SistemaHeartbeat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HeartbeatApplicationService {

    private final ISistemaHeartbeatRepository heartbeatRepository;

    @Transactional(rollbackFor = Exception.class)
    public void procesarLatido(String componente, String ipOrigen) {
        SistemaHeartbeat heartbeat = heartbeatRepository.findByComponente(componente);

        if (heartbeat != null) {
            // Modificación orquestada en la capa de aplicación/dominio
            heartbeat.registrarNuevoLatido(ipOrigen);
            heartbeatRepository.save(heartbeat);
        } else {
            // Construcción inicial si no existiera en la BD
            SistemaHeartbeat nuevoHeartbeat = SistemaHeartbeat.builder()
                    .componente(componente)
                    .ultimoLatido(LocalDateTime.now())
                    .latidosAcumulados(1L)
                    .ipOrigen(ipOrigen)
                    .descripcion("Heartbeat preventivo Aiven MySQL - Lima 2026")
                    .build();
            heartbeatRepository.save(nuevoHeartbeat);
        }
    }
}
