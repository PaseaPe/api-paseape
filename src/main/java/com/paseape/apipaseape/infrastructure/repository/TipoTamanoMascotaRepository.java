package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.paseape.apipaseape.application.repository.ITipoTamanoMascotaRepository;
import com.paseape.apipaseape.domain.entity.TipoTamanoMascota;
import com.paseape.apipaseape.infrastructure.entity.TipoTamanoMascotaEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.ITipoTamanoMascotaDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ITipoTamanoMascotaJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TipoTamanoMascotaRepository implements ITipoTamanoMascotaRepository {

    private final ITipoTamanoMascotaJpaRepository jpaRepository;
    private final ITipoTamanoMascotaDboMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public TipoTamanoMascota findById(Integer id) {
        if (id == null) {
            return null;
        }
        TipoTamanoMascotaEntity entity = jpaRepository.findById(id.intValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public TipoTamanoMascota findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        TipoTamanoMascotaEntity entity = jpaRepository.findByUuid(uuid.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public TipoTamanoMascota findByDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return null;
        }
        TipoTamanoMascotaEntity entity = jpaRepository.findByDescripcion(descripcion.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoTamanoMascota> findAllByEstado(Integer estado) {
        List<TipoTamanoMascotaEntity> entities = jpaRepository.findByEstado(estado);
        return mapper.toDomainList(entities);
    }

    @Override
    @Transactional
    public TipoTamanoMascota save(TipoTamanoMascota tipoTamanoMascota) {
        TipoTamanoMascotaEntity entity = mapper.toEntity(tipoTamanoMascota);
        TipoTamanoMascotaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
