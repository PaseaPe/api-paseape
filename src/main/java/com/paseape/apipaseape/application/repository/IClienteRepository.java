package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.Cliente;

import java.util.List;

public interface IClienteRepository {

    Cliente findById(Long id);

    Cliente findByUsuarioId(Long usuarioId);

    Cliente findByUuid(String uuid);

    List<Cliente> findAllByDistritoId(Integer distritoId);

    List<Cliente> findAllByEstado(Integer estado);

    Cliente save(Cliente cliente);
}
