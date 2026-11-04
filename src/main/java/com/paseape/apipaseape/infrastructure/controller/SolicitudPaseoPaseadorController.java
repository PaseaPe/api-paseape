package com.paseape.apipaseape.infrastructure.controller;

import com.paseape.apipaseape.application.service.SolicitudPaseoService;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CancelarSolicitudReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.RechazarSolicitudReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
import com.paseape.apipaseape.infrastructure.dto.response.HeaderDto;
import com.paseape.apipaseape.infrastructure.dto.response.PageResponseDto;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping
    public ResponseEntity<ResponseDto<PageResponseDto<SolicitudPaseoResDto>>> obtenerBandejaPaseador(
            Principal principal,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "tab", defaultValue = Constant.TAB_INMEDIATAS) String tab,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {
        var response = new ResponseDto<PageResponseDto<SolicitudPaseoResDto>>();
        Date startDatetime = Calendar.getInstance().getTime();
        try {
            ErrorDetailDto validationError = solicitudPaseoValidator.validarConsultaBandejaPaseador(tab, page, size);
            if (validationError != null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            String userEmail = resolverUserEmail(principal, authHeader);
            PageResponseDto<SolicitudPaseoResDto> data = solicitudPaseoService.obtenerBandejaPaseador(userEmail, tab, page, size);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage(Constant.MSJ_BANDEJA_PASEADOR_OBTENIDA);
            response.setHeader(headerDto);
            response.setResponse(data);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "obtenerBandejaPaseador: tab=" + tab + ", page=" + page + ", size=" + size);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(ex.statusCode != null ? ex.statusCode : StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "obtenerBandejaPaseador: tab=" + tab + ", page=" + page + ", size=" + size);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al obtener la bandeja de solicitudes del paseador");
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

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

    @PatchMapping("/{uuid}/descartar")
    public ResponseEntity<ResponseDto<Void>> descartarSolicitudInmediata(
            Principal principal,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String uuid
    ) {
        var response = new ResponseDto<Void>();
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
            solicitudPaseoService.descartarSolicitudInmediata(userEmail, uuid);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage(Constant.MSJ_SOLICITUD_DESCARTADA_EXITO);
            response.setHeader(headerDto);
            response.setResponse(null);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "descartarSolicitudInmediata: uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(ex.statusCode != null ? ex.statusCode : StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "descartarSolicitudInmediata: uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al descartar la solicitud de paseo inmediata");
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @PatchMapping("/{uuid}/aceptar")
    public ResponseEntity<ResponseDto<SolicitudPaseoResDto>> aceptarSolicitudProgramada(
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
            SolicitudPaseoResDto data = solicitudPaseoService.aceptarSolicitudProgramada(userEmail, uuid);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage(Constant.MSJ_SOLICITUD_ACEPTADA_EXITO);
            response.setHeader(headerDto);
            response.setResponse(data);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "aceptarSolicitudProgramada: uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(ex.statusCode != null ? ex.statusCode : StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "aceptarSolicitudProgramada: uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al aceptar la solicitud de paseo");
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @PatchMapping("/{uuid}/rechazar")
    public ResponseEntity<ResponseDto<SolicitudPaseoResDto>> rechazarSolicitudProgramada(
            Principal principal,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String uuid,
            @RequestBody(required = false) RechazarSolicitudReqDto reqDto
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

            ErrorDetailDto reqError = solicitudPaseoValidator.validarRechazarSolicitud(reqDto);
            if (reqError != null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(reqError.getStatusCode());
                response.setCode(reqError.getCode());
                response.setMessage(reqError.getMessage());
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            String userEmail = resolverUserEmail(principal, authHeader);
            SolicitudPaseoResDto data = solicitudPaseoService.rechazarSolicitudProgramada(userEmail, uuid, reqDto);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage(Constant.MSJ_SOLICITUD_RECHAZADA_EXITO);
            response.setHeader(headerDto);
            response.setResponse(data);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "rechazarSolicitudProgramada: uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(ex.statusCode != null ? ex.statusCode : StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "rechazarSolicitudProgramada: uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al rechazar la solicitud de paseo");
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @PatchMapping("/{uuid}/cancelar")
    public ResponseEntity<ResponseDto<SolicitudPaseoResDto>> cancelarPaseoAceptado(
            Principal principal,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String uuid,
            @RequestBody CancelarSolicitudReqDto reqDto
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

            ErrorDetailDto reqError = solicitudPaseoValidator.validarCancelarSolicitud(reqDto);
            if (reqError != null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(reqError.getStatusCode());
                response.setCode(reqError.getCode());
                response.setMessage(reqError.getMessage());
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            String userEmail = resolverUserEmail(principal, authHeader);
            SolicitudPaseoResDto data = solicitudPaseoService.cancelarPaseoAceptado(userEmail, uuid, reqDto);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage(Constant.MSJ_SOLICITUD_CANCELADA_EXITO);
            response.setHeader(headerDto);
            response.setResponse(data);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "cancelarPaseoAceptado: uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(ex.statusCode != null ? ex.statusCode : StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "cancelarPaseoAceptado: uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al cancelar el paseo");
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @PatchMapping("/{uuid}/iniciar")
    public ResponseEntity<ResponseDto<SolicitudPaseoResDto>> iniciarPaseo(
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
            SolicitudPaseoResDto data = solicitudPaseoService.iniciarPaseo(userEmail, uuid);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage(Constant.MSJ_PASEO_INICIADO_EXITO);
            response.setHeader(headerDto);
            response.setResponse(data);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "iniciarPaseo: uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(ex.statusCode != null ? ex.statusCode : StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "iniciarPaseo: uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al iniciar el paseo");
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
