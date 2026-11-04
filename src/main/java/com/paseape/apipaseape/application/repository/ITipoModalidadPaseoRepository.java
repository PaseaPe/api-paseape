package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.TipoModalidadPaseo;

import java.util.List;

public interface ITipoModalidadPaseoRepository {

    TipoModalidadPaseo findById(Integer id);

    List<TipoModalidadPaseo> findAll();

    List<TipoModalidadPaseo> findAllByEstado(Boolean estado);
}
