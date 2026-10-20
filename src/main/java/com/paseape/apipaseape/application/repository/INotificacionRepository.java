package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.Notificacion;

import java.util.List;

public interface INotificacionRepository {

    Notificacion save(Notificacion notificacion);

    Notificacion findById(Long id);

    List<Notificacion> findAllByDestinatarioUsuarioId(Long usuarioId);

    Integer countNoLeidasByDestinatarioUsuarioId(Long usuarioId);
}
