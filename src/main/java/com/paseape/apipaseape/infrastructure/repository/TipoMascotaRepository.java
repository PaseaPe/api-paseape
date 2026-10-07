package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.application.repository.ITipoMascotaRepository;
import com.paseape.apipaseape.domain.entity.TipoMascota;
import com.paseape.apipaseape.infrastructure.entity.TipoMascotaEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.ITipoMascotaDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ITipoMascotaJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TipoMascotaRepository implements ITipoMascotaRepository {

    private final ITipoMascotaJpaRepository jpaRepository;
    private final ITipoMascotaDboMapper mapper;

    @Override
    public TipoMascota findById(Integer id) {
        if (id == null) {
            return null;
        }
        TipoMascotaEntity entity = jpaRepository.findById(id.intValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public TipoMascota findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        TipoMascotaEntity entity = jpaRepository.findByUuid(uuid.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public TipoMascota findByDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return null;
        }
        TipoMascotaEntity entity = jpaRepository.findByDescripcion(descripcion.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<TipoMascota> findAllByEstado(Integer estado) {
        List<TipoMascotaEntity> entities = jpaRepository.findByEstado(estado);
        return mapper.toDomainList(entities);
    }

    @Override
    public TipoMascota save(TipoMascota tipoMascota) {
        TipoMascotaEntity entity = mapper.toEntity(tipoMascota);
        TipoMascotaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
