package com.paseape.apipaseape.infrastructure.validator;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.MascotaReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.RegistrarMascotaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;

import java.math.BigDecimal;

@Component
public class MascotaValidator {

    public ErrorDetailDto validateRegistroUnitario(RegistrarMascotaReqDto request) {
        if (request == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El cuerpo de la solicitud no puede estar vacío.");
        }
        if (!StringUtils.hasText(request.getUsuarioUuid())) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'usuario_uuid' es estrictamente obligatorio.");
        }
        if (request.getMascota() == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El objeto 'mascota' es obligatorio.");
        }
        return validateMascota(request.getMascota());
    }

    public ErrorDetailDto validateMascota(MascotaReqDto mascota) {
        if (mascota == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "Los datos de la mascota no pueden ser nulos.");
        }
        if (!StringUtils.hasText(mascota.getNombre())) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El nombre de la mascota es obligatorio.");
        }
        if (mascota.getTipoMascotaId() == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El tipo de mascota es obligatorio.");
        }
        if (mascota.getTipoRazaId() == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "La raza de la mascota es obligatoria.");
        }
        if (mascota.getTipoGeneroMascotaId() == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El género de la mascota es obligatorio.");
        }
        if (mascota.getTipoTamanoMascotaId() == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El tamaño de la mascota es obligatorio.");
        }
        if (mascota.getTipoNivelEnergiaId() == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El nivel de energía de la mascota es obligatorio.");
        }
        if (mascota.getPesoKg() != null && mascota.getPesoKg().compareTo(BigDecimal.ZERO) <= 0) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El peso de la mascota debe ser mayor a 0 kg.");
        }
        return null;
    }

    private ErrorDetailDto buildError(int statusCode, String code, String message) {
        return ErrorDetailDto.builder()
                .statusCode(statusCode)
                .code(code)
                .message(message)
                .build();
    }
}
