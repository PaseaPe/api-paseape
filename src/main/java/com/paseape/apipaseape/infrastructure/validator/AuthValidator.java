package com.paseape.apipaseape.infrastructure.validator;

import org.springframework.stereotype.Component;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.GoogleAuthReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;

@Component
public class AuthValidator {

    public ErrorDetailDto validateGoogleAuthRequest(GoogleAuthReqDto request) {
        if (request == null || request.getIdToken() == null || request.getIdToken().trim().isEmpty()) {
            return ErrorDetailDto.builder()
                    .statusCode(StatusCodes.Code400)
                    .code(MessageCodes.ResponseCodeBR01)
                    .message("El parametro 'idToken' es estrictamente obligatorio.")
                    .build();
        }

        if (request.getSelectedRole() != null &&
            !request.getSelectedRole().equalsIgnoreCase("OWNER") &&
            !request.getSelectedRole().equalsIgnoreCase("WALKER")) {
            return ErrorDetailDto.builder()
                    .statusCode(StatusCodes.Code400)
                    .code(MessageCodes.ResponseCodeBR01)
                    .message("El rol seleccionado debe ser 'OWNER' o 'WALKER'.")
                    .build();
        }

        return null;
    }
}