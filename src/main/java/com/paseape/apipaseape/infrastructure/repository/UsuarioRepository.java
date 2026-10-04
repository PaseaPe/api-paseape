package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.entity.UsuarioEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.IUsuarioDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.IUsuarioJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UsuarioRepository implements IUsuarioRepository {

    private final IUsuarioJpaRepository jpaRepository;
    private final IUsuarioDboMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Usuario findById(Long id) {
        if (id == null) {
            return null;
        }
        UsuarioEntity entity = jpaRepository.findById(id.longValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        UsuarioEntity entity = jpaRepository.findByUuid(uuid.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario findByCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return null;
        }
        UsuarioEntity entity = jpaRepository.findByCorreo(correo.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario findByProviderId(String providerId) {
        if (providerId == null || providerId.trim().isEmpty()) {
            return null;
        }
        UsuarioEntity entity = jpaRepository.findByProviderId(providerId.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findAllByTipoUsuarioId(Integer tipoUsuarioId) {
        if (tipoUsuarioId == null) {
            return List.of();
        }
        List<UsuarioEntity> entities = jpaRepository.findByTipoUsuarioId(tipoUsuarioId);
        return mapper.toDomainList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findAllByUsuarioEstadoId(Integer usuarioEstadoId) {
        if (usuarioEstadoId == null) {
            return List.of();
        }
        List<UsuarioEntity> entities = jpaRepository.findByUsuarioEstadoId(usuarioEstadoId);
        return mapper.toDomainList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findAllByEstado(Integer estado) {
        List<UsuarioEntity> entities = jpaRepository.findByEstado(estado);
        return mapper.toDomainList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return false;
        }
        return jpaRepository.existsByCorreo(correo.trim());
    }

    @Override
    @Transactional
    public Usuario save(Usuario usuario) {
        UsuarioEntity entity = mapper.toEntity(usuario);
        UsuarioEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
