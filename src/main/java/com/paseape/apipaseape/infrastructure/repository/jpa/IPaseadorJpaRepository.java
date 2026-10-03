package com.paseape.apipaseape.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.infrastructure.entity.PaseadorEntity;

import java.util.List;

@Repository
public interface IPaseadorJpaRepository extends JpaRepository<PaseadorEntity, Long> {

    PaseadorEntity findById(long id);

    PaseadorEntity findByUuid(String uuid);

    PaseadorEntity findByNumeroDocumento(String numeroDocumento);

    List<PaseadorEntity> findByDistritoCoberturaId(Integer distritoId);

    List<PaseadorEntity> findByEstadoVerificacionId(Integer estadoVerificacionId);

    List<PaseadorEntity> findByEstado(Integer estado);
}
