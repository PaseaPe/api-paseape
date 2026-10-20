package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.application.repository.IDisponibilidadPaseadorRepository;
import com.paseape.apipaseape.domain.entity.DisponibilidadPaseador;
import com.paseape.apipaseape.infrastructure.entity.DisponibilidadPaseadorEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.IDisponibilidadPaseadorDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.IDisponibilidadPaseadorJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DisponibilidadPaseadorRepository implements IDisponibilidadPaseadorRepository {

    private final IDisponibilidadPaseadorJpaRepository jpaRepository;
    private final IDisponibilidadPaseadorDboMapper mapper;

    @Override
    public DisponibilidadPaseador save(DisponibilidadPaseador disponibilidad) {
        if (disponibilidad == null) {
            return null;
        }
        DisponibilidadPaseadorEntity entity = mapper.toEntity(disponibilidad);
        DisponibilidadPaseadorEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<DisponibilidadPaseador> saveAll(List<DisponibilidadPaseador> disponibilidades) {
        if (disponibilidades == null || disponibilidades.isEmpty()) {
            return List.of();
        }
        List<DisponibilidadPaseadorEntity> entities = disponibilidades.stream()
                .map(mapper::toEntity)
                .toList();
        List<DisponibilidadPaseadorEntity> savedEntities = jpaRepository.saveAll(entities);
        return mapper.toDomainList(savedEntities);
    }

    @Override
    public DisponibilidadPaseador findById(Long id) {
        if (id == null) {
            return null;
        }
        DisponibilidadPaseadorEntity entity = jpaRepository.findById(id.longValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public DisponibilidadPaseador findByPaseadorIdAndDiaSemanaIdAndBloqueHorarioId(Long paseadorId, Integer diaSemanaId, Integer bloqueHorarioId) {
        if (paseadorId == null || diaSemanaId == null || bloqueHorarioId == null) {
            return null;
        }
        DisponibilidadPaseadorEntity entity = jpaRepository.findByPaseadorIdAndDiaSemanaIdAndBloqueHorarioId(paseadorId, diaSemanaId, bloqueHorarioId);
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<DisponibilidadPaseador> findAllByPaseadorId(Long paseadorId) {
        if (paseadorId == null) {
            return List.of();
        }
        List<DisponibilidadPaseadorEntity> entities = jpaRepository.findAllByPaseadorId(paseadorId);
        return mapper.toDomainList(entities);
    }

    @Override
    public List<DisponibilidadPaseador> findAllByPaseadorIdAndDiaSemanaId(Long paseadorId, Integer diaSemanaId) {
        if (paseadorId == null || diaSemanaId == null) {
            return List.of();
        }
        List<DisponibilidadPaseadorEntity> entities = jpaRepository.findAllByPaseadorIdAndDiaSemanaId(paseadorId, diaSemanaId);
        return mapper.toDomainList(entities);
    }
}
