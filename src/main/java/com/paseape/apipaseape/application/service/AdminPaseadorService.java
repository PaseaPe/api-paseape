package com.paseape.apipaseape.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.paseape.apipaseape.application.repository.IPaseadorRepository;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.domain.entity.PaseadorEstadoVerificacion;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarVerificacionPaseadorReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.PaseadorAdminResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.IPaseadorAdminDtoMapper;

import java.util.List;

import static com.paseape.apipaseape.infrastructure.constant.Constant.ESTADO_LOGICO_ACTIVO;
import static com.paseape.apipaseape.infrastructure.constant.Constant.ID_APROBADO;

@Service
@RequiredArgsConstructor
public class AdminPaseadorService {

    private final IPaseadorRepository paseadorRepository;
    private final IPaseadorAdminDtoMapper paseadorAdminDtoMapper;

    @Transactional(readOnly = true)
    public List<PaseadorAdminResDto> listarPaseadores(Integer estadoVerificacionId) {
        List<Paseador> paseadores;
        if (estadoVerificacionId != null) {
            paseadores = paseadorRepository.findAllByEstadoVerificacionId(estadoVerificacionId);
        } else {
            paseadores = paseadorRepository.findAllByEstado(ESTADO_LOGICO_ACTIVO);
        }
        return paseadorAdminDtoMapper.toResDtoList(paseadores);
    }

    @Transactional(readOnly = true)
    public PaseadorAdminResDto obtenerPaseador(String uuid) throws BadRequestException {
        Paseador paseador = paseadorRepository.findByUuid(uuid);
        if (paseador == null) {
            throw new BadRequestException("El paseador especificado no existe.");
        }
        return paseadorAdminDtoMapper.toResDto(paseador);
    }

    @Transactional(rollbackFor = Exception.class)
    public PaseadorAdminResDto actualizarVerificacion(String uuid, ActualizarVerificacionPaseadorReqDto reqDto) throws BadRequestException {
        Paseador paseador = paseadorRepository.findByUuid(uuid);
        if (paseador == null) {
            throw new BadRequestException("El paseador especificado no existe.");
        }

        Integer nuevoEstado = reqDto.getEstadoVerificacionId();
        if (nuevoEstado == null) {
            throw new BadRequestException("El estado de verificacion es obligatorio.");
        }

        if (nuevoEstado == ID_APROBADO && !StringUtils.hasText(paseador.getAntecedentesPolicialesUrl())) {
            throw new BadRequestException("No se puede aprobar al paseador: no ha adjuntado antecedentes policiales.");
        }

        paseador.setEstadoVerificacion(new PaseadorEstadoVerificacion(nuevoEstado));
        Paseador paseadorActualizado = paseadorRepository.save(paseador);
        return paseadorAdminDtoMapper.toResDto(paseadorActualizado);
    }
}
