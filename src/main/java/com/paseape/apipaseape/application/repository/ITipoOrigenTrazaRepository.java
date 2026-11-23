package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.TipoOrigenTraza;

import java.util.List;

public interface ITipoOrigenTrazaRepository {

    TipoOrigenTraza save(TipoOrigenTraza tipoOrigenTraza);

    TipoOrigenTraza findById(Integer id);

    TipoOrigenTraza findByUuid(String uuid);

    TipoOrigenTraza findByDescripcion(String descripcion);

    List<TipoOrigenTraza> findAll();
}
