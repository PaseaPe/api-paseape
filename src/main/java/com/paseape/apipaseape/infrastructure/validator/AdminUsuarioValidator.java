package com.paseape.apipaseape.infrastructure.validator;

import org.springframework.stereotype.Component;

import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarEstadoUsuarioReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;

import static com.paseape.apipaseape.infrastructure.constant.Constant.*;

@Component
public class AdminUsuarioValidator {

    public ErrorDetailDto validateActualizarEstado(ActualizarEstadoUsuarioReqDto request) {
        if (request == null) {
            return buildError("El cuerpo de la solicitud no puede estar vacio.");
        }

        if (request.getUsuarioEstadoId() == null) {
            return buildError("El campo 'usuario_estado_id' es estrictamente obligatorio.");
        }

        Integer usuarioEstadoId = request.getUsuarioEstadoId();
        if (usuarioEstadoId != ID_ACTIVO
                && usuarioEstadoId != ID_INACTIVO
                && usuarioEstadoId != ID_SUSPENDIDO
                && usuarioEstadoId != ID_BLOQUEADO) {
            return buildError("Estado de usuario no permitido. Valores validos: 1 (ACTIVO), 2 (INACTIVO), 4 (SUSPENDIDO), 5 (BLOQUEADO).");
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
