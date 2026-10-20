package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.application.repository.ITipoNotificacionRepository;
import com.paseape.apipaseape.domain.entity.TipoNotificacion;
import com.paseape.apipaseape.infrastructure.entity.TipoNotificacionEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.ITipoNotificacionDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ITipoNotificacionJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TipoNotificacionRepository implements ITipoNotificacionRepository {

    private final ITipoNotificacionJpaRepository jpaRepository;
    private final ITipoNotificacionDboMapper mapper;

    @Override
    public TipoNotificacion findById(Integer id) {
        if (id == null) {
            return null;
        }
        TipoNotificacionEntity entity = jpaRepository.findById(id.intValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<TipoNotificacion> findAll() {
        List<TipoNotificacionEntity> entities = jpaRepository.findAll();
        return mapper.toDomainList(entities);
    }

    @Override
    public List<TipoNotificacion> findAllByEstado(Boolean estado) {
        if (estado == null) {
            return List.of();
        }
        List<TipoNotificacionEntity> entities = jpaRepository.findAllByEstado(estado);
        return mapper.toDomainList(entities);
    }
}
