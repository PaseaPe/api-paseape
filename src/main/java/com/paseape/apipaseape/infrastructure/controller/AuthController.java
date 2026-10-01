package com.paseape.apipaseape.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paseape.apipaseape.application.service.AuthService;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.GoogleAuthReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.AuthResDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
import com.paseape.apipaseape.infrastructure.dto.response.HeaderDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.shared.RequestService;
import com.paseape.apipaseape.infrastructure.validator.AuthValidator;

import java.security.Principal;
import java.util.Calendar;
import java.util.Date;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final AuthService authService;
    private final AuthValidator authValidator;
    private final RequestService requestService;

    @PostMapping("/google")
    public ResponseEntity<ResponseDto<AuthResDto>> loginWithGoogle(@RequestBody GoogleAuthReqDto request) {
        var response = new ResponseDto<AuthResDto>();

        try {
            Date startDatetime = Calendar.getInstance().getTime();

            // 1. Validación de entrada previa
            ErrorDetailDto validationError = authValidator.validateGoogleAuthRequest(request);
            if (validationError != null) {
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            // 2. Intercambio de identidad con Google y generación de JWT
            AuthResDto authResDto = authService.authenticateWithGoogle(request);

            // 3. Telemetría y cabecera de auditoría
            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            // 4. Mapeo de respuesta HTTP 200 OK
            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Autenticación con Google procesada exitosamente");
            response.setHeader(headerDto);
            response.setResponse(authResDto);

            return ResponseEntity.status(response.getStatusCode()).body(response);

        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "role=" + (request != null ? request.getSelectedRole() : "null"));

            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al procesar la autenticación con Google");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));

            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseDto<String>> validateSession(Principal principal) {
        var response = new ResponseDto<String>();

        try {
            Date startDatetime = Calendar.getInstance().getTime();

            if (principal == null || principal.getName() == null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(StatusCodes.Code401);
                response.setCode(MessageCodes.ResponseCodeE01);
                response.setMessage("No se encontró una sesión activa autenticada");
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            String userId = principal.getName();
            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Sesión activa y verificada exitosamente");
            response.setHeader(headerDto);
            response.setResponse("Usuario autenticado con ID interno: " + userId);

            return ResponseEntity.status(response.getStatusCode()).body(response);

        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "principal=" + (principal != null ? principal.getName() : "null"));

            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Fallo interno al validar el token de sesión");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));

            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }
}