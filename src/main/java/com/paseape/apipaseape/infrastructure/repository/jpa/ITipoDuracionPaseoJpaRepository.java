package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.TipoDuracionPaseoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITipoDuracionPaseoJpaRepository extends JpaRepository<TipoDuracionPaseoEntity, Integer> {

    TipoDuracionPaseoEntity findById(int id);

    List<TipoDuracionPaseoEntity> findAllByEstado(Boolean estado);
}
