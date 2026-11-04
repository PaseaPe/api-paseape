package com.paseape.apipaseape.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.infrastructure.entity.PaseadorEntity;

import java.util.List;

@Repository
public interface IPaseadorJpaRepository extends JpaRepository<PaseadorEntity, Long> {

    PaseadorEntity findById(long id);

    PaseadorEntity findByUsuarioId(long usuarioId);

    PaseadorEntity findByUuid(String uuid);

    PaseadorEntity findByNumeroDocumento(String numeroDocumento);

    @Query("SELECT p FROM PaseadorEntity p WHERE (:distritoId IS NULL OR p.distritoCobertura.id = :distritoId) AND p.estadoVerificacion.id = :estadoVerificacionId")
    List<PaseadorEntity> findByDistritoCoberturaIdAndEstadoVerificacionId(
            @Param("distritoId") Integer distritoId,
            @Param("estadoVerificacionId") Integer estadoVerificacionId
    );

    @Query("SELECT p FROM PaseadorEntity p WHERE p.distritoCobertura.id IN :distritoIds AND p.estadoVerificacion.id = :estadoVerificacionId")
    List<PaseadorEntity> findByDistritoCoberturaIdInAndEstadoVerificacionId(
            @Param("distritoIds") List<Integer> distritoIds,
            @Param("estadoVerificacionId") Integer estadoVerificacionId
    );

    List<PaseadorEntity> findByEstadoVerificacionId(Integer estadoVerificacionId);

    List<PaseadorEntity> findByEstado(Integer estado);
}
