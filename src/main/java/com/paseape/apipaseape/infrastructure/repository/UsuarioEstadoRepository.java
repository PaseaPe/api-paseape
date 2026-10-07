package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.application.repository.IUsuarioEstadoRepository;
import com.paseape.apipaseape.domain.entity.UsuarioEstado;
import com.paseape.apipaseape.infrastructure.entity.UsuarioEstadoEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.IUsuarioEstadoDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.IUsuarioEstadoJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UsuarioEstadoRepository implements IUsuarioEstadoRepository {

    private final IUsuarioEstadoJpaRepository jpaRepository;
    private final IUsuarioEstadoDboMapper mapper;

    @Override
    public UsuarioEstado findById(Integer id) {
        if (id == null) {
            return null;
        }
        UsuarioEstadoEntity entity = jpaRepository.findById(id.intValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public UsuarioEstado findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        UsuarioEstadoEntity entity = jpaRepository.findByUuid(uuid.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public UsuarioEstado findByDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return null;
        }
        UsuarioEstadoEntity entity = jpaRepository.findByDescripcion(descripcion.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<UsuarioEstado> findAllByEstado(Integer estado) {
        List<UsuarioEstadoEntity> entities = jpaRepository.findByEstado(estado);
        return mapper.toDomainList(entities);
    }

    @Override
    public UsuarioEstado save(UsuarioEstado usuarioEstado) {
        UsuarioEstadoEntity entity = mapper.toEntity(usuarioEstado);
        UsuarioEstadoEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}