package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoUbicacionEnVivoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ISolicitudPaseoUbicacionEnVivoJpaRepository extends JpaRepository<SolicitudPaseoUbicacionEnVivoEntity, Long> {

    Optional<SolicitudPaseoUbicacionEnVivoEntity> findByUuid(String uuid);

    Optional<SolicitudPaseoUbicacionEnVivoEntity> findBySolicitudPaseoId(Long solicitudPaseoId);

    @Query("SELECT u FROM SolicitudPaseoUbicacionEnVivoEntity u WHERE u.solicitudPaseo.uuid = :solicitudPaseoUuid")
    Optional<SolicitudPaseoUbicacionEnVivoEntity> findBySolicitudPaseoUuid(@Param("solicitudPaseoUuid") String solicitudPaseoUuid);

    @Modifying
    @Query("DELETE FROM SolicitudPaseoUbicacionEnVivoEntity u WHERE u.solicitudPaseo.id = :solicitudPaseoId")
    void deleteBySolicitudPaseoId(@Param("solicitudPaseoId") Long solicitudPaseoId);
}
