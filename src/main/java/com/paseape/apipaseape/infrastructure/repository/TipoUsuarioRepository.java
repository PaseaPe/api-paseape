package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.application.repository.ITipoUsuarioRepository;
import com.paseape.apipaseape.domain.entity.TipoUsuario;
import com.paseape.apipaseape.infrastructure.entity.TipoUsuarioEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.ITipoUsuarioDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ITipoUsuarioJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TipoUsuarioRepository implements ITipoUsuarioRepository {

    private final ITipoUsuarioJpaRepository jpaRepository;
    private final ITipoUsuarioDboMapper mapper;

    @Override
    public TipoUsuario findById(Integer id) {
        if (id == null) {
            return null;
        }
        TipoUsuarioEntity entity = jpaRepository.findById(id.intValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public TipoUsuario findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        TipoUsuarioEntity entity = jpaRepository.findByUuid(uuid.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public TipoUsuario findByDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return null;
        }
        TipoUsuarioEntity entity = jpaRepository.findByDescripcion(descripcion.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<TipoUsuario> findAllByEstado(Integer estado) {
        List<TipoUsuarioEntity> entities = jpaRepository.findByEstado(estado);
        return mapper.toDomainList(entities);
    }

    @Override
    public TipoUsuario save(TipoUsuario tipoUsuario) {
        TipoUsuarioEntity entity = mapper.toEntity(tipoUsuario);
        TipoUsuarioEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}