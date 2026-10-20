package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.application.repository.ISolicitudPaseoMascotaRepository;
import com.paseape.apipaseape.domain.entity.SolicitudPaseoMascota;
import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoMascotaEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.ISolicitudPaseoMascotaDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ISolicitudPaseoMascotaJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SolicitudPaseoMascotaRepository implements ISolicitudPaseoMascotaRepository {

    private final ISolicitudPaseoMascotaJpaRepository jpaRepository;
    private final ISolicitudPaseoMascotaDboMapper mapper;

    @Override
    public SolicitudPaseoMascota save(SolicitudPaseoMascota solicitudMascota) {
        if (solicitudMascota == null) {
            return null;
        }
        SolicitudPaseoMascotaEntity entity = mapper.toEntity(solicitudMascota);
        SolicitudPaseoMascotaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<SolicitudPaseoMascota> saveAll(List<SolicitudPaseoMascota> solicitudesMascotas) {
        if (solicitudesMascotas == null || solicitudesMascotas.isEmpty()) {
            return List.of();
        }
        List<SolicitudPaseoMascotaEntity> entities = solicitudesMascotas.stream()
                .map(mapper::toEntity)
                .toList();
        List<SolicitudPaseoMascotaEntity> savedEntities = jpaRepository.saveAll(entities);
        return mapper.toDomainList(savedEntities);
    }

    @Override
    public List<SolicitudPaseoMascota> findAllBySolicitudPaseoId(Long solicitudPaseoId) {
        if (solicitudPaseoId == null) {
            return List.of();
        }
        List<SolicitudPaseoMascotaEntity> entities = jpaRepository.findAllBySolicitudPaseoId(solicitudPaseoId);
        return mapper.toDomainList(entities);
    }
}
