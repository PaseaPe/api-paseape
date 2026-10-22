package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoMascotaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ISolicitudPaseoMascotaJpaRepository extends JpaRepository<SolicitudPaseoMascotaEntity, Long> {

    SolicitudPaseoMascotaEntity findById(long id);

    List<SolicitudPaseoMascotaEntity> findAllBySolicitudPaseoId(Long solicitudPaseoId);

    @Query("SELECT spm FROM SolicitudPaseoMascotaEntity spm " +
            "JOIN FETCH spm.mascota m " +
            "LEFT JOIN FETCH m.tipoRaza " +
            "LEFT JOIN FETCH m.tipoTamanoMascota " +
            "WHERE spm.solicitudPaseo.id = :solicitudPaseoId")
    List<SolicitudPaseoMascotaEntity> findAllBySolicitudPaseoIdWithDetails(@Param("solicitudPaseoId") Long solicitudPaseoId);

    @Query("SELECT spm FROM SolicitudPaseoMascotaEntity spm " +
            "JOIN FETCH spm.mascota m " +
            "LEFT JOIN FETCH m.tipoRaza " +
            "LEFT JOIN FETCH m.tipoTamanoMascota " +
            "WHERE spm.solicitudPaseo.id IN :solicitudPaseoIds")
    List<SolicitudPaseoMascotaEntity> findAllBySolicitudPaseoIdInWithDetails(@Param("solicitudPaseoIds") List<Long> solicitudPaseoIds);
}
