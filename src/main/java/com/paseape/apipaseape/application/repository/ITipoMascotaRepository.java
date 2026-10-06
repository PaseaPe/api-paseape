package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.TipoMascota;

import java.util.List;

public interface ITipoMascotaRepository {

    TipoMascota findById(Integer id);

    TipoMascota findByUuid(String uuid);

    TipoMascota findByDescripcion(String descripcion);

    List<TipoMascota> findAllByEstado(Integer estado);

    TipoMascota save(TipoMascota tipoMascota);
}
