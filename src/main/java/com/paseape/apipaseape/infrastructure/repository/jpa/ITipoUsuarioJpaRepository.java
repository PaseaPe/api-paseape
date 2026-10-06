package com.paseape.apipaseape.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.infrastructure.entity.TipoUsuarioEntity;

import java.util.List;

@Repository
public interface ITipoUsuarioJpaRepository extends JpaRepository<TipoUsuarioEntity, Integer> {

    TipoUsuarioEntity findById(int id);

    TipoUsuarioEntity findByUuid(String uuid);

    TipoUsuarioEntity findByDescripcion(String descripcion);

    List<TipoUsuarioEntity> findByEstado(Integer estado);
}