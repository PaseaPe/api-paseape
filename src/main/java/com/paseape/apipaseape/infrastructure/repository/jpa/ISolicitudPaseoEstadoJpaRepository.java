package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoEstadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISolicitudPaseoEstadoJpaRepository extends JpaRepository<SolicitudPaseoEstadoEntity, Integer> {

    SolicitudPaseoEstadoEntity findById(int id);

    List<SolicitudPaseoEstadoEntity> findAllByEstado(Boolean estado);
}
