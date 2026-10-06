package com.paseape.apipaseape.infrastructure.validator;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CambiarDistritoCoberturaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;

@Component
public class PaseadorValidator {

    public ErrorDetailDto validateCambiarDistritoCobertura(CambiarDistritoCoberturaReqDto request) {
        if (request == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El cuerpo de la solicitud no puede estar vacio.");
        }
        if (!StringUtils.hasText(request.getUsuarioUuid())) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'usuario_uuid' es estrictamente obligatorio.");
        }
        if (request.getDistritoId() == null || request.getDistritoId() <= 0) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'distrito_id' es obligatorio y debe ser mayor a 0.");
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
