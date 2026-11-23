package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoMascotaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ISolicitudPaseoMascotaJpaRepository extends JpaRepository<SolicitudPaseoMascotaEntity, Long> {

    SolicitudPaseoMascotaEntity findById(long id);

    List<SolicitudPaseoMascotaEntity> findAllBySolicitudPaseoId(Long solicitudPaseoId);

    @Query("SELECT spm FROM SolicitudPaseoMascotaEntity spm " +
            "JOIN FETCH spm.mascota m " +
            "LEFT JOIN FETCH m.tipoMascota " +
            "LEFT JOIN FETCH m.tipoRaza " +
            "LEFT JOIN FETCH m.tipoGeneroMascota " +
            "LEFT JOIN FETCH m.tipoTamanoMascota " +
            "LEFT JOIN FETCH m.tipoNivelEnergia " +
            "LEFT JOIN FETCH m.folderDigitalDocument " +
            "WHERE spm.solicitudPaseo.id = :solicitudPaseoId")
    List<SolicitudPaseoMascotaEntity> findAllBySolicitudPaseoIdWithDetails(@Param("solicitudPaseoId") Long solicitudPaseoId);

    @Query("SELECT spm FROM SolicitudPaseoMascotaEntity spm " +
            "JOIN FETCH spm.mascota m " +
            "LEFT JOIN FETCH m.tipoRaza " +
            "LEFT JOIN FETCH m.tipoTamanoMascota " +
            "WHERE spm.solicitudPaseo.id IN :solicitudPaseoIds")
    List<SolicitudPaseoMascotaEntity> findAllBySolicitudPaseoIdInWithDetails(@Param("solicitudPaseoIds") List<Long> solicitudPaseoIds);

    @Query("SELECT spm FROM SolicitudPaseoMascotaEntity spm " +
            "JOIN FETCH spm.solicitudPaseo sp " +
            "JOIN FETCH sp.bloqueHorario bh " +
            "JOIN FETCH spm.mascota m " +
            "LEFT JOIN FETCH m.tipoRaza " +
            "LEFT JOIN FETCH m.tipoTamanoMascota " +
            "LEFT JOIN FETCH m.tipoNivelEnergia " +
            "LEFT JOIN FETCH m.tipoGeneroMascota " +
            "WHERE sp.paseador.id = :paseadorId " +
            "AND sp.fechaProgramada = :fechaProgramada " +
            "AND sp.solicitudPaseoEstado.id = :solicitudPaseoEstadoId")
    List<SolicitudPaseoMascotaEntity> findAllByPaseadorIdAndFechaProgramadaAndSolicitudPaseoEstadoId(
            @Param("paseadorId") Long paseadorId,
            @Param("fechaProgramada") LocalDate fechaProgramada,
            @Param("solicitudPaseoEstadoId") Integer solicitudPaseoEstadoId
    );
}
