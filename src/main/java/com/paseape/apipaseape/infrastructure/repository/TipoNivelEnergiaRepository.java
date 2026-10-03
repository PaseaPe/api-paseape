package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.paseape.apipaseape.application.repository.ITipoNivelEnergiaRepository;
import com.paseape.apipaseape.domain.entity.TipoNivelEnergia;
import com.paseape.apipaseape.infrastructure.entity.TipoNivelEnergiaEntity;
import com.paseape.apipaseape.infrastructure.mapper.ITipoNivelEnergiaDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ITipoNivelEnergiaJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TipoNivelEnergiaRepository implements ITipoNivelEnergiaRepository {

    private final ITipoNivelEnergiaJpaRepository jpaRepository;
    private final ITipoNivelEnergiaDboMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public TipoNivelEnergia findById(Integer id) {
        if (id == null) {
            return null;
        }
        TipoNivelEnergiaEntity entity = jpaRepository.findById(id.intValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public TipoNivelEnergia findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        TipoNivelEnergiaEntity entity = jpaRepository.findByUuid(uuid.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public TipoNivelEnergia findByDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return null;
        }
        TipoNivelEnergiaEntity entity = jpaRepository.findByDescripcion(descripcion.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoNivelEnergia> findAllByEstado(Integer estado) {
        List<TipoNivelEnergiaEntity> entities = jpaRepository.findByEstado(estado);
        return mapper.toDomainList(entities);
    }

    @Override
    @Transactional
    public TipoNivelEnergia save(TipoNivelEnergia tipoNivelEnergia) {
        TipoNivelEnergiaEntity entity = mapper.toEntity(tipoNivelEnergia);
        TipoNivelEnergiaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
