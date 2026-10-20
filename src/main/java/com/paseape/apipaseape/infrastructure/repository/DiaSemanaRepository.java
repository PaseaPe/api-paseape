package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.application.repository.IDiaSemanaRepository;
import com.paseape.apipaseape.domain.entity.DiaSemana;
import com.paseape.apipaseape.infrastructure.entity.DiaSemanaEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.IDiaSemanaDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.IDiaSemanaJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DiaSemanaRepository implements IDiaSemanaRepository {

    private final IDiaSemanaJpaRepository jpaRepository;
    private final IDiaSemanaDboMapper mapper;

    @Override
    public DiaSemana findById(Integer id) {
        if (id == null) {
            return null;
        }
        DiaSemanaEntity entity = jpaRepository.findById(id.intValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public DiaSemana findByNumeroDia(Integer numeroDia) {
        if (numeroDia == null) {
            return null;
        }
        DiaSemanaEntity entity = jpaRepository.findByNumeroDia(numeroDia);
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<DiaSemana> findAll() {
        List<DiaSemanaEntity> entities = jpaRepository.findAll();
        return mapper.toDomainList(entities);
    }

    @Override
    public List<DiaSemana> findAllByEstado(Boolean estado) {
        if (estado == null) {
            return List.of();
        }
        List<DiaSemanaEntity> entities = jpaRepository.findAllByEstado(estado);
        return mapper.toDomainList(entities);
    }
}
