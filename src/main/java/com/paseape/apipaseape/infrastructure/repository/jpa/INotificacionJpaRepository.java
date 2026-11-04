package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.NotificacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface INotificacionJpaRepository extends JpaRepository<NotificacionEntity, Long> {

    NotificacionEntity findById(long id);

    List<NotificacionEntity> findAllByDestinatarioUsuarioIdAndLeidoFalseOrderByCreadoElDesc(Long usuarioId);

    Integer countByDestinatarioUsuarioIdAndLeidoFalse(Long usuarioId);
}
