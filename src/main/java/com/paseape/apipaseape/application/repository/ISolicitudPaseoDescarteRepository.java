package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.SolicitudPaseoDescarte;

public interface ISolicitudPaseoDescarteRepository {

    SolicitudPaseoDescarte save(SolicitudPaseoDescarte descarte);

    boolean existsBySolicitudPaseoIdAndPaseadorId(Long solicitudPaseoId, Long paseadorId);
}
