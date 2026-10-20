package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.DisponibilidadPaseadorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDisponibilidadPaseadorJpaRepository extends JpaRepository<DisponibilidadPaseadorEntity, Long> {

    DisponibilidadPaseadorEntity findById(long id);

    DisponibilidadPaseadorEntity findByPaseadorIdAndDiaSemanaIdAndBloqueHorarioId(Long paseadorId, Integer diaSemanaId, Integer bloqueHorarioId);

    List<DisponibilidadPaseadorEntity> findAllByPaseadorId(Long paseadorId);

    List<DisponibilidadPaseadorEntity> findAllByPaseadorIdAndDiaSemanaId(Long paseadorId, Integer diaSemanaId);
}
