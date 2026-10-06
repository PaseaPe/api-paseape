package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.DistritoLima;

import java.util.List;

public interface IDistritoLimaRepository {

    DistritoLima findById(Integer id);

    DistritoLima findByUuid(String uuid);

    DistritoLima findByDescripcion(String descripcion);

    DistritoLima findByCodigoUbigeo(String codigoUbigeo);

    List<DistritoLima> findAllByEstado(Integer estado);

    DistritoLima save(DistritoLima distritoLima);
}
