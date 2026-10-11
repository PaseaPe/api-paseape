package com.paseape.apipaseape.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paseape.apipaseape.application.service.AdminUsuarioService;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarEstadoUsuarioReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarVerificacionPaseadorReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
import com.paseape.apipaseape.infrastructure.dto.response.HeaderDto;
import com.paseape.apipaseape.infrastructure.dto.response.PaseadorAdminResDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.dto.response.UsuarioAdminResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.shared.RequestService;
import com.paseape.apipaseape.infrastructure.validator.AdminUsuarioValidator;

import java.security.Principal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminUsuarioController extends BaseController {

    private final AdminUsuarioService adminUsuarioService;
    private final AdminUsuarioValidator adminUsuarioValidator;
    private final RequestService requestService;

    // --- Usuarios ---

    @GetMapping("/usuarios")
    public ResponseEntity<ResponseDto<List<UsuarioAdminResDto>>> listarUsuarios(
            Principal principal,
            @RequestParam(value = "tipo_usuario_id", required = false) Integer tipoUsuarioId,
            @RequestParam(value = "usuario_estado_id", required = false) Integer usuarioEstadoId) {
        var response = new ResponseDto<List<UsuarioAdminResDto>>();
        try {
            Date startDatetime = Calendar.getInstance().getTime();

            List<UsuarioAdminResDto> usuarios = adminUsuarioService.listarUsuarios(tipoUsuarioId, usuarioEstadoId);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Usuarios listados exitosamente");
            response.setHeader(headerDto);
            response.setResponse(usuarios);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "tipoUsuarioId=" + tipoUsuarioId + ", usuarioEstadoId=" + usuarioEstadoId);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al listar los usuarios");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(response.getStatusCode()).body(response);
        }
    }

    @GetMapping("/usuarios/{uuid}")
    public ResponseEntity<ResponseDto<UsuarioAdminResDto>> obtenerUsuario(Principal principal, @PathVariable String uuid) {
        var response = new ResponseDto<UsuarioAdminResDto>();
        try {
            Date startDatetime = Calendar.getInstance().getTime();

            UsuarioAdminResDto usuario = adminUsuarioService.obtenerUsuario(uuid);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Usuario obtenido exitosamente");
            response.setHeader(headerDto);
            response.setResponse(usuario);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al obtener el usuario");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @PutMapping("/usuarios/{uuid}/estado")
    public ResponseEntity<ResponseDto<UsuarioAdminResDto>> actualizarEstado(
            Principal principal,
            @PathVariable String uuid,
            @RequestBody ActualizarEstadoUsuarioReqDto request) {
        var response = new ResponseDto<UsuarioAdminResDto>();
        try {
            Date startDatetime = Calendar.getInstance().getTime();

            ErrorDetailDto validationError = adminUsuarioValidator.validateActualizarEstado(request);
            if (validationError != null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            UsuarioAdminResDto usuario = adminUsuarioService.actualizarEstado(uuid, request);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Estado de usuario actualizado exitosamente");
            response.setHeader(headerDto);
            response.setResponse(usuario);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al actualizar el estado del usuario");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    // --- Paseadores ---

    @GetMapping("/paseadores")
    public ResponseEntity<ResponseDto<List<PaseadorAdminResDto>>> listarPaseadores(
            Principal principal,
            @RequestParam(value = "estado_verificacion_id", required = false) Integer estadoVerificacionId) {
        var response = new ResponseDto<List<PaseadorAdminResDto>>();
        try {
            Date startDatetime = Calendar.getInstance().getTime();

            List<PaseadorAdminResDto> paseadores = adminUsuarioService.listarPaseadores(estadoVerificacionId);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Paseadores listados exitosamente");
            response.setHeader(headerDto);
            response.setResponse(paseadores);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "estadoVerificacionId=" + estadoVerificacionId);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al listar los paseadores");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(response.getStatusCode()).body(response);
        }
    }

    @GetMapping("/paseadores/{uuid}")
    public ResponseEntity<ResponseDto<PaseadorAdminResDto>> obtenerPaseador(Principal principal, @PathVariable String uuid) {
        var response = new ResponseDto<PaseadorAdminResDto>();
        try {
            Date startDatetime = Calendar.getInstance().getTime();

            PaseadorAdminResDto paseador = adminUsuarioService.obtenerPaseador(uuid);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Paseador obtenido exitosamente");
            response.setHeader(headerDto);
            response.setResponse(paseador);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al obtener el paseador");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @PutMapping("/paseadores/{uuid}/verificacion")
    public ResponseEntity<ResponseDto<PaseadorAdminResDto>> actualizarVerificacion(
            Principal principal,
            @PathVariable String uuid,
            @RequestBody ActualizarVerificacionPaseadorReqDto request) {
        var response = new ResponseDto<PaseadorAdminResDto>();
        try {
            Date startDatetime = Calendar.getInstance().getTime();

            ErrorDetailDto validationError = adminUsuarioValidator.validateActualizarVerificacion(request);
            if (validationError != null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            PaseadorAdminResDto paseador = adminUsuarioService.actualizarVerificacion(uuid, request);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Estado de verificacion actualizado exitosamente");
            response.setHeader(headerDto);
            response.setResponse(paseador);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al actualizar el estado de verificacion");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }
}
