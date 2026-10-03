package com.paseape.apipaseape.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.infrastructure.entity.TipoProveedorAuthEntity;

import java.util.List;

@Repository
public interface ITipoProveedorAuthJpaRepository extends JpaRepository<TipoProveedorAuthEntity, Integer> {

    TipoProveedorAuthEntity findById(int id);

    TipoProveedorAuthEntity findByUuid(String uuid);

    TipoProveedorAuthEntity findByDescripcion(String descripcion);

    List<TipoProveedorAuthEntity> findByEstado(Integer estado);
}