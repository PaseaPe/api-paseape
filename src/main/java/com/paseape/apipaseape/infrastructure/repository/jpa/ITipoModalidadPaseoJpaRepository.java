package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.TipoModalidadPaseoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITipoModalidadPaseoJpaRepository extends JpaRepository<TipoModalidadPaseoEntity, Integer> {

    TipoModalidadPaseoEntity findById(int id);

    List<TipoModalidadPaseoEntity> findAllByEstado(Boolean estado);
}
