package com.paseape.apipaseape.infrastructure.repository;

import com.paseape.apipaseape.domain.entity.SistemaHeartbeat;
import com.paseape.apipaseape.infrastructure.entity.SistemaHeartbeatEntity;
import com.paseape.apipaseape.infrastructure.mapper.ISistemaHeartbeatDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ISistemaHeartbeatJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.paseape.apipaseape.application.repository.ISistemaHeartbeatRepository;

@Repository
@RequiredArgsConstructor
public class SistemaHeartbeatRepository implements ISistemaHeartbeatRepository {

    private final ISistemaHeartbeatJpaRepository jpaRepository;
    private final ISistemaHeartbeatDboMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public SistemaHeartbeat findByComponente(String componente) {
        SistemaHeartbeatEntity entity = jpaRepository.findByComponente(componente);
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional
    public SistemaHeartbeat save(SistemaHeartbeat heartbeat) {
        SistemaHeartbeatEntity entity = mapper.toEntity(heartbeat);
        SistemaHeartbeatEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
