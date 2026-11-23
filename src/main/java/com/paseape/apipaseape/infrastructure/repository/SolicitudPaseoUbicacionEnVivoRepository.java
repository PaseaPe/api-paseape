package com.paseape.apipaseape.infrastructure.repository;

import com.paseape.apipaseape.application.repository.ISolicitudPaseoUbicacionEnVivoRepository;
import com.paseape.apipaseape.domain.entity.SolicitudPaseoUbicacionEnVivo;
import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoUbicacionEnVivoEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.ISolicitudPaseoUbicacionEnVivoDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ISolicitudPaseoUbicacionEnVivoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SolicitudPaseoUbicacionEnVivoRepository implements ISolicitudPaseoUbicacionEnVivoRepository {

    private final ISolicitudPaseoUbicacionEnVivoJpaRepository jpaRepository;
    private final ISolicitudPaseoUbicacionEnVivoDboMapper mapper;

    @Override
    public SolicitudPaseoUbicacionEnVivo save(SolicitudPaseoUbicacionEnVivo ubicacionEnVivo) {
        if (ubicacionEnVivo == null) {
            return null;
        }
        SolicitudPaseoUbicacionEnVivoEntity entity = mapper.toEntity(ubicacionEnVivo);
        SolicitudPaseoUbicacionEnVivoEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public SolicitudPaseoUbicacionEnVivo findById(Long id) {
        if (id == null) {
            return null;
        }
        SolicitudPaseoUbicacionEnVivoEntity entity = jpaRepository.findById(id).orElse(null);
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public SolicitudPaseoUbicacionEnVivo findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        SolicitudPaseoUbicacionEnVivoEntity entity = jpaRepository.findByUuid(uuid.trim()).orElse(null);
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public SolicitudPaseoUbicacionEnVivo findBySolicitudPaseoId(Long solicitudPaseoId) {
        if (solicitudPaseoId == null) {
            return null;
        }
        SolicitudPaseoUbicacionEnVivoEntity entity = jpaRepository.findBySolicitudPaseoId(solicitudPaseoId).orElse(null);
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public SolicitudPaseoUbicacionEnVivo findBySolicitudPaseoUuid(String solicitudPaseoUuid) {
        if (solicitudPaseoUuid == null || solicitudPaseoUuid.trim().isEmpty()) {
            return null;
        }
        SolicitudPaseoUbicacionEnVivoEntity entity = jpaRepository.findBySolicitudPaseoUuid(solicitudPaseoUuid.trim()).orElse(null);
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public void deleteBySolicitudPaseoId(Long solicitudPaseoId) {
        if (solicitudPaseoId != null) {
            jpaRepository.deleteBySolicitudPaseoId(solicitudPaseoId);
        }
    }

    @Override
    public void delete(SolicitudPaseoUbicacionEnVivo ubicacionEnVivo) {
        if (ubicacionEnVivo != null && ubicacionEnVivo.getId() != null) {
            jpaRepository.deleteById(ubicacionEnVivo.getId());
        }
    }
}
