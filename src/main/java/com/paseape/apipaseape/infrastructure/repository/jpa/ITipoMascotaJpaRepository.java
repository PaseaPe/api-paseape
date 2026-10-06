package com.paseape.apipaseape.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.infrastructure.entity.TipoMascotaEntity;

import java.util.List;

@Repository
public interface ITipoMascotaJpaRepository extends JpaRepository<TipoMascotaEntity, Integer> {

    TipoMascotaEntity findById(int id);

    TipoMascotaEntity findByUuid(String uuid);

    TipoMascotaEntity findByDescripcion(String descripcion);

    List<TipoMascotaEntity> findByEstado(Integer estado);
}
