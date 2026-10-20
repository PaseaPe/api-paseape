package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.application.repository.ITipoModalidadPaseoRepository;
import com.paseape.apipaseape.domain.entity.TipoModalidadPaseo;
import com.paseape.apipaseape.infrastructure.entity.TipoModalidadPaseoEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.ITipoModalidadPaseoDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ITipoModalidadPaseoJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TipoModalidadPaseoRepository implements ITipoModalidadPaseoRepository {

    private final ITipoModalidadPaseoJpaRepository jpaRepository;
    private final ITipoModalidadPaseoDboMapper mapper;

    @Override
    public TipoModalidadPaseo findById(Integer id) {
        if (id == null) {
            return null;
        }
        TipoModalidadPaseoEntity entity = jpaRepository.findById(id.intValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<TipoModalidadPaseo> findAll() {
        List<TipoModalidadPaseoEntity> entities = jpaRepository.findAll();
        return mapper.toDomainList(entities);
    }

    @Override
    public List<TipoModalidadPaseo> findAllByEstado(Boolean estado) {
        if (estado == null) {
            return List.of();
        }
        List<TipoModalidadPaseoEntity> entities = jpaRepository.findAllByEstado(estado);
        return mapper.toDomainList(entities);
    }
}
