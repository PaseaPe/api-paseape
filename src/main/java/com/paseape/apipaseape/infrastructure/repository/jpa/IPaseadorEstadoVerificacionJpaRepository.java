package com.paseape.apipaseape.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.infrastructure.entity.PaseadorEstadoVerificacionEntity;

import java.util.List;

@Repository
public interface IPaseadorEstadoVerificacionJpaRepository extends JpaRepository<PaseadorEstadoVerificacionEntity, Integer> {

    PaseadorEstadoVerificacionEntity findById(int id);

    PaseadorEstadoVerificacionEntity findByUuid(String uuid);

    PaseadorEstadoVerificacionEntity findByDescripcion(String descripcion);

    List<PaseadorEstadoVerificacionEntity> findByEstado(Integer estado);
}
