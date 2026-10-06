package com.paseape.apipaseape.infrastructure.validator;

import org.springframework.stereotype.Component;

import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarVerificacionPaseadorReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;

import static com.paseape.apipaseape.infrastructure.constant.Constant.*;

@Component
public class AdminPaseadorValidator {

    public ErrorDetailDto validateActualizarVerificacion(ActualizarVerificacionPaseadorReqDto request) {
        if (request == null) {
            return buildError("El cuerpo de la solicitud no puede estar vacio.");
        }

        if (request.getEstadoVerificacionId() == null) {
            return buildError("El campo 'estado_verificacion_id' es estrictamente obligatorio.");
        }

        Integer estadoVerificacionId = request.getEstadoVerificacionId();
        if (estadoVerificacionId != ID_EN_REVISION
                && estadoVerificacionId != ID_APROBADO
                && estadoVerificacionId != ID_RECHAZADO
                && estadoVerificacionId != ID_OBSERVADO) {
            return buildError("Estado de verificacion no permitido. Valores validos: 2 (EN_REVISION), 3 (APROBADO), 4 (RECHAZADO), 5 (OBSERVADO).");
        }

        return null;
    }

    private ErrorDetailDto buildError(String message) {
        return ErrorDetailDto.builder()
                .statusCode(StatusCodes.Code400)
                .code(MessageCodes.ResponseCodeBR01)
                .message(message)
                .build();
    }
}
