package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.PaseadorEstadoVerificacion;

import java.util.List;

public interface IPaseadorEstadoVerificacionRepository {

    PaseadorEstadoVerificacion findById(Integer id);

    PaseadorEstadoVerificacion findByUuid(String uuid);

    PaseadorEstadoVerificacion findByDescripcion(String descripcion);

    List<PaseadorEstadoVerificacion> findAllByEstado(Integer estado);

    PaseadorEstadoVerificacion save(PaseadorEstadoVerificacion paseadorEstadoVerificacion);
}
