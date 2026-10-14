package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.infrastructure.dto.response.UsuarioResDto;
import com.paseape.apipaseape.infrastructure.mapper.dto.IPaseadorDtoMapper;
import com.paseape.apipaseape.infrastructure.mapper.dto.IUsuarioDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paseape.apipaseape.application.repository.IPaseadorRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.DistritoLima;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.dto.request.CambiarDistritoCoberturaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.PaseadorResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaseadorService {

    private final IPaseadorRepository paseadorRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IPaseadorDtoMapper paseadorDtoMapper;
    private final IUsuarioDtoMapper usuarioDtoMapper;

    @Transactional(rollbackFor = Exception.class)
    public PaseadorResDto cambiarDistritoCobertura(CambiarDistritoCoberturaReqDto reqDto) throws BadRequestException {
        Usuario usuario = usuarioRepository.findByUuid(reqDto.getUsuarioUuid().trim());
        if (usuario == null) {
            throw new BadRequestException("El usuario especificado no existe.");
        }

        Paseador paseador = paseadorRepository.findByUsuarioId(usuario.getId());
        if (paseador == null) {
            throw new BadRequestException("El usuario no cuenta con un perfil de paseador registrado.");
        }

        paseador.setDistritoCobertura(new DistritoLima(reqDto.getDistritoId()));
        Paseador paseadorActualizado = paseadorRepository.save(paseador);

        return paseadorDtoMapper.toResDto(paseadorActualizado);
    }

    @Transactional(readOnly = true)
    public UsuarioResDto obtenerPaseador(String uuid) throws BadRequestException {
        Usuario usuario = usuarioRepository.findByUuid(uuid);
        if (usuario == null){
            throw new BadRequestException("No se encontro el usuario");
        };
        Paseador paseador = paseadorRepository.findByUsuarioId(usuario.getId());
        if (paseador == null){
            throw new BadRequestException("No se encontro el paseador");
        };

        return usuarioDtoMapper.toResDto(usuario,null,paseador, null,paseadorDtoMapper);
    }

    @Transactional(readOnly = true)
    public List<PaseadorResDto> obtenerPaseadores(Integer distrito) {
        List<Paseador> paseadores = paseadorRepository.findAllByDistritoCoberturaId(distrito);
        return paseadorDtoMapper.toResDtoList(paseadores);
    }
}