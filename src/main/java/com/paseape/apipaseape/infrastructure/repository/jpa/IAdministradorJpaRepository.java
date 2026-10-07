package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.PaseadorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.infrastructure.entity.AdministradorEntity;

import java.util.List;

@Repository
public interface IAdministradorJpaRepository extends JpaRepository<AdministradorEntity, Long> {

    AdministradorEntity findById(long id);

    AdministradorEntity findByUsuarioId(long usuarioId);

    AdministradorEntity findByUuid(String uuid);

    AdministradorEntity findByCodigoEmpleado(String codigoEmpleado);

    List<AdministradorEntity> findByEstado(Integer estado);
}
