package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoMascotaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISolicitudPaseoMascotaJpaRepository extends JpaRepository<SolicitudPaseoMascotaEntity, Long> {

    SolicitudPaseoMascotaEntity findById(long id);

    List<SolicitudPaseoMascotaEntity> findAllBySolicitudPaseoId(Long solicitudPaseoId);
}
