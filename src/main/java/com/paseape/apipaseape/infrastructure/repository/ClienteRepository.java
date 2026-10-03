package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.infrastructure.entity.ClienteEntity;
import com.paseape.apipaseape.infrastructure.mapper.IClienteDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.IClienteJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ClienteRepository implements IClienteRepository {

    private final IClienteJpaRepository jpaRepository;
    private final IClienteDboMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Cliente findById(Long id) {
        if (id == null) {
            return null;
        }
        ClienteEntity entity = jpaRepository.findById(id.longValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        ClienteEntity entity = jpaRepository.findByUuid(uuid.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> findAllByDistritoId(Integer distritoId) {
        if (distritoId == null) {
            return List.of();
        }
        List<ClienteEntity> entities = jpaRepository.findByDistritoId(distritoId);
        return mapper.toDomainList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> findAllByEstado(Integer estado) {
        List<ClienteEntity> entities = jpaRepository.findByEstado(estado);
        return mapper.toDomainList(entities);
    }

    @Override
    @Transactional
    public Cliente save(Cliente cliente) {
        if (cliente.getId() == null && cliente.getUsuario() != null) {
            cliente.setId(cliente.getUsuario().getId());
        }
        ClienteEntity entity = mapper.toEntity(cliente);
        ClienteEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
