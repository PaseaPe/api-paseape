package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.TipoDuracionPaseo;

import java.util.List;

public interface ITipoDuracionPaseoRepository {

    TipoDuracionPaseo findById(Integer id);

    List<TipoDuracionPaseo> findAll();

    List<TipoDuracionPaseo> findAllByEstado(Boolean estado);
}
