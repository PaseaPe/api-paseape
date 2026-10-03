package com.paseape.apipaseape.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.infrastructure.entity.TipoRazaEntity;

import java.util.List;

@Repository
public interface ITipoRazaJpaRepository extends JpaRepository<TipoRazaEntity, Integer> {

    TipoRazaEntity findById(int id);

    TipoRazaEntity findByUuid(String uuid);

    TipoRazaEntity findByDescripcion(String descripcion);

    List<TipoRazaEntity> findByTipoMascotaId(Integer tipoMascotaId);

    List<TipoRazaEntity> findByTipoMascotaIdAndEstado(Integer tipoMascotaId, Integer estado);

    List<TipoRazaEntity> findByEstado(Integer estado);
}
