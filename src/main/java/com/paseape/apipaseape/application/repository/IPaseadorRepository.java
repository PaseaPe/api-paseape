package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.Paseador;

import java.util.List;

public interface IPaseadorRepository {

    Paseador findById(Long id);

    Paseador findByUuid(String uuid);

    Paseador findByNumeroDocumento(String numeroDocumento);

    List<Paseador> findAllByDistritoCoberturaId(Integer distritoId);

    List<Paseador> findAllByEstadoVerificacionId(Integer estadoVerificacionId);

    List<Paseador> findAllByEstado(Integer estado);

    Paseador save(Paseador paseador);
}
