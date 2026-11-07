package com.paseape.apipaseape.infrastructure.repository;

import com.paseape.apipaseape.application.repository.ISolicitudPaseoRecorridoRepository;
import com.paseape.apipaseape.domain.entity.SolicitudPaseoRecorrido;
import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoRecorridoEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.ISolicitudPaseoRecorridoDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ISolicitudPaseoRecorridoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SolicitudPaseoRecorridoRepository implements ISolicitudPaseoRecorridoRepository {

    private final ISolicitudPaseoRecorridoJpaRepository jpaRepository;
    private final ISolicitudPaseoRecorridoDboMapper mapper;

    @Override
    public SolicitudPaseoRecorrido save(SolicitudPaseoRecorrido recorrido) {
        if (recorrido == null) {
            return null;
        }
        SolicitudPaseoRecorridoEntity entity = mapper.toEntity(recorrido);
        SolicitudPaseoRecorridoEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public SolicitudPaseoRecorrido findById(Long id) {
        if (id == null) {
            return null;
        }
        SolicitudPaseoRecorridoEntity entity = jpaRepository.findById(id).orElse(null);
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public SolicitudPaseoRecorrido findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        SolicitudPaseoRecorridoEntity entity = jpaRepository.findByUuid(uuid.trim()).orElse(null);
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public SolicitudPaseoRecorrido findBySolicitudPaseoId(Long solicitudPaseoId) {
        if (solicitudPaseoId == null) {
            return null;
        }
        SolicitudPaseoRecorridoEntity entity = jpaRepository.findBySolicitudPaseoId(solicitudPaseoId).orElse(null);
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public SolicitudPaseoRecorrido findBySolicitudPaseoUuid(String solicitudPaseoUuid) {
        if (solicitudPaseoUuid == null || solicitudPaseoUuid.trim().isEmpty()) {
            return null;
        }
        SolicitudPaseoRecorridoEntity entity = jpaRepository.findBySolicitudPaseoUuid(solicitudPaseoUuid.trim()).orElse(null);
        return entity != null ? mapper.toDomain(entity) : null;
    }
}
