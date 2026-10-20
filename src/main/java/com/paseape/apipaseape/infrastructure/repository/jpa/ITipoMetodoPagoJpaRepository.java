package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.TipoMetodoPagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITipoMetodoPagoJpaRepository extends JpaRepository<TipoMetodoPagoEntity, Integer> {

    TipoMetodoPagoEntity findById(int id);

    List<TipoMetodoPagoEntity> findAllByEstado(Boolean estado);
}
