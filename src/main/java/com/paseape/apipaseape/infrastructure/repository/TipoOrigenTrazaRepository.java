package com.paseape.apipaseape.infrastructure.repository;

import com.paseape.apipaseape.application.repository.ITipoOrigenTrazaRepository;
import com.paseape.apipaseape.domain.entity.TipoOrigenTraza;
import com.paseape.apipaseape.infrastructure.entity.TipoOrigenTrazaEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.ITipoOrigenTrazaDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ITipoOrigenTrazaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TipoOrigenTrazaRepository implements ITipoOrigenTrazaRepository {

    private final ITipoOrigenTrazaJpaRepository jpaRepository;
    private final ITipoOrigenTrazaDboMapper mapper;

    @Override
    public TipoOrigenTraza save(TipoOrigenTraza tipoOrigenTraza) {
        if (tipoOrigenTraza == null) {
            return null;
        }
        TipoOrigenTrazaEntity entity = mapper.toEntity(tipoOrigenTraza);
        TipoOrigenTrazaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public TipoOrigenTraza findById(Integer id) {
        if (id == null) {
            return null;
        }
        TipoOrigenTrazaEntity entity = jpaRepository.findById(id).orElse(null);
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public TipoOrigenTraza findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        TipoOrigenTrazaEntity entity = jpaRepository.findByUuid(uuid.trim()).orElse(null);
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public TipoOrigenTraza findByDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return null;
        }
        TipoOrigenTrazaEntity entity = jpaRepository.findByDescripcion(descripcion.trim()).orElse(null);
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<TipoOrigenTraza> findAll() {
        List<TipoOrigenTrazaEntity> entities = jpaRepository.findAll();
        return mapper.toDomainList(entities);
    }
}
