package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.TipoProveedorAuth;

import java.util.List;

public interface ITipoProveedorAuthRepository {

    TipoProveedorAuth findById(Integer id);

    TipoProveedorAuth findByUuid(String uuid);

    TipoProveedorAuth findByDescripcion(String descripcion);

    List<TipoProveedorAuth> findAllByEstado(Integer estado);

    TipoProveedorAuth save(TipoProveedorAuth tipoProveedorAuth);
}