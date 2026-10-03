package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.TipoGeneroMascota;

import java.util.List;

public interface ITipoGeneroMascotaRepository {

    TipoGeneroMascota findById(Integer id);

    TipoGeneroMascota findByUuid(String uuid);

    TipoGeneroMascota findByDescripcion(String descripcion);

    List<TipoGeneroMascota> findAllByEstado(Integer estado);

    TipoGeneroMascota save(TipoGeneroMascota tipoGeneroMascota);
}
