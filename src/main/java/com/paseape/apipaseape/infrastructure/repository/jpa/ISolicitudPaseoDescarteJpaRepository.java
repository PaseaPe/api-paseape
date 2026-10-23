package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoDescarteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISolicitudPaseoDescarteJpaRepository extends JpaRepository<SolicitudPaseoDescarteEntity, Long> {

    boolean existsBySolicitudPaseoIdAndPaseadorId(Long solicitudPaseoId, Long paseadorId);

    SolicitudPaseoDescarteEntity findBySolicitudPaseoIdAndPaseadorId(Long solicitudPaseoId, Long paseadorId);
}
