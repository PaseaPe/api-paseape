package com.paseape.apipaseape.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.infrastructure.entity.TipoGeneroMascotaEntity;

import java.util.List;

@Repository
public interface ITipoGeneroMascotaJpaRepository extends JpaRepository<TipoGeneroMascotaEntity, Integer> {

    TipoGeneroMascotaEntity findById(int id);

    TipoGeneroMascotaEntity findByUuid(String uuid);

    TipoGeneroMascotaEntity findByDescripcion(String descripcion);

    List<TipoGeneroMascotaEntity> findByEstado(Integer estado);
}
