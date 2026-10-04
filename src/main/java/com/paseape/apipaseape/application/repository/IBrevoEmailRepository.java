package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.BrevoEmail;

import java.util.List;

public interface IBrevoEmailRepository {

    BrevoEmail findById(Long id);

    BrevoEmail findByUuid(String uuid);

    List<BrevoEmail> findAllByDestinatarioEmail(String destinatarioEmail);

    List<BrevoEmail> findAllByTipoNotificacion(String tipoNotificacion);

    BrevoEmail save(BrevoEmail brevoEmail);
}
