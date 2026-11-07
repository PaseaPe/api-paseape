package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.SolicitudPaseoUbicacionEnVivo;

public interface ISolicitudPaseoUbicacionEnVivoRepository {

    SolicitudPaseoUbicacionEnVivo save(SolicitudPaseoUbicacionEnVivo ubicacionEnVivo);

    SolicitudPaseoUbicacionEnVivo findById(Long id);

    SolicitudPaseoUbicacionEnVivo findByUuid(String uuid);

    SolicitudPaseoUbicacionEnVivo findBySolicitudPaseoId(Long solicitudPaseoId);

    SolicitudPaseoUbicacionEnVivo findBySolicitudPaseoUuid(String solicitudPaseoUuid);

    void deleteBySolicitudPaseoId(Long solicitudPaseoId);

    void delete(SolicitudPaseoUbicacionEnVivo ubicacionEnVivo);
}
