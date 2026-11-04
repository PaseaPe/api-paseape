package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.TipoNotificacion;

import java.util.List;

public interface ITipoNotificacionRepository {

    TipoNotificacion findById(Integer id);

    List<TipoNotificacion> findAll();

    List<TipoNotificacion> findAllByEstado(Boolean estado);
}
