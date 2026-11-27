package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.PaseoCalificacionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IPaseoCalificacionJpaRepository extends JpaRepository<PaseoCalificacionEntity, Long> {

    Optional<PaseoCalificacionEntity> findBySolicitudPaseoId(Long solicitudPaseoId);

    Optional<PaseoCalificacionEntity> findByUuid(String uuid);

    Page<PaseoCalificacionEntity> findByPaseadorIdAndVisibleTrueOrderByCreadoElDesc(Long paseadorId, Pageable pageable);
}
