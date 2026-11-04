package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.BloqueHorario;

import java.util.List;

public interface IBloqueHorarioRepository {

    BloqueHorario findById(Integer id);

    BloqueHorario findByCodigo(String codigo);

    BloqueHorario findByOrden(Integer orden);

    List<BloqueHorario> findAll();

    List<BloqueHorario> findAllByEstado(Boolean estado);
}
