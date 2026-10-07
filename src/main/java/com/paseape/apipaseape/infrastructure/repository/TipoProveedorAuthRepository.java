package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.application.repository.ITipoProveedorAuthRepository;
import com.paseape.apipaseape.domain.entity.TipoProveedorAuth;
import com.paseape.apipaseape.infrastructure.entity.TipoProveedorAuthEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.ITipoProveedorAuthDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ITipoProveedorAuthJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TipoProveedorAuthRepository implements ITipoProveedorAuthRepository {

    private final ITipoProveedorAuthJpaRepository jpaRepository;
    private final ITipoProveedorAuthDboMapper mapper;

    @Override
    public TipoProveedorAuth findById(Integer id) {
        if (id == null) {
            return null;
        }
        TipoProveedorAuthEntity entity = jpaRepository.findById(id.intValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public TipoProveedorAuth findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        TipoProveedorAuthEntity entity = jpaRepository.findByUuid(uuid.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public TipoProveedorAuth findByDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return null;
        }
        TipoProveedorAuthEntity entity = jpaRepository.findByDescripcion(descripcion.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<TipoProveedorAuth> findAllByEstado(Integer estado) {
        List<TipoProveedorAuthEntity> entities = jpaRepository.findByEstado(estado);
        return mapper.toDomainList(entities);
    }

    @Override
    public TipoProveedorAuth save(TipoProveedorAuth tipoProveedorAuth) {
        TipoProveedorAuthEntity entity = mapper.toEntity(tipoProveedorAuth);
        TipoProveedorAuthEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}