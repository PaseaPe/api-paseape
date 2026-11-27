package com.paseape.apipaseape.infrastructure.repository;

import com.paseape.apipaseape.application.repository.IPaseoCalificacionRepository;
import com.paseape.apipaseape.domain.entity.PaseoCalificacion;
import com.paseape.apipaseape.infrastructure.entity.PaseoCalificacionEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.IPaseoCalificacionDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.IPaseoCalificacionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaseoCalificacionRepository implements IPaseoCalificacionRepository {

    private final IPaseoCalificacionJpaRepository jpaRepository;
    private final IPaseoCalificacionDboMapper mapper;

    @Override
    public PaseoCalificacion save(PaseoCalificacion paseoCalificacion) {
        PaseoCalificacionEntity entity = mapper.toDbo(paseoCalificacion);
        entity = jpaRepository.save(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public PaseoCalificacion findBySolicitudPaseoId(Long solicitudPaseoId) {
        if (solicitudPaseoId == null) {
            return null;
        }
        return jpaRepository.findBySolicitudPaseoId(solicitudPaseoId)
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public PaseoCalificacion findByUuid(String uuid) {
        if (uuid == null) {
            return null;
        }
        return jpaRepository.findByUuid(uuid)
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public Page<PaseoCalificacion> findByPaseadorIdVisiblesPaginado(Long paseadorId, Pageable pageable) {
        if (paseadorId == null || pageable == null) {
            return Page.empty();
        }
        return jpaRepository.findByPaseadorIdAndVisibleTrueOrderByCreadoElDesc(paseadorId, pageable)
                .map(mapper::toDomain);
    }
}
