package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.Mascota;

import java.util.List;

public interface IMascotaRepository {

    Mascota findById(Long id);

    Mascota findByUuid(String uuid);

    List<Mascota> findAllByClienteId(Long clienteId);

    List<Mascota> findAllByClienteIdAndEstado(Long clienteId, Integer estado);

    List<Mascota> findAllByEstado(Integer estado);

    Mascota save(Mascota mascota);
}
