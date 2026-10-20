package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.application.repository.ITipoMetodoPagoRepository;
import com.paseape.apipaseape.domain.entity.TipoMetodoPago;
import com.paseape.apipaseape.infrastructure.entity.TipoMetodoPagoEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.ITipoMetodoPagoDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ITipoMetodoPagoJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TipoMetodoPagoRepository implements ITipoMetodoPagoRepository {

    private final ITipoMetodoPagoJpaRepository jpaRepository;
    private final ITipoMetodoPagoDboMapper mapper;

    @Override
    public TipoMetodoPago findById(Integer id) {
        if (id == null) {
            return null;
        }
        TipoMetodoPagoEntity entity = jpaRepository.findById(id.intValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<TipoMetodoPago> findAll() {
        List<TipoMetodoPagoEntity> entities = jpaRepository.findAll();
        return mapper.toDomainList(entities);
    }

    @Override
    public List<TipoMetodoPago> findAllByEstado(Boolean estado) {
        if (estado == null) {
            return List.of();
        }
        List<TipoMetodoPagoEntity> entities = jpaRepository.findAllByEstado(estado);
        return mapper.toDomainList(entities);
    }
}
