package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.TipoOrigenTrazaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ITipoOrigenTrazaJpaRepository extends JpaRepository<TipoOrigenTrazaEntity, Integer> {

    Optional<TipoOrigenTrazaEntity> findByUuid(String uuid);

    Optional<TipoOrigenTrazaEntity> findByDescripcion(String descripcion);
}
