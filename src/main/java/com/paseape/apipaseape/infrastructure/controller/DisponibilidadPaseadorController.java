package com.paseape.apipaseape.infrastructure.controller;

import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.application.service.DisponibilidadPaseadorService;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarBloqueDisponibilidadReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarCapacidadBaseReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.GuardarDisponibilidadReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.DisponibilidadPaseadorResDto;
import com.paseape.apipaseape.infrastructure.dto.response.DisponibilidadSlotResDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
import com.paseape.apipaseape.infrastructure.dto.response.HeaderDto;
import com.paseape.apipaseape.infrastructure.dto.response.PaseadorPerfilResDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.security.JwtTokenProvider;
import com.paseape.apipaseape.infrastructure.shared.RequestService;
import com.paseape.apipaseape.infrastructure.validator.DisponibilidadPaseadorValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Calendar;
import java.util.Date;

@RestController
@RequestMapping("/paseador/disponibilidad")
@RequiredArgsConstructor
public class DisponibilidadPaseadorController extends BaseController {

    private final DisponibilidadPaseadorService disponibilidadService;
    private final DisponibilidadPaseadorValidator disponibilidadValidator;
    private final RequestService requestService;
    private final JwtTokenProvider jwtTokenProvider;
    private final IUsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<ResponseDto<DisponibilidadPaseadorResDto>> obtenerMiDisponibilidad(
            Principal principal,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        var response = new ResponseDto<DisponibilidadPaseadorResDto>();
        Date startDatetime = Calendar.getInstance().getTime();
        try {
            Long usuarioId = resolverUsuarioId(principal, authHeader);
            DisponibilidadPaseadorResDto data = disponibilidadService.obtenerMiDisponibilidad(usuarioId);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Disponibilidad del paseador obtenida exitosamente");
            response.setHeader(headerDto);
            response.setResponse(data);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "obtenerMiDisponibilidad");
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "obtenerMiDisponibilidad");
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al obtener la disponibilidad del paseador");
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @PutMapping
    public ResponseEntity<ResponseDto<DisponibilidadPaseadorResDto>> sincronizarDisponibilidad(
            Principal principal,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody GuardarDisponibilidadReqDto reqDto
    ) {
        var response = new ResponseDto<DisponibilidadPaseadorResDto>();
        Date startDatetime = Calendar.getInstance().getTime();
        try {
            ErrorDetailDto validationError = disponibilidadValidator.validarGuardarDisponibilidad(reqDto);
            if (validationError != null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            Long usuarioId = resolverUsuarioId(principal, authHeader);
            DisponibilidadPaseadorResDto data = disponibilidadService.sincronizarDisponibilidad(usuarioId, reqDto);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Disponibilidad sincronizada exitosamente");
            response.setHeader(headerDto);
            response.setResponse(data);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "sincronizarDisponibilidad");
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "sincronizarDisponibilidad");
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al sincronizar la disponibilidad");
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @PatchMapping("/bloque")
    public ResponseEntity<ResponseDto<DisponibilidadSlotResDto>> actualizarBloque(
            Principal principal,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ActualizarBloqueDisponibilidadReqDto reqDto
    ) {
        var response = new ResponseDto<DisponibilidadSlotResDto>();
        Date startDatetime = Calendar.getInstance().getTime();
        try {
            ErrorDetailDto validationError = disponibilidadValidator.validarActualizarBloque(reqDto);
            if (validationError != null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            Long usuarioId = resolverUsuarioId(principal, authHeader);
            DisponibilidadSlotResDto data = disponibilidadService.actualizarBloqueDisponibilidad(usuarioId, reqDto);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Bloque de disponibilidad actualizado exitosamente");
            response.setHeader(headerDto);
            response.setResponse(data);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "actualizarBloque");
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "actualizarBloque");
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al actualizar el bloque de disponibilidad");
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @PatchMapping("/capacidad-base")
    public ResponseEntity<ResponseDto<PaseadorPerfilResDto>> actualizarCapacidadBase(
            Principal principal,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ActualizarCapacidadBaseReqDto reqDto
    ) {
        var response = new ResponseDto<PaseadorPerfilResDto>();
        Date startDatetime = Calendar.getInstance().getTime();
        try {
            ErrorDetailDto validationError = disponibilidadValidator.validarActualizarCapacidadBase(reqDto);
            if (validationError != null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            Long usuarioId = resolverUsuarioId(principal, authHeader);
            PaseadorPerfilResDto data = disponibilidadService.actualizarCapacidadBase(usuarioId, reqDto);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Capacidad base de mascotas actualizada exitosamente");
            response.setHeader(headerDto);
            response.setResponse(data);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "actualizarCapacidadBase");
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "actualizarCapacidadBase");
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al actualizar la capacidad base");
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    private Long resolverUsuarioId(Principal principal, String authHeader) throws BadRequestException {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (jwtTokenProvider.validateToken(token)) {
                return jwtTokenProvider.getUserIdFromToken(token);
            }
        }
        if (principal != null && StringUtils.hasText(principal.getName())) {
            Usuario usuario = usuarioRepository.findByCorreo(principal.getName().trim().toLowerCase());
            if (usuario != null) {
                return usuario.getId();
            }
        }
        throw new BadRequestException("No se encontró una sesión activa autenticada para el paseador.");
    }
}
