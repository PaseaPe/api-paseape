package com.paseape.apipaseape.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.infrastructure.entity.ClienteEntity;

import java.util.List;

@Repository
public interface IClienteJpaRepository extends JpaRepository<ClienteEntity, Long> {

    ClienteEntity findById(long id);

    ClienteEntity findByUuid(String uuid);

    List<ClienteEntity> findByDistritoId(Integer distritoId);

    List<ClienteEntity> findByEstado(Integer estado);
}
