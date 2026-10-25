package com.paseape.apipaseape.infrastructure.controller;

import com.paseape.apipaseape.application.service.SolicitudPaseoService;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudInmediataReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudProgramadaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
import com.paseape.apipaseape.infrastructure.dto.response.HeaderDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudPaseoResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.security.JwtTokenProvider;
import com.paseape.apipaseape.infrastructure.shared.RequestService;
import com.paseape.apipaseape.infrastructure.validator.SolicitudPaseoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Calendar;
import java.util.Date;

@RestController
@RequestMapping("/cliente/solicitudes")
@RequiredArgsConstructor
public class SolicitudPaseoClienteController extends BaseController {

    private final SolicitudPaseoService solicitudPaseoService;
    private final SolicitudPaseoValidator solicitudPaseoValidator;
    private final RequestService requestService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/inmediata")
    public ResponseEntity<ResponseDto<SolicitudPaseoResDto>> crearSolicitudInmediata(
            Principal principal,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CrearSolicitudInmediataReqDto reqDto
    ) {
        var response = new ResponseDto<SolicitudPaseoResDto>();
        Date startDatetime = Calendar.getInstance().getTime();
        try {
            ErrorDetailDto validationError = solicitudPaseoValidator.validarCrearSolicitudInmediata(reqDto);
            if (validationError != null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            String userEmail = resolverUserEmail(principal, authHeader);
            SolicitudPaseoResDto data = solicitudPaseoService.crearSolicitudInmediata(userEmail, reqDto);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code201);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage(Constant.MSJ_SOLICITUD_CREADA_EXITO);
            response.setHeader(headerDto);
            response.setResponse(data);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "crearSolicitudInmediata");
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(ex.statusCode != null ? ex.statusCode : StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "crearSolicitudInmediata");
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al crear la solicitud de paseo inmediato");
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @PostMapping("/programada")
    public ResponseEntity<ResponseDto<SolicitudPaseoResDto>> crearSolicitudProgramada(
            Principal principal,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CrearSolicitudProgramadaReqDto reqDto
    ) {
        var response = new ResponseDto<SolicitudPaseoResDto>();
        Date startDatetime = Calendar.getInstance().getTime();
        try {
            ErrorDetailDto validationError = solicitudPaseoValidator.validarCrearSolicitudProgramada(reqDto);
            if (validationError != null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            String userEmail = resolverUserEmail(principal, authHeader);
            SolicitudPaseoResDto data = solicitudPaseoService.crearSolicitudProgramada(userEmail, reqDto);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code201);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage(Constant.MSJ_SOLICITUD_PROGRAMADA_CREADA_EXITO);
            response.setHeader(headerDto);
            response.setResponse(data);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "crearSolicitudProgramada");
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(ex.statusCode != null ? ex.statusCode : StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "crearSolicitudProgramada");
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al crear la solicitud de paseo programado");
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    private String resolverUserEmail(Principal principal, String authHeader) throws BadRequestException {
        if (principal != null && StringUtils.hasText(principal.getName())) {
            return principal.getName().trim().toLowerCase();
        }
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (jwtTokenProvider.validateToken(token)) {
                return jwtTokenProvider.getEmailFromToken(token);
            }
        }
        throw new BadRequestException(MessageCodes.ResponseCodeE01, "No se encontró una sesión activa autenticada para el cliente.");
    }
}
