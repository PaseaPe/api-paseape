package com.paseape.apipaseape.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.infrastructure.entity.TipoTamanoMascotaEntity;

import java.util.List;

@Repository
public interface ITipoTamanoMascotaJpaRepository extends JpaRepository<TipoTamanoMascotaEntity, Integer> {

    TipoTamanoMascotaEntity findById(int id);

    TipoTamanoMascotaEntity findByUuid(String uuid);

    TipoTamanoMascotaEntity findByDescripcion(String descripcion);

    List<TipoTamanoMascotaEntity> findByEstado(Integer estado);
}
