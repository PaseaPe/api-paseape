package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.paseape.apipaseape.application.repository.IAdministradorRepository;
import com.paseape.apipaseape.domain.entity.Administrador;
import com.paseape.apipaseape.infrastructure.entity.AdministradorEntity;
import com.paseape.apipaseape.infrastructure.mapper.IAdministradorDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.IAdministradorJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AdministradorRepository implements IAdministradorRepository {

    private final IAdministradorJpaRepository jpaRepository;
    private final IAdministradorDboMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Administrador findById(Long id) {
        if (id == null) {
            return null;
        }
        AdministradorEntity entity = jpaRepository.findById(id.longValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public Administrador findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        AdministradorEntity entity = jpaRepository.findByUuid(uuid.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public Administrador findByCodigoEmpleado(String codigoEmpleado) {
        if (codigoEmpleado == null || codigoEmpleado.trim().isEmpty()) {
            return null;
        }
        AdministradorEntity entity = jpaRepository.findByCodigoEmpleado(codigoEmpleado.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Administrador> findAllByEstado(Integer estado) {
        List<AdministradorEntity> entities = jpaRepository.findByEstado(estado);
        return mapper.toDomainList(entities);
    }

    @Override
    @Transactional
    public Administrador save(Administrador administrador) {
        if (administrador.getId() == null && administrador.getUsuario() != null) {
            administrador.setId(administrador.getUsuario().getId());
        }
        AdministradorEntity entity = mapper.toEntity(administrador);
        AdministradorEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
