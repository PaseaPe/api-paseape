package com.paseape.apipaseape.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.infrastructure.entity.DistritoLimaEntity;

import java.util.List;

@Repository
public interface IDistritoLimaJpaRepository extends JpaRepository<DistritoLimaEntity, Integer> {

    DistritoLimaEntity findById(int id);

    DistritoLimaEntity findByUuid(String uuid);

    DistritoLimaEntity findByDescripcion(String descripcion);

    DistritoLimaEntity findByCodigoUbigeo(String codigoUbigeo);

    List<DistritoLimaEntity> findByEstado(Integer estado);
}
