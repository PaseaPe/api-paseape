package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.TipoNivelEnergia;

import java.util.List;

public interface ITipoNivelEnergiaRepository {

    TipoNivelEnergia findById(Integer id);

    TipoNivelEnergia findByUuid(String uuid);

    TipoNivelEnergia findByDescripcion(String descripcion);

    List<TipoNivelEnergia> findAllByEstado(Integer estado);

    TipoNivelEnergia save(TipoNivelEnergia tipoNivelEnergia);
}
