package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ISolicitudPaseoJpaRepository extends JpaRepository<SolicitudPaseoEntity, Long> {

    SolicitudPaseoEntity findById(long id);

    SolicitudPaseoEntity findByCodigoReserva(String codigoReserva);

    List<SolicitudPaseoEntity> findAllByClienteId(Long clienteId);

    List<SolicitudPaseoEntity> findAllByPaseadorId(Long paseadorId);

    List<SolicitudPaseoEntity> findAllByDistritoIdAndSolicitudPaseoEstadoId(Integer distritoId, Integer solicitudPaseoEstadoId);

    @Query("SELECT COUNT(spm) FROM SolicitudPaseoMascotaEntity spm " +
            "WHERE spm.solicitudPaseo.paseador.id = :paseadorId " +
            "AND spm.solicitudPaseo.fechaProgramada = :fechaProgramada " +
            "AND spm.solicitudPaseo.bloqueHorario.id = :bloqueHorarioId " +
            "AND spm.solicitudPaseo.solicitudPaseoEstado.id IN :estadosIds")
    Long countMascotasOcupadas(
            @Param("paseadorId") Long paseadorId,
            @Param("fechaProgramada") LocalDate fechaProgramada,
            @Param("bloqueHorarioId") Integer bloqueHorarioId,
            @Param("estadosIds") List<Integer> estadosIds
    );
}
