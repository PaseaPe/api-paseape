package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.TipoMetodoPago;

import java.util.List;

public interface ITipoMetodoPagoRepository {

    TipoMetodoPago findById(Integer id);

    List<TipoMetodoPago> findAll();

    List<TipoMetodoPago> findAllByEstado(Boolean estado);
}
