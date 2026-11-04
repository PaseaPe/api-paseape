package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.application.repository.ITipoDuracionPaseoRepository;
import com.paseape.apipaseape.domain.entity.TipoDuracionPaseo;
import com.paseape.apipaseape.infrastructure.entity.TipoDuracionPaseoEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.ITipoDuracionPaseoDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ITipoDuracionPaseoJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TipoDuracionPaseoRepository implements ITipoDuracionPaseoRepository {

    private final ITipoDuracionPaseoJpaRepository jpaRepository;
    private final ITipoDuracionPaseoDboMapper mapper;

    @Override
    public TipoDuracionPaseo findById(Integer id) {
        if (id == null) {
            return null;
        }
        TipoDuracionPaseoEntity entity = jpaRepository.findById(id.intValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<TipoDuracionPaseo> findAll() {
        List<TipoDuracionPaseoEntity> entities = jpaRepository.findAll();
        return mapper.toDomainList(entities);
    }

    @Override
    public List<TipoDuracionPaseo> findAllByEstado(Boolean estado) {
        if (estado == null) {
            return List.of();
        }
        List<TipoDuracionPaseoEntity> entities = jpaRepository.findAllByEstado(estado);
        return mapper.toDomainList(entities);
    }
}
