package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.application.repository.ITipoGeneroMascotaRepository;
import com.paseape.apipaseape.domain.entity.TipoGeneroMascota;
import com.paseape.apipaseape.infrastructure.entity.TipoGeneroMascotaEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.ITipoGeneroMascotaDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ITipoGeneroMascotaJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TipoGeneroMascotaRepository implements ITipoGeneroMascotaRepository {

    private final ITipoGeneroMascotaJpaRepository jpaRepository;
    private final ITipoGeneroMascotaDboMapper mapper;

    @Override
    public TipoGeneroMascota findById(Integer id) {
        if (id == null) {
            return null;
        }
        TipoGeneroMascotaEntity entity = jpaRepository.findById(id.intValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public TipoGeneroMascota findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        TipoGeneroMascotaEntity entity = jpaRepository.findByUuid(uuid.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public TipoGeneroMascota findByDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return null;
        }
        TipoGeneroMascotaEntity entity = jpaRepository.findByDescripcion(descripcion.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<TipoGeneroMascota> findAllByEstado(Integer estado) {
        List<TipoGeneroMascotaEntity> entities = jpaRepository.findByEstado(estado);
        return mapper.toDomainList(entities);
    }

    @Override
    public TipoGeneroMascota save(TipoGeneroMascota tipoGeneroMascota) {
        TipoGeneroMascotaEntity entity = mapper.toEntity(tipoGeneroMascota);
        TipoGeneroMascotaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
