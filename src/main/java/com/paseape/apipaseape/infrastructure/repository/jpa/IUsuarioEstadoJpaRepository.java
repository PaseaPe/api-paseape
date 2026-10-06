package com.paseape.apipaseape.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.infrastructure.entity.UsuarioEstadoEntity;

import java.util.List;

@Repository
public interface IUsuarioEstadoJpaRepository extends JpaRepository<UsuarioEstadoEntity, Integer> {

    UsuarioEstadoEntity findById(int id);

    UsuarioEstadoEntity findByUuid(String uuid);

    UsuarioEstadoEntity findByDescripcion(String descripcion);

    List<UsuarioEstadoEntity> findByEstado(Integer estado);
}