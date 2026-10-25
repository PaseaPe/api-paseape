package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.application.repository.IBloqueHorarioRepository;
import com.paseape.apipaseape.domain.entity.BloqueHorario;
import com.paseape.apipaseape.infrastructure.entity.BloqueHorarioEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.IBloqueHorarioDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.IBloqueHorarioJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BloqueHorarioRepository implements IBloqueHorarioRepository {

    private final IBloqueHorarioJpaRepository jpaRepository;
    private final IBloqueHorarioDboMapper mapper;

    @Override
    public BloqueHorario findById(Integer id) {
        if (id == null) {
            return null;
        }
        BloqueHorarioEntity entity = jpaRepository.findById(id.intValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public BloqueHorario findByCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return null;
        }
        BloqueHorarioEntity entity = jpaRepository.findByCodigo(codigo.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public BloqueHorario findByOrden(Integer orden) {
        if (orden == null) {
            return null;
        }
        BloqueHorarioEntity entity = jpaRepository.findByOrden(orden);
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<BloqueHorario> findAll() {
        List<BloqueHorarioEntity> entities = jpaRepository.findAll();
        return mapper.toDomainList(entities);
    }

    @Override
    public List<BloqueHorario> findAllByEstado(Boolean estado) {
        if (estado == null) {
            return List.of();
        }
        List<BloqueHorarioEntity> entities = jpaRepository.findAllByEstadoOrderByOrdenAsc(estado);
        return mapper.toDomainList(entities);
    }
}
