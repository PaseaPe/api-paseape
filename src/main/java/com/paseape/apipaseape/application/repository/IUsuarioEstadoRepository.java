package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.UsuarioEstado;

import java.util.List;

public interface IUsuarioEstadoRepository {

    UsuarioEstado findById(Integer id);

    UsuarioEstado findByUuid(String uuid);

    UsuarioEstado findByDescripcion(String descripcion);

    List<UsuarioEstado> findAllByEstado(Integer estado);

    UsuarioEstado save(UsuarioEstado usuarioEstado);
}