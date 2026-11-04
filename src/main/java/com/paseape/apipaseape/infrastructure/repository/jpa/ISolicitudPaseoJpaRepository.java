package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import java.time.LocalDateTime;

@Repository
public interface ISolicitudPaseoJpaRepository extends JpaRepository<SolicitudPaseoEntity, Long> {

    SolicitudPaseoEntity findById(long id);

    SolicitudPaseoEntity findByUuid(String uuid);

    SolicitudPaseoEntity findByCodigoReserva(String codigoReserva);

    List<SolicitudPaseoEntity> findAllByClienteId(Long clienteId);

    List<SolicitudPaseoEntity> findAllByPaseadorId(Long paseadorId);

    List<SolicitudPaseoEntity> findAllByDistritoIdAndSolicitudPaseoEstadoId(Integer distritoId, Integer solicitudPaseoEstadoId);

    @Query("SELECT s FROM SolicitudPaseoEntity s " +
            "JOIN FETCH s.tipoModalidadPaseo " +
            "JOIN FETCH s.tipoMetodoPago " +
            "JOIN FETCH s.tipoDuracionPaseo " +
            "JOIN FETCH s.distrito " +
            "JOIN FETCH s.solicitudPaseoEstado " +
            "JOIN FETCH s.cliente c " +
            "JOIN FETCH c.usuario " +
            "LEFT JOIN FETCH s.paseador p " +
            "LEFT JOIN FETCH p.usuario " +
            "WHERE s.uuid = :uuid")
    SolicitudPaseoEntity findByUuidWithDetails(@Param("uuid") String uuid);

    @Query("SELECT DISTINCT s FROM SolicitudPaseoEntity s " +
            "JOIN FETCH s.tipoModalidadPaseo " +
            "JOIN FETCH s.tipoMetodoPago " +
            "JOIN FETCH s.tipoDuracionPaseo " +
            "JOIN FETCH s.distrito " +
            "JOIN FETCH s.solicitudPaseoEstado " +
            "JOIN FETCH s.cliente c " +
            "JOIN FETCH c.usuario " +
            "WHERE s.tipoModalidadPaseo.id = :modalidadId " +
            "AND s.solicitudPaseoEstado.id = :estadoId " +
            "AND s.paseador IS NULL " +
            "AND s.distrito.id = :distritoId " +
            "ORDER BY s.creadoEl DESC")
    List<SolicitudPaseoEntity> findDisponiblesByDistrito(
            @Param("modalidadId") Integer modalidadId,
            @Param("estadoId") Integer estadoId,
            @Param("distritoId") Integer distritoId
    );

    @Modifying(clearAutomatically = true)
    @Query("UPDATE SolicitudPaseoEntity s " +
            "SET s.paseador.id = :paseadorId, " +
            "s.solicitudPaseoEstado.id = :estadoAceptadaId, " +
            "s.fechaHoraAceptada = :ahoraUtc, " +
            "s.actualizadoEl = :ahoraUtc, " +
            "s.actualizadoPor = :emailPaseador " +
            "WHERE s.uuid = :solicitudUuid " +
            "AND s.paseador IS NULL " +
            "AND s.solicitudPaseoEstado.id = :estadoBuscandoId")
    int adjudicarSolicitudInmediata(
            @Param("solicitudUuid") String solicitudUuid,
            @Param("paseadorId") Long paseadorId,
            @Param("estadoAceptadaId") Integer estadoAceptadaId,
            @Param("estadoBuscandoId") Integer estadoBuscandoId,
            @Param("ahoraUtc") LocalDateTime ahoraUtc,
            @Param("emailPaseador") String emailPaseador
    );

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

    @Query("SELECT spm.solicitudPaseo.bloqueHorario.id, COUNT(spm) FROM SolicitudPaseoMascotaEntity spm " +
            "WHERE spm.solicitudPaseo.paseador.id = :paseadorId " +
            "AND spm.solicitudPaseo.fechaProgramada = :fechaProgramada " +
            "AND spm.solicitudPaseo.solicitudPaseoEstado.id IN :estadosIds " +
            "GROUP BY spm.solicitudPaseo.bloqueHorario.id")
    List<Object[]> countMascotasOcupadasAgrupadoPorBloque(
            @Param("paseadorId") Long paseadorId,
            @Param("fechaProgramada") LocalDate fechaProgramada,
            @Param("estadosIds") List<Integer> estadosIds
    );

    @Query("SELECT sp FROM SolicitudPaseoEntity sp " +
            "WHERE sp.paseador.id = :paseadorId " +
            "AND sp.bloqueHorario.id = :bloqueHorarioId " +
            "AND sp.fechaProgramada >= :fechaHoy " +
            "AND sp.solicitudPaseoEstado.id IN :estadosIds")
    List<SolicitudPaseoEntity> findFutureBookingsByPaseadorAndBloque(
            @Param("paseadorId") Long paseadorId,
            @Param("bloqueHorarioId") Integer bloqueHorarioId,
            @Param("fechaHoy") LocalDate fechaHoy,
            @Param("estadosIds") List<Integer> estadosIds
    );

    @Query(value = "SELECT s FROM SolicitudPaseoEntity s " +
            "JOIN FETCH s.tipoModalidadPaseo " +
            "JOIN FETCH s.tipoMetodoPago " +
            "JOIN FETCH s.tipoDuracionPaseo " +
            "JOIN FETCH s.distrito " +
            "JOIN FETCH s.solicitudPaseoEstado " +
            "JOIN FETCH s.cliente c " +
            "JOIN FETCH c.usuario " +
            "WHERE s.tipoModalidadPaseo.id = :modalidadId " +
            "AND s.solicitudPaseoEstado.id = :estadoId " +
            "AND s.paseador IS NULL " +
            "AND s.distrito.id = :distritoId " +
            "AND s.id NOT IN (" +
            "   SELECT d.solicitudPaseo.id FROM SolicitudPaseoDescarteEntity d " +
            "   WHERE d.paseador.id = :paseadorId" +
            ")",
            countQuery = "SELECT COUNT(s) FROM SolicitudPaseoEntity s " +
                    "WHERE s.tipoModalidadPaseo.id = :modalidadId " +
                    "AND s.solicitudPaseoEstado.id = :estadoId " +
                    "AND s.paseador IS NULL " +
                    "AND s.distrito.id = :distritoId " +
                    "AND s.id NOT IN (" +
                    "   SELECT d.solicitudPaseo.id FROM SolicitudPaseoDescarteEntity d " +
                    "   WHERE d.paseador.id = :paseadorId" +
                    ")")
    Page<SolicitudPaseoEntity> findInmediatasDisponiblesPaginado(
            @Param("modalidadId") Integer modalidadId,
            @Param("estadoId") Integer estadoId,
            @Param("distritoId") Integer distritoId,
            @Param("paseadorId") Long paseadorId,
            Pageable pageable
    );

    @Query(value = "SELECT s FROM SolicitudPaseoEntity s " +
            "JOIN FETCH s.tipoModalidadPaseo " +
            "JOIN FETCH s.tipoMetodoPago " +
            "JOIN FETCH s.tipoDuracionPaseo " +
            "JOIN FETCH s.distrito " +
            "JOIN FETCH s.solicitudPaseoEstado " +
            "JOIN FETCH s.cliente c " +
            "JOIN FETCH c.usuario " +
            "LEFT JOIN FETCH s.paseador p " +
            "LEFT JOIN FETCH p.usuario " +
            "WHERE s.paseador.id = :paseadorId " +
            "AND s.solicitudPaseoEstado.id IN :estadosIds",
            countQuery = "SELECT COUNT(s) FROM SolicitudPaseoEntity s " +
                    "WHERE s.paseador.id = :paseadorId " +
                    "AND s.solicitudPaseoEstado.id IN :estadosIds")
    Page<SolicitudPaseoEntity> findByPaseadorIdAndEstadosPaginado(
            @Param("paseadorId") Long paseadorId,
            @Param("estadosIds") List<Integer> estadosIds,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true)
    @Query("UPDATE SolicitudPaseoEntity s " +
            "SET s.solicitudPaseoEstado.id = :estadoVencidaId " +
            "WHERE s.tipoModalidadPaseo.id = :modalidadInmediatoId " +
            "AND s.solicitudPaseoEstado.id = :estadoBuscandoId " +
            "AND s.paseador IS NULL " +
            "AND s.horaVencimiento IS NOT NULL " +
            "AND s.horaVencimiento <= :ahora")
    int expirarSolicitudesInmediatasVencidas(
            @Param("modalidadInmediatoId") Integer modalidadInmediatoId,
            @Param("estadoBuscandoId") Integer estadoBuscandoId,
            @Param("estadoVencidaId") Integer estadoVencidaId,
            @Param("ahora") LocalDateTime ahora
    );

    @Query("SELECT s FROM SolicitudPaseoEntity s WHERE s.cliente.id = :clienteId AND s.solicitudPaseoEstado.id IN (:estados)")
    Page<SolicitudPaseoEntity> findByClienteIdAndEstadosPaginado(
            @Param("clienteId") Long clienteId,
            @Param("estados") List<Integer> estados,
            Pageable pageable
    );
}
