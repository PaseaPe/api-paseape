package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.PaseoCalificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPaseoCalificacionRepository {

    PaseoCalificacion save(PaseoCalificacion paseoCalificacion);

    PaseoCalificacion findBySolicitudPaseoId(Long solicitudPaseoId);

    PaseoCalificacion findByUuid(String uuid);

    Page<PaseoCalificacion> findByPaseadorIdVisiblesPaginado(Long paseadorId, Pageable pageable);
}
