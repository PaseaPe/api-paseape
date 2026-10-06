package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.paseape.apipaseape.application.repository.IPaseadorEstadoVerificacionRepository;
import com.paseape.apipaseape.domain.entity.PaseadorEstadoVerificacion;
import com.paseape.apipaseape.infrastructure.entity.PaseadorEstadoVerificacionEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.IPaseadorEstadoVerificacionDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.IPaseadorEstadoVerificacionJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaseadorEstadoVerificacionRepository implements IPaseadorEstadoVerificacionRepository {

    private final IPaseadorEstadoVerificacionJpaRepository jpaRepository;
    private final IPaseadorEstadoVerificacionDboMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PaseadorEstadoVerificacion findById(Integer id) {
        if (id == null) {
            return null;
        }
        PaseadorEstadoVerificacionEntity entity = jpaRepository.findById(id.intValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public PaseadorEstadoVerificacion findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        PaseadorEstadoVerificacionEntity entity = jpaRepository.findByUuid(uuid.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public PaseadorEstadoVerificacion findByDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return null;
        }
        PaseadorEstadoVerificacionEntity entity = jpaRepository.findByDescripcion(descripcion.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaseadorEstadoVerificacion> findAllByEstado(Integer estado) {
        List<PaseadorEstadoVerificacionEntity> entities = jpaRepository.findByEstado(estado);
        return mapper.toDomainList(entities);
    }

    @Override
    @Transactional
    public PaseadorEstadoVerificacion save(PaseadorEstadoVerificacion paseadorEstadoVerificacion) {
        PaseadorEstadoVerificacionEntity entity = mapper.toEntity(paseadorEstadoVerificacion);
        PaseadorEstadoVerificacionEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
