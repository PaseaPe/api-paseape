package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.infrastructure.mapper.dto.IPaseadorDtoMapper;
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

@Service
@RequiredArgsConstructor
public class PaseadorService {

    private final IPaseadorRepository paseadorRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IPaseadorDtoMapper paseadorDtoMapper;

    @Transactional(rollbackFor = Exception.class)
    public PaseadorResDto cambiarDistritoCobertura(CambiarDistritoCoberturaReqDto reqDto) throws BadRequestException {
        Usuario usuario = usuarioRepository.findByUuid(reqDto.getUsuarioUuid().trim());
        if (usuario == null) {
            throw new BadRequestException("El usuario especificado no existe.");
        }

        Paseador paseador = paseadorRepository.findById(usuario.getId());
        if (paseador == null) {
            throw new BadRequestException("El usuario no cuenta con un perfil de paseador registrado.");
        }

        paseador.setDistritoCobertura(new DistritoLima(reqDto.getDistritoId()));
        Paseador paseadorActualizado = paseadorRepository.save(paseador);

        return paseadorDtoMapper.toResDto(paseadorActualizado);
    }
}