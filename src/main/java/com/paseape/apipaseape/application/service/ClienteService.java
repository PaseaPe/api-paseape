package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.dto.response.ClienteResDto;
import com.paseape.apipaseape.infrastructure.dto.response.UsuarioResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.IClienteDtoMapper;
import com.paseape.apipaseape.infrastructure.mapper.dto.IUsuarioDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final IClienteRepository clienteRepository;
    private final IUsuarioRepository usuarioRepository;

    private final IUsuarioDtoMapper usuarioDtoMapper;
    private final IClienteDtoMapper clienteDtoMapper;

    @Transactional(readOnly = true)
    public UsuarioResDto obtenerCliente(String uuid) throws BadRequestException {
        Usuario usuario = usuarioRepository.findByUuid(uuid);
        if (usuario == null){
            throw new BadRequestException("No se encontro el usuario");
        };
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId());
        if (cliente == null){
            throw new BadRequestException("No se encontro el cliente");
        };

        return usuarioDtoMapper.toResDto(usuario,cliente,null, clienteDtoMapper,null);
    }

}
