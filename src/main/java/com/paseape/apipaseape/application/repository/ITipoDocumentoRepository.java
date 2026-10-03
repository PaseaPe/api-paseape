package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.TipoDocumento;

import java.util.List;

public interface ITipoDocumentoRepository {

    TipoDocumento findById(Integer id);

    TipoDocumento findByUuid(String uuid);

    TipoDocumento findByDescripcion(String descripcion);

    List<TipoDocumento> findAllByEstado(Integer estado);

    TipoDocumento save(TipoDocumento tipoDocumento);
}
