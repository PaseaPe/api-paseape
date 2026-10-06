package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.paseape.apipaseape.application.repository.IMascotaRepository;
import com.paseape.apipaseape.domain.entity.Mascota;
import com.paseape.apipaseape.infrastructure.entity.MascotaEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.IMascotaDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.IMascotaJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MascotaRepository implements IMascotaRepository {

    private final IMascotaJpaRepository jpaRepository;
    private final IMascotaDboMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Mascota findById(Long id) {
        if (id == null) {
            return null;
        }
        MascotaEntity entity = jpaRepository.findById(id.longValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public Mascota findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        MascotaEntity entity = jpaRepository.findByUuid(uuid.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mascota> findAllByClienteId(Long clienteId) {
        if (clienteId == null) {
            return List.of();
        }
        List<MascotaEntity> entities = jpaRepository.findByClienteId(clienteId);
        return mapper.toDomainList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mascota> findAllByClienteIdAndEstado(Long clienteId, Integer estado) {
        if (clienteId == null) {
            return List.of();
        }
        List<MascotaEntity> entities = jpaRepository.findByClienteIdAndEstado(clienteId, estado);
        return mapper.toDomainList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mascota> findAllByEstado(Integer estado) {
        List<MascotaEntity> entities = jpaRepository.findByEstado(estado);
        return mapper.toDomainList(entities);
    }

    @Override
    @Transactional
    public Mascota save(Mascota mascota) {
        MascotaEntity entity = mapper.toEntity(mascota);
        MascotaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
