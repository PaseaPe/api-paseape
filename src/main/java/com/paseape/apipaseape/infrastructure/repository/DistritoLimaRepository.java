package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.application.repository.IDistritoLimaRepository;
import com.paseape.apipaseape.domain.entity.DistritoLima;
import com.paseape.apipaseape.infrastructure.entity.DistritoLimaEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.IDistritoLimaDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.IDistritoLimaJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DistritoLimaRepository implements IDistritoLimaRepository {

    private final IDistritoLimaJpaRepository jpaRepository;
    private final IDistritoLimaDboMapper mapper;

    @Override
    public DistritoLima findById(Integer id) {
        if (id == null) {
            return null;
        }
        DistritoLimaEntity entity = jpaRepository.findById(id.intValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public DistritoLima findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        DistritoLimaEntity entity = jpaRepository.findByUuid(uuid.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public DistritoLima findByDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return null;
        }
        DistritoLimaEntity entity = jpaRepository.findByDescripcion(descripcion.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public DistritoLima findByCodigoUbigeo(String codigoUbigeo) {
        if (codigoUbigeo == null || codigoUbigeo.trim().isEmpty()) {
            return null;
        }
        DistritoLimaEntity entity = jpaRepository.findByCodigoUbigeo(codigoUbigeo.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<DistritoLima> findAllByEstado(Integer estado) {
        List<DistritoLimaEntity> entities = jpaRepository.findByEstado(estado);
        return mapper.toDomainList(entities);
    }

    @Override
    public DistritoLima save(DistritoLima distritoLima) {
        DistritoLimaEntity entity = mapper.toEntity(distritoLima);
        DistritoLimaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
