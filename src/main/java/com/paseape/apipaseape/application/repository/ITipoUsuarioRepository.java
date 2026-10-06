package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.TipoUsuario;

import java.util.List;

public interface ITipoUsuarioRepository {

    TipoUsuario findById(Integer id);

    TipoUsuario findByUuid(String uuid);

    TipoUsuario findByDescripcion(String descripcion);

    List<TipoUsuario> findAllByEstado(Integer estado);

    TipoUsuario save(TipoUsuario tipoUsuario);
}