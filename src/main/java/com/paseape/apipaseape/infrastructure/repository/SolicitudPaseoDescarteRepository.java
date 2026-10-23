package com.paseape.apipaseape.infrastructure.repository;

import com.paseape.apipaseape.application.repository.ISolicitudPaseoDescarteRepository;
import com.paseape.apipaseape.domain.entity.SolicitudPaseoDescarte;
import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoDescarteEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.ISolicitudPaseoDescarteDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ISolicitudPaseoDescarteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SolicitudPaseoDescarteRepository implements ISolicitudPaseoDescarteRepository {

    private final ISolicitudPaseoDescarteJpaRepository jpaRepository;
    private final ISolicitudPaseoDescarteDboMapper mapper;

    @Override
    public SolicitudPaseoDescarte save(SolicitudPaseoDescarte descarte) {
        if (descarte == null) {
            return null;
        }
        SolicitudPaseoDescarteEntity entity = mapper.toEntity(descarte);
        SolicitudPaseoDescarteEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public boolean existsBySolicitudPaseoIdAndPaseadorId(Long solicitudPaseoId, Long paseadorId) {
        if (solicitudPaseoId == null || paseadorId == null) {
            return false;
        }
        return jpaRepository.existsBySolicitudPaseoIdAndPaseadorId(solicitudPaseoId, paseadorId);
    }
}
