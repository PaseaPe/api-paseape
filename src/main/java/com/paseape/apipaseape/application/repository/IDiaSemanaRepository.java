package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.DiaSemana;

import java.util.List;

public interface IDiaSemanaRepository {

    DiaSemana findById(Integer id);

    DiaSemana findByNumeroDia(Integer numeroDia);

    List<DiaSemana> findAll();

    List<DiaSemana> findAllByEstado(Boolean estado);
}
