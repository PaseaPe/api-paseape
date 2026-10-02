package com.paseape.apipaseape.application.repository;


import com.paseape.apipaseape.domain.entity.SistemaHeartbeat;

public interface ISistemaHeartbeatRepository {
    SistemaHeartbeat findByComponente(String componente);
    SistemaHeartbeat save(SistemaHeartbeat heartbeat);
}
