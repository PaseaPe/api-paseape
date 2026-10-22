package com.paseape.apipaseape.infrastructure.controller;

import com.paseape.apipaseape.application.service.SolicitudPaseoService;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/paseador/solicitudes")
@RequiredArgsConstructor
public class SolicitudPaseoPaseadorController extends BaseController {

    private final SolicitudPaseoService solicitudPaseoService;
    private final SolicitudPaseoValidator solicitudPaseoValidator;
    private final RequestService requestService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/disponibles")
    public ResponseEntity<ResponseDto<List<SolicitudPaseoResDto>>> obtenerSolicitudesDisponibles(
            Principal principal,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        var response = new ResponseDto<List<SolicitudPaseoResDto>>();
        Date startDatetime = Calendar.getInstance().getTime();
        try {
            String userEmail = resolverUserEmail(principal, authHeader);
            List<SolicitudPaseoResDto> data = solicitudPaseoService.obtenerSolicitudesInmediatasDisponibles(userEmail);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage(Constant.MSJ_SOLICITUDES_DISPONIBLES_OBTENIDAS);
            response.setHeader(headerDto);
            response.setResponse(data);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "obtenerSolicitudesDisponibles");
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(ex.statusCode != null ? ex.statusCode : StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "obtenerSolicitudesDisponibles");
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al obtener las solicitudes de paseo disponibles");
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @PatchMapping("/{uuid}/tomar")
    public ResponseEntity<ResponseDto<SolicitudPaseoResDto>> tomarSolicitudInmediata(
            Principal principal,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String uuid
    ) {
        var response = new ResponseDto<SolicitudPaseoResDto>();
        Date startDatetime = Calendar.getInstance().getTime();
        try {
            ErrorDetailDto validationError = solicitudPaseoValidator.validarUuid(uuid, "uuid");
            if (validationError != null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            String userEmail = resolverUserEmail(principal, authHeader);
            SolicitudPaseoResDto data = solicitudPaseoService.tomarSolicitudInmediata(userEmail, uuid);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage(Constant.MSJ_SOLICITUD_TOMADA_EXITO);
            response.setHeader(headerDto);
            response.setResponse(data);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "tomarSolicitudInmediata: uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(ex.statusCode != null ? ex.statusCode : StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "tomarSolicitudInmediata: uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al adjudicar la solicitud de paseo");
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
        throw new BadRequestException(MessageCodes.ResponseCodeE01, "No se encontró una sesión activa autenticada para el paseador.");
    }
}
