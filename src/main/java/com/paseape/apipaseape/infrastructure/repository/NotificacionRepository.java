package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.application.repository.INotificacionRepository;
import com.paseape.apipaseape.domain.entity.Notificacion;
import com.paseape.apipaseape.infrastructure.entity.NotificacionEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.INotificacionDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.INotificacionJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificacionRepository implements INotificacionRepository {

    private final INotificacionJpaRepository jpaRepository;
    private final INotificacionDboMapper mapper;

    @Override
    public Notificacion save(Notificacion notificacion) {
        if (notificacion == null) {
            return null;
        }
        NotificacionEntity entity = mapper.toEntity(notificacion);
        NotificacionEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Notificacion findById(Long id) {
        if (id == null) {
            return null;
        }
        NotificacionEntity entity = jpaRepository.findById(id.longValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<Notificacion> findAllByDestinatarioUsuarioId(Long usuarioId) {
        if (usuarioId == null) {
            return List.of();
        }
        List<NotificacionEntity> entities = jpaRepository.findAllByDestinatarioUsuarioIdOrderByCreadoElDesc(usuarioId);
        return mapper.toDomainList(entities);
    }

    @Override
    public Integer countNoLeidasByDestinatarioUsuarioId(Long usuarioId) {
        if (usuarioId == null) {
            return 0;
        }
        Integer count = jpaRepository.countByDestinatarioUsuarioIdAndLeidoFalse(usuarioId);
        return count != null ? count : 0;
    }
}
