package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.paseape.apipaseape.application.repository.ITipoRazaRepository;
import com.paseape.apipaseape.domain.entity.TipoRaza;
import com.paseape.apipaseape.infrastructure.entity.TipoRazaEntity;
import com.paseape.apipaseape.infrastructure.mapper.ITipoRazaDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ITipoRazaJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TipoRazaRepository implements ITipoRazaRepository {

    private final ITipoRazaJpaRepository jpaRepository;
    private final ITipoRazaDboMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public TipoRaza findById(Integer id) {
        if (id == null) {
            return null;
        }
        TipoRazaEntity entity = jpaRepository.findById(id.intValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public TipoRaza findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        TipoRazaEntity entity = jpaRepository.findByUuid(uuid.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public TipoRaza findByDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return null;
        }
        TipoRazaEntity entity = jpaRepository.findByDescripcion(descripcion.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoRaza> findAllByTipoMascotaId(Integer tipoMascotaId) {
        if (tipoMascotaId == null) {
            return List.of();
        }
        List<TipoRazaEntity> entities = jpaRepository.findByTipoMascotaId(tipoMascotaId);
        return mapper.toDomainList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoRaza> findAllByTipoMascotaIdAndEstado(Integer tipoMascotaId, Integer estado) {
        if (tipoMascotaId == null) {
            return List.of();
        }
        List<TipoRazaEntity> entities = jpaRepository.findByTipoMascotaIdAndEstado(tipoMascotaId, estado);
        return mapper.toDomainList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoRaza> findAllByEstado(Integer estado) {
        List<TipoRazaEntity> entities = jpaRepository.findByEstado(estado);
        return mapper.toDomainList(entities);
    }

    @Override
    @Transactional
    public TipoRaza save(TipoRaza tipoRaza) {
        TipoRazaEntity entity = mapper.toEntity(tipoRaza);
        TipoRazaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
