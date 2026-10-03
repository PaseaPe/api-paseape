package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.Administrador;

import java.util.List;

public interface IAdministradorRepository {

    Administrador findById(Long id);

    Administrador findByUuid(String uuid);

    Administrador findByCodigoEmpleado(String codigoEmpleado);

    List<Administrador> findAllByEstado(Integer estado);

    Administrador save(Administrador administrador);
}
