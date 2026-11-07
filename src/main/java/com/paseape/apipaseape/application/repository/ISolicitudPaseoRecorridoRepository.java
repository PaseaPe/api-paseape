package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.SolicitudPaseoRecorrido;

public interface ISolicitudPaseoRecorridoRepository {

    SolicitudPaseoRecorrido save(SolicitudPaseoRecorrido recorrido);

    SolicitudPaseoRecorrido findById(Long id);

    SolicitudPaseoRecorrido findByUuid(String uuid);

    SolicitudPaseoRecorrido findBySolicitudPaseoId(Long solicitudPaseoId);

    SolicitudPaseoRecorrido findBySolicitudPaseoUuid(String solicitudPaseoUuid);
}
