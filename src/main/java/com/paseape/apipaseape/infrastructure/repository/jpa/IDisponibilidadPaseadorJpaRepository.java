package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.DisponibilidadPaseadorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDisponibilidadPaseadorJpaRepository extends JpaRepository<DisponibilidadPaseadorEntity, Long> {

    DisponibilidadPaseadorEntity findById(long id);

    DisponibilidadPaseadorEntity findByPaseadorIdAndDiaSemanaIdAndBloqueHorarioId(Long paseadorId, Integer diaSemanaId, Integer bloqueHorarioId);

    List<DisponibilidadPaseadorEntity> findAllByPaseadorId(Long paseadorId);

    List<DisponibilidadPaseadorEntity> findAllByPaseadorIdAndDiaSemanaId(Long paseadorId, Integer diaSemanaId);

    @Query(value = "SELECT * FROM disponibilidad_paseador WHERE paseador_id = :paseadorId AND dia_semana_id = :diaSemanaId AND bloque_horario_id = :bloqueHorarioId", nativeQuery = true)
    DisponibilidadPaseadorEntity findByPaseadorIdAndDiaSemanaIdAndBloqueHorarioIdIncludingInactive(
            @Param("paseadorId") Long paseadorId,
            @Param("diaSemanaId") Integer diaSemanaId,
            @Param("bloqueHorarioId") Integer bloqueHorarioId
    );

    @Query(value = "SELECT * FROM disponibilidad_paseador WHERE paseador_id = :paseadorId", nativeQuery = true)
    List<DisponibilidadPaseadorEntity> findAllByPaseadorIdIncludingInactive(@Param("paseadorId") Long paseadorId);
}

