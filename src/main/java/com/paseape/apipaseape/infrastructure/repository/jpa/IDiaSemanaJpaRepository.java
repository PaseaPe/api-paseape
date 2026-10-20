package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.DiaSemanaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDiaSemanaJpaRepository extends JpaRepository<DiaSemanaEntity, Integer> {

    DiaSemanaEntity findById(int id);

    DiaSemanaEntity findByNumeroDia(Integer numeroDia);

    List<DiaSemanaEntity> findAllByEstado(Boolean estado);
}
