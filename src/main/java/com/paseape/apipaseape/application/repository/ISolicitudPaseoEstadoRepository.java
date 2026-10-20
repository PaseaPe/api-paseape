package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.SolicitudPaseoEstado;

import java.util.List;

public interface ISolicitudPaseoEstadoRepository {

    SolicitudPaseoEstado findById(Integer id);

    List<SolicitudPaseoEstado> findAll();

    List<SolicitudPaseoEstado> findAllByEstado(Boolean estado);
}
