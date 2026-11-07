package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoRecorridoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ISolicitudPaseoRecorridoJpaRepository extends JpaRepository<SolicitudPaseoRecorridoEntity, Long> {

    Optional<SolicitudPaseoRecorridoEntity> findByUuid(String uuid);

    Optional<SolicitudPaseoRecorridoEntity> findBySolicitudPaseoId(Long solicitudPaseoId);

    @Query("SELECT r FROM SolicitudPaseoRecorridoEntity r WHERE r.solicitudPaseo.uuid = :solicitudPaseoUuid")
    Optional<SolicitudPaseoRecorridoEntity> findBySolicitudPaseoUuid(@Param("solicitudPaseoUuid") String solicitudPaseoUuid);
}
