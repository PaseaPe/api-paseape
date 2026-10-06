package com.paseape.apipaseape.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.infrastructure.entity.UsuarioEntity;

import java.util.List;

@Repository
public interface IUsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    UsuarioEntity findById(long id);

    UsuarioEntity findByUuid(String uuid);

    UsuarioEntity findByCorreo(String correo);

    UsuarioEntity findByProviderId(String providerId);

    List<UsuarioEntity> findByTipoUsuarioId(Integer tipoUsuarioId);

    List<UsuarioEntity> findByUsuarioEstadoId(Integer usuarioEstadoId);

    List<UsuarioEntity> findByEstado(Integer estado);

    boolean existsByCorreo(String correo);
}
