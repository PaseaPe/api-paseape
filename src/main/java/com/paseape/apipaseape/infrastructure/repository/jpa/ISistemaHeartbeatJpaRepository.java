package com.paseape.apipaseape.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paseape.apipaseape.infrastructure.entity.SistemaHeartbeatEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ISistemaHeartbeatJpaRepository extends JpaRepository<SistemaHeartbeatEntity, Integer> {

    SistemaHeartbeatEntity findByComponente(String componente);
}
