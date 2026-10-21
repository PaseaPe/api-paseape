package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.DisponibilidadPaseador;

import java.util.List;

public interface IDisponibilidadPaseadorRepository {

    DisponibilidadPaseador save(DisponibilidadPaseador disponibilidad);

    List<DisponibilidadPaseador> saveAll(List<DisponibilidadPaseador> disponibilidades);

    DisponibilidadPaseador findById(Long id);

    DisponibilidadPaseador findByPaseadorIdAndDiaSemanaIdAndBloqueHorarioId(Long paseadorId, Integer diaSemanaId, Integer bloqueHorarioId);

    DisponibilidadPaseador findByPaseadorIdAndDiaSemanaIdAndBloqueHorarioIdIncludingInactive(Long paseadorId, Integer diaSemanaId, Integer bloqueHorarioId);

    List<DisponibilidadPaseador> findAllByPaseadorId(Long paseadorId);

    List<DisponibilidadPaseador> findAllByPaseadorIdIncludingInactive(Long paseadorId);

    List<DisponibilidadPaseador> findAllByPaseadorIdAndDiaSemanaId(Long paseadorId, Integer diaSemanaId);
}

