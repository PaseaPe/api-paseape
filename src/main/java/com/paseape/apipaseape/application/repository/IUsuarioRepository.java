package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.Usuario;

import java.util.List;

public interface IUsuarioRepository {

    Usuario findById(Long id);

    Usuario findByUuid(String uuid);

    Usuario findByCorreo(String correo);

    Usuario findByProviderId(String providerId);

    List<Usuario> findAllByTipoUsuarioId(Integer tipoUsuarioId);

    List<Usuario> findAllByUsuarioEstadoId(Integer usuarioEstadoId);

    List<Usuario> findAllByEstado(Integer estado);

    boolean existsByCorreo(String correo);

    Usuario save(Usuario usuario);
}
