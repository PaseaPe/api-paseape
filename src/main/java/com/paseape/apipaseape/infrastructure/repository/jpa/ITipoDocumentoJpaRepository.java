package com.paseape.apipaseape.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.infrastructure.entity.TipoDocumentoEntity;

import java.util.List;

@Repository
public interface ITipoDocumentoJpaRepository extends JpaRepository<TipoDocumentoEntity, Integer> {

    TipoDocumentoEntity findById(int id);

    TipoDocumentoEntity findByUuid(String uuid);

    TipoDocumentoEntity findByDescripcion(String descripcion);

    List<TipoDocumentoEntity> findByEstado(Integer estado);
}
