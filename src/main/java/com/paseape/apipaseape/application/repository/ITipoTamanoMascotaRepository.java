package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.TipoTamanoMascota;

import java.util.List;

public interface ITipoTamanoMascotaRepository {

    TipoTamanoMascota findById(Integer id);

    TipoTamanoMascota findByUuid(String uuid);

    TipoTamanoMascota findByDescripcion(String descripcion);

    List<TipoTamanoMascota> findAllByEstado(Integer estado);

    TipoTamanoMascota save(TipoTamanoMascota tipoTamanoMascota);
}
