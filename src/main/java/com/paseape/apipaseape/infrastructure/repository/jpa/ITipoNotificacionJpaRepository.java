package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.TipoNotificacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITipoNotificacionJpaRepository extends JpaRepository<TipoNotificacionEntity, Integer> {

    TipoNotificacionEntity findById(int id);

    List<TipoNotificacionEntity> findAllByEstado(Boolean estado);
}
