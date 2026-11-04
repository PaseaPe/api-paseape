package com.paseape.apipaseape.infrastructure.controller;

import com.paseape.apipaseape.application.service.SolicitudPaseoService;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CancelarSolicitudReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudInmediataReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudProgramadaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.*;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.security.JwtTokenProvider;
import com.paseape.apipaseape.infrastructure.shared.RequestService;
import com.paseape.apipaseape.infrastructure.validator.SolicitudPaseoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ResponseEntity<ResponseDto<PageResponseDto<SolicitudPaseoResDto>>> obtenerBandejaCliente(
            Principal principal,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "tab", defaultValue = Constant.TAB_CLIENTE_PENDIENTES) String tab,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {
        var response = new ResponseDto<PageResponseDto<SolicitudPaseoResDto>>();
        Date startDatetime = Calendar.getInstance().getTime();
        try {
            ErrorDetailDto validationError = solicitudPaseoValidator.validarConsultaBandejaCliente(tab, page, size);
            if (validationError != null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            String userEmail = resolverUserEmail(principal, authHeader);
            PageResponseDto<SolicitudPaseoResDto> data = solicitudPaseoService.obtenerBandejaCliente(userEmail, tab, page, size);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage(Constant.MSJ_BANDEJA_CLIENTE_OBTENIDA);
            response.setHeader(headerDto);
            response.setResponse(data);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "obtenerBandejaCliente: tab=" + tab + ", page=" + page + ", size=" + size);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(ex.statusCode != null ? ex.statusCode : StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "obtenerBandejaCliente: tab=" + tab + ", page=" + page + ", size=" + size);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al obtener la bandeja de solicitudes del cliente");
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @PatchMapping("/{uuid}/cancelar")
    public ResponseEntity<ResponseDto<SolicitudPaseoResDto>> cancelarSolicitud(
            Principal principal,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String uuid,
            @RequestBody(required = false) CancelarSolicitudReqDto reqDto
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

            ErrorDetailDto reqError = solicitudPaseoValidator.validarCancelarSolicitudCliente(reqDto);
            if (reqError != null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(reqError.getStatusCode());
                response.setCode(reqError.getCode());
                response.setMessage(reqError.getMessage());
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            String userEmail = resolverUserEmail(principal, authHeader);
            SolicitudPaseoResDto data = solicitudPaseoService.cancelarSolicitudCliente(userEmail, uuid, reqDto);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage(Constant.MSJ_SOLICITUD_CANCELADA_CLIENTE_EXITO);
            response.setHeader(headerDto);
            response.setResponse(data);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "cancelarSolicitud: uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(ex.statusCode != null ? ex.statusCode : StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "cancelarSolicitud: uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al cancelar la solicitud de paseo");
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
