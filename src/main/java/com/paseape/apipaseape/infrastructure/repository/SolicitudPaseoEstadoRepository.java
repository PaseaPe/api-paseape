package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.application.repository.ISolicitudPaseoEstadoRepository;
import com.paseape.apipaseape.domain.entity.SolicitudPaseoEstado;
import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoEstadoEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.ISolicitudPaseoEstadoDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ISolicitudPaseoEstadoJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SolicitudPaseoEstadoRepository implements ISolicitudPaseoEstadoRepository {

    private final ISolicitudPaseoEstadoJpaRepository jpaRepository;
    private final ISolicitudPaseoEstadoDboMapper mapper;

    @Override
    public SolicitudPaseoEstado findById(Integer id) {
        if (id == null) {
            return null;
        }
        SolicitudPaseoEstadoEntity entity = jpaRepository.findById(id.intValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<SolicitudPaseoEstado> findAll() {
        List<SolicitudPaseoEstadoEntity> entities = jpaRepository.findAll();
        return mapper.toDomainList(entities);
    }

    @Override
    public List<SolicitudPaseoEstado> findAllByEstado(Boolean estado) {
        if (estado == null) {
            return List.of();
        }
        List<SolicitudPaseoEstadoEntity> entities = jpaRepository.findAllByEstado(estado);
        return mapper.toDomainList(entities);
    }
}
