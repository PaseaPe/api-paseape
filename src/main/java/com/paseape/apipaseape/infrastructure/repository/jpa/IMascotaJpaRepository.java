package com.paseape.apipaseape.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.infrastructure.entity.MascotaEntity;

import java.util.List;

@Repository
public interface IMascotaJpaRepository extends JpaRepository<MascotaEntity, Long> {

    MascotaEntity findById(long id);

    MascotaEntity findByUuid(String uuid);

    List<MascotaEntity> findByClienteId(Long clienteId);

    List<MascotaEntity> findByClienteIdAndEstado(Long clienteId, Integer estado);

    List<MascotaEntity> findByEstado(Integer estado);
}
