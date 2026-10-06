package com.paseape.apipaseape.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.infrastructure.entity.TipoNivelEnergiaEntity;

import java.util.List;

@Repository
public interface ITipoNivelEnergiaJpaRepository extends JpaRepository<TipoNivelEnergiaEntity, Integer> {

    TipoNivelEnergiaEntity findById(int id);

    TipoNivelEnergiaEntity findByUuid(String uuid);

    TipoNivelEnergiaEntity findByDescripcion(String descripcion);

    List<TipoNivelEnergiaEntity> findByEstado(Integer estado);
}
