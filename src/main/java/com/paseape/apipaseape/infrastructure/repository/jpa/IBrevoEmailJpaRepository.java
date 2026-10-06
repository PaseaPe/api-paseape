package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.BrevoEmailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IBrevoEmailJpaRepository extends JpaRepository<BrevoEmailEntity, Long> {

    BrevoEmailEntity findById(long id);

    BrevoEmailEntity findByUuid(String uuid);

    List<BrevoEmailEntity> findByDestinatarioEmail(String destinatarioEmail);

    List<BrevoEmailEntity> findByTipoNotificacion(String tipoNotificacion);
}
