package com.paseape.apipaseape.infrastructure.validator;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarPerfilReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;

import java.math.BigDecimal;

import static com.paseape.apipaseape.infrastructure.constant.Constant.*;

@Component
public class PerfilValidator {

    private static final int MAX_NOMBRE_APELLIDO_LENGTH = 100;
    private static final int MAX_TELEFONO_LENGTH = 15;
    private static final int MAX_DIRECCION_LENGTH = 255;
    private static final int MAX_CONTACTO_NOMBRE_LENGTH = 100;
    private static final int MAX_CONTACTO_TELEFONO_LENGTH = 15;
    private static final int MAX_NOTAS_LENGTH = 1000;
    private static final int MAX_BIOGRAFIA_LENGTH = 1000;

    public ErrorDetailDto validateActualizarPerfil(ActualizarPerfilReqDto request, Integer tipoUsuarioId) {
        if (request == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El cuerpo de la solicitud no puede estar vacio.");
        }

        // Validar campos de usuario base
        ErrorDetailDto baseError = validateUsuarioBase(request);
        if (baseError != null) {
            return baseError;
        }

        // Validar campos específicos por tipo de usuario
        if (tipoUsuarioId != null && tipoUsuarioId == ID_CLIENTE) {
            return validateClienteFields(request);
        } else if (tipoUsuarioId != null && tipoUsuarioId == ID_PASEADOR) {
            return validatePaseadorFields(request);
        }

        return null;
    }

    private ErrorDetailDto validateUsuarioBase(ActualizarPerfilReqDto request) {
        if (StringUtils.hasText(request.getNombres()) && request.getNombres().trim().length() > MAX_NOMBRE_APELLIDO_LENGTH) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "Los nombres no deben superar " + MAX_NOMBRE_APELLIDO_LENGTH + " caracteres.");
        }
        if (StringUtils.hasText(request.getApellidos()) && request.getApellidos().trim().length() > MAX_NOMBRE_APELLIDO_LENGTH) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "Los apellidos no deben superar " + MAX_NOMBRE_APELLIDO_LENGTH + " caracteres.");
        }
        if (StringUtils.hasText(request.getTelefono()) && request.getTelefono().trim().length() > MAX_TELEFONO_LENGTH) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El telefono no debe superar " + MAX_TELEFONO_LENGTH + " caracteres.");
        }
        return null;
    }

    private ErrorDetailDto validateClienteFields(ActualizarPerfilReqDto request) {
        if (StringUtils.hasText(request.getDireccionReferencia()) && request.getDireccionReferencia().trim().length() > MAX_DIRECCION_LENGTH) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "La direccion de referencia no debe superar " + MAX_DIRECCION_LENGTH + " caracteres.");
        }
        if (request.getDistritoId() != null && request.getDistritoId() <= 0) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El distrito debe ser mayor a 0.");
        }
        if (StringUtils.hasText(request.getContactoEmergenciaNombre()) && request.getContactoEmergenciaNombre().trim().length() > MAX_CONTACTO_NOMBRE_LENGTH) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El nombre del contacto de emergencia no debe superar " + MAX_CONTACTO_NOMBRE_LENGTH + " caracteres.");
        }
        if (StringUtils.hasText(request.getContactoEmergenciaTelefono()) && request.getContactoEmergenciaTelefono().trim().length() > MAX_CONTACTO_TELEFONO_LENGTH) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El telefono del contacto de emergencia no debe superar " + MAX_CONTACTO_TELEFONO_LENGTH + " caracteres.");
        }
        if (StringUtils.hasText(request.getNotasAdicionales()) && request.getNotasAdicionales().trim().length() > MAX_NOTAS_LENGTH) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "Las notas adicionales no deben superar " + MAX_NOTAS_LENGTH + " caracteres.");
        }
        return null;
    }

    private ErrorDetailDto validatePaseadorFields(ActualizarPerfilReqDto request) {
        if (StringUtils.hasText(request.getBiografia()) && request.getBiografia().trim().length() > MAX_BIOGRAFIA_LENGTH) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "La biografia no debe superar " + MAX_BIOGRAFIA_LENGTH + " caracteres.");
        }
        if (request.getTarifaHoraPen() != null && request.getTarifaHoraPen().compareTo(BigDecimal.ZERO) < 0) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "La tarifa por hora no puede ser negativa.");
        }
        if (request.getDistritoCoberturaId() != null && request.getDistritoCoberturaId() <= 0) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El distrito de cobertura debe ser mayor a 0.");
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
