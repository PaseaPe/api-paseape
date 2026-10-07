package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.application.repository.IPaseadorRepository;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.infrastructure.entity.PaseadorEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.IPaseadorDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.IPaseadorJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaseadorRepository implements IPaseadorRepository {

    private final IPaseadorJpaRepository jpaRepository;
    private final IPaseadorDboMapper mapper;

    @Override
    public Paseador findById(Long id) {
        if (id == null) {
            return null;
        }
        PaseadorEntity entity = jpaRepository.findById(id.longValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public Paseador findByUsuarioId(Long usuarioId) {
        if (usuarioId == null) {
            return null;
        }
        PaseadorEntity entity = jpaRepository.findByUsuarioId(usuarioId.longValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public Paseador findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        PaseadorEntity entity = jpaRepository.findByUuid(uuid.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public Paseador findByNumeroDocumento(String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            return null;
        }
        PaseadorEntity entity = jpaRepository.findByNumeroDocumento(numeroDocumento.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<Paseador> findAllByDistritoCoberturaId(Integer distritoId) {
        if (distritoId == null) {
            return List.of();
        }
        List<PaseadorEntity> entities = jpaRepository.findByDistritoCoberturaId(distritoId);
        return mapper.toDomainList(entities);
    }

    @Override
    public List<Paseador> findAllByEstadoVerificacionId(Integer estadoVerificacionId) {
        if (estadoVerificacionId == null) {
            return List.of();
        }
        List<PaseadorEntity> entities = jpaRepository.findByEstadoVerificacionId(estadoVerificacionId);
        return mapper.toDomainList(entities);
    }

    @Override
    public List<Paseador> findAllByEstado(Integer estado) {
        List<PaseadorEntity> entities = jpaRepository.findByEstado(estado);
        return mapper.toDomainList(entities);
    }

    @Override
    public Paseador save(Paseador paseador) {
        PaseadorEntity entity = mapper.toEntity(paseador);
        PaseadorEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
