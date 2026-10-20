package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.SolicitudPaseoMascota;

import java.util.List;

public interface ISolicitudPaseoMascotaRepository {

    SolicitudPaseoMascota save(SolicitudPaseoMascota solicitudMascota);

    List<SolicitudPaseoMascota> saveAll(List<SolicitudPaseoMascota> solicitudesMascotas);

    List<SolicitudPaseoMascota> findAllBySolicitudPaseoId(Long solicitudPaseoId);
}
