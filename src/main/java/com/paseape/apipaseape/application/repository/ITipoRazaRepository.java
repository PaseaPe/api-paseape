package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.TipoRaza;

import java.util.List;

public interface ITipoRazaRepository {

    TipoRaza findById(Integer id);

    TipoRaza findByUuid(String uuid);

    TipoRaza findByDescripcion(String descripcion);

    List<TipoRaza> findAllByTipoMascotaId(Integer tipoMascotaId);

    List<TipoRaza> findAllByTipoMascotaIdAndEstado(Integer tipoMascotaId, Integer estado);

    List<TipoRaza> findAllByEstado(Integer estado);

    TipoRaza save(TipoRaza tipoRaza);
}
