package com.paseape.apipaseape.infrastructure.controller;

import com.paseape.apipaseape.application.service.CalificacionService;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.RegistrarCalificacionReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.CalificacionResDto;
import com.paseape.apipaseape.infrastructure.dto.response.HeaderDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResumenCalificacionesPaseadorResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.security.JwtTokenProvider;
import com.paseape.apipaseape.infrastructure.shared.RequestService;
import com.paseape.apipaseape.infrastructure.validator.CalificacionValidator;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Calendar;
import java.util.Date;

@RestController
@RequestMapping(path = "calificaciones", produces = "application/json")
@RequiredArgsConstructor
public class CalificacionController {

    private final CalificacionService calificacionService;
    private final CalificacionValidator calificacionValidator;
    private final RequestService requestService;
    private final JwtTokenProvider jwtTokenProvider;

    private final Logger logger = LoggerFactory.getLogger(CalificacionController.class);

    @Operation(summary = "Registrar calificación de 1 a 5 estrellas y opinión opcional para un paseo finalizado")
    @PostMapping("solicitudes/{uuid}")
    public ResponseEntity<ResponseDto<CalificacionResDto>> registrarCalificacion(
            Principal principal,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String uuid,
            @RequestBody RegistrarCalificacionReqDto reqDto
    ) {
        var response = new ResponseDto<CalificacionResDto>();
        Date startDatetime = Calendar.getInstance().getTime();
        try {
            String userEmail = resolverUserEmail(principal, authHeader);

            ResponseDto<?> validationError = calificacionValidator.validateRegistrarCalificacion(userEmail, uuid, reqDto);
            if (validationError != null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            CalificacionResDto data = calificacionService.registrarCalificacion(userEmail, uuid, reqDto);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code201);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage(Constant.MSJ_CALIFICACION_REGISTRADA_EXITO);
            response.setHeader(headerDto);
            response.setResponse(data);
            return ResponseEntity.status(response.getStatusCode()).body(response);

        } catch (BadRequestException brex) {
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(brex.statusCode != null ? brex.statusCode : StatusCodes.Code400);
            response.setCode(brex.code != null ? brex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(brex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(response.getStatusCode()).body(response);

        } catch (Exception ex) {
            logger.error("Error no controlado en registrarCalificacion | uuid: {} | Causa: {}", uuid, ex.getMessage(), ex);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Ocurrió un error interno al registrar la calificación");
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @Operation(summary = "Consultar promedio y opiniones del paseador autenticado")
    @GetMapping("paseador")
    public ResponseEntity<ResponseDto<ResumenCalificacionesPaseadorResDto>> obtenerMisCalificaciones(
            Principal principal,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var response = new ResponseDto<ResumenCalificacionesPaseadorResDto>();
        Date startDatetime = Calendar.getInstance().getTime();
        try {
            String userEmail = resolverUserEmail(principal, authHeader);

            ResponseDto<?> validationError = calificacionValidator.validateConsultarCalificacionesPaseadorAutenticado(userEmail, page, size);
            if (validationError != null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            ResumenCalificacionesPaseadorResDto data = calificacionService.obtenerCalificacionesPaseadorAutenticado(userEmail, page, size);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage(Constant.MSJ_CALIFICACIONES_PASEADOR_OBTENIDAS);
            response.setHeader(headerDto);
            response.setResponse(data);
            return ResponseEntity.status(response.getStatusCode()).body(response);

        } catch (BadRequestException brex) {
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(brex.statusCode != null ? brex.statusCode : StatusCodes.Code400);
            response.setCode(brex.code != null ? brex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(brex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(response.getStatusCode()).body(response);

        } catch (Exception ex) {
            logger.error("Error no controlado en obtenerMisCalificaciones | Causa: {}", ex.getMessage(), ex);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Ocurrió un error interno al consultar las calificaciones");
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @Operation(summary = "Consultar promedio y opiniones públicas de un paseador")
    @GetMapping("paseadores/{uuid}")
    public ResponseEntity<ResponseDto<ResumenCalificacionesPaseadorResDto>> obtenerCalificacionesPaseador(
            Principal principal,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String uuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var response = new ResponseDto<ResumenCalificacionesPaseadorResDto>();
        Date startDatetime = Calendar.getInstance().getTime();
        try {
            String userEmail = resolverUserEmail(principal, authHeader);

            ResponseDto<?> validationError = calificacionValidator.validateConsultarCalificacionesPaseadorPublico(userEmail, uuid, page, size);
            if (validationError != null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            ResumenCalificacionesPaseadorResDto data = calificacionService.obtenerCalificacionesPorPaseadorUuid(uuid, page, size);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage(Constant.MSJ_CALIFICACIONES_PASEADOR_OBTENIDAS);
            response.setHeader(headerDto);
            response.setResponse(data);
            return ResponseEntity.status(response.getStatusCode()).body(response);

        } catch (BadRequestException brex) {
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(brex.statusCode != null ? brex.statusCode : StatusCodes.Code400);
            response.setCode(brex.code != null ? brex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(brex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(response.getStatusCode()).body(response);

        } catch (Exception ex) {
            logger.error("Error no controlado en obtenerCalificacionesPaseador | uuid: {} | Causa: {}", uuid, ex.getMessage(), ex);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Ocurrió un error interno al consultar las calificaciones del paseador");
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
