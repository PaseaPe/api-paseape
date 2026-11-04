package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.BloqueHorarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IBloqueHorarioJpaRepository extends JpaRepository<BloqueHorarioEntity, Integer> {

    BloqueHorarioEntity findById(int id);

    BloqueHorarioEntity findByCodigo(String codigo);

    BloqueHorarioEntity findByOrden(Integer orden);

    List<BloqueHorarioEntity> findAllByEstado(Boolean estado);

    List<BloqueHorarioEntity> findAllByEstadoOrderByOrdenAsc(Boolean estado);

}
