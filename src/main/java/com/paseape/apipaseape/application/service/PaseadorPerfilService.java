package com.paseape.apipaseape.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paseape.apipaseape.application.repository.IPaseadorRepository;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.infrastructure.dto.response.PaseadorPerfilResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.IPaseadorPerfilDtoMapper;

import static com.paseape.apipaseape.infrastructure.constant.Constant.ID_APROBADO;

@Service
@RequiredArgsConstructor
public class PaseadorPerfilService {

    private final IPaseadorRepository paseadorRepository;
    private final IPaseadorPerfilDtoMapper paseadorPerfilDtoMapper;

    @Transactional(readOnly = true)
    public PaseadorPerfilResDto obtenerPerfil(String uuid) throws BadRequestException {
        Paseador paseador = paseadorRepository.findByUuid(uuid);
        if (paseador == null) {
            throw new BadRequestException("El paseador especificado no existe.");
        }

        if (paseador.getEstadoVerificacion() == null || paseador.getEstadoVerificacion().getId() != ID_APROBADO) {
            throw new BadRequestException("El perfil del paseador no se encuentra disponible.");
        }

        return paseadorPerfilDtoMapper.toResDto(paseador);
    }
}
