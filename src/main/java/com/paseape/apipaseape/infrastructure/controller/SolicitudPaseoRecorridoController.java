package com.paseape.apipaseape.infrastructure.controller;

import com.paseape.apipaseape.application.service.SolicitudPaseoService;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.response.HeaderDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudPaseoRecorridoResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.security.JwtTokenProvider;
import com.paseape.apipaseape.infrastructure.shared.RequestService;
import com.paseape.apipaseape.infrastructure.validator.SolicitudPaseoValidator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Calendar;
import java.util.Date;

@RestController
@RequestMapping(path = "/solicitudes", produces = "application/json")
@RequiredArgsConstructor
public class SolicitudPaseoRecorridoController {

    private final SolicitudPaseoService solicitudPaseoService;
    private final SolicitudPaseoValidator solicitudPaseoValidator;
    private final RequestService requestService;
    private final JwtTokenProvider jwtTokenProvider;

    private final Logger logger = LoggerFactory.getLogger(SolicitudPaseoRecorridoController.class);

    @GetMapping("/{uuid}/recorrido")
    public ResponseEntity<ResponseDto<SolicitudPaseoRecorridoResDto>> obtenerRecorridoConsolidado(
            Principal principal,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String uuid
    ) {
        var response = new ResponseDto<SolicitudPaseoRecorridoResDto>();
        Date startDatetime = Calendar.getInstance().getTime();
        try {
            String userEmail = resolverUserEmail(principal, authHeader);
            ResponseDto<?> validationError = solicitudPaseoValidator.validateObtenerRecorridoConsolidado(userEmail, uuid);
            if (validationError != null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            SolicitudPaseoRecorridoResDto data = solicitudPaseoService.obtenerRecorridoConsolidado(uuid);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage(Constant.MSJ_RECORRIDO_OBTENIDO_EXITO);
            response.setHeader(headerDto);
            response.setResponse(data);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            logger.warn("Validación de negocio o solicitud no encontrada en obtenerRecorridoConsolidado | uuid: {} | Causa: {}", uuid, ex.getMessage());
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(ex.statusCode != null ? ex.statusCode : StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception ex) {
            logger.error("Error no controlado en obtenerRecorridoConsolidado | uuid: {} | Causa: {}", uuid, ex.getMessage(), ex);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al obtener el recorrido histórico del paseo");
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
        throw new BadRequestException(MessageCodes.ResponseCodeE01, "No se encontró una sesión activa autenticada.");
    }
}
