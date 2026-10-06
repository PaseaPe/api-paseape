package com.paseape.apipaseape.infrastructure.controller;

import com.paseape.apipaseape.infrastructure.dto.request.*;
import com.paseape.apipaseape.infrastructure.dto.response.*;
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
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
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

            ErrorDetailDto validationError = authValidator.validateGoogleAuthRequest(request);
            if (validationError != null) {
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            AuthResDto authResDto = authService.authenticateWithGoogle(request);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Autenticacion con Google procesada exitosamente");
            response.setHeader(headerDto);
            response.setResponse(authResDto);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "loginGoogle");
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "loginGoogle");
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al procesar la autenticacion con Google");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<AuthResDto>> loginLocal(@RequestBody LoginReqDto request) {
        var response = new ResponseDto<AuthResDto>();
        try {
            Date startDatetime = Calendar.getInstance().getTime();

            ErrorDetailDto validationError = authValidator.validateLoginLocal(request);
            if (validationError != null) {
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            AuthResDto authResDto = authService.loginLocal(request);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Inicio de sesion local procesado exitosamente");
            response.setHeader(headerDto);
            response.setResponse(authResDto);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "correo=" + (request != null ? request.getCorreo() : "null"));
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "correo=" + (request != null ? request.getCorreo() : "null"));
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al procesar el inicio de sesion local");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @PostMapping("/register/google")
    public ResponseEntity<ResponseDto<AuthResDto>> registerWithGoogle(@RequestBody UsuarioReqDto request) {
        var response = new ResponseDto<AuthResDto>();
        try {
            Date startDatetime = Calendar.getInstance().getTime();

            ErrorDetailDto validationError = authValidator.validateRegisterGoogle(request);
            if (validationError != null) {
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            AuthResDto authResDto = authService.registerWithGoogle(request);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code201);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Usuario registrado e iniciado exitosamente mediante Google");
            response.setHeader(headerDto);
            response.setResponse(authResDto);
            return ResponseEntity.status(StatusCodes.Code201).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "tipoUsuario=" + (request != null ? request.getTipoUsuarioId() : "null"));
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "tipoUsuario=" + (request != null ? request.getTipoUsuarioId() : "null"));
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @PostMapping("/register/local")
    public ResponseEntity<ResponseDto<AuthResDto>> registerLocal(@RequestBody UsuarioReqDto request) {
        var response = new ResponseDto<AuthResDto>();
        try {
            Date startDatetime = Calendar.getInstance().getTime();

            ErrorDetailDto validationError = authValidator.validateRegisterLocal(request);
            if (validationError != null) {
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            AuthResDto authResDto = authService.registerLocal(request);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code201);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Usuario registrado e iniciado exitosamente");
            response.setHeader(headerDto);
            response.setResponse(authResDto);
            return ResponseEntity.status(StatusCodes.Code201).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "correo=" + (request != null ? request.getCorreo() : "null"));
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "correo=" + (request != null ? request.getCorreo() : "null"));
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage(ex.getMessage());
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
                response.setMessage("No se encontro una sesion activa autenticada");
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }
            String userId = principal.getName();
            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);
            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Sesion activa y verificada exitosamente");
            response.setHeader(headerDto);
            response.setResponse("Usuario autenticado con identificador: " + userId);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "principal=" + (principal != null ? principal.getName() : "null"));
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Fallo interno al validar el token de sesion");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseDto<LogoutResDto>> logout(Principal principal) {
        var response = new ResponseDto<LogoutResDto>();
        try {
            Date startDatetime = Calendar.getInstance().getTime();

            if (principal == null || principal.getName() == null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(StatusCodes.Code401);
                response.setCode(MessageCodes.ResponseCodeE01);
                response.setMessage("No se encontro una sesion activa autenticada");
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            LogoutResDto logoutResDto = authService.logout(principal.getName());

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Cierre de sesion completado exitosamente");
            response.setHeader(headerDto);
            response.setResponse(logoutResDto);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "principal=" + (principal != null ? principal.getName() : "null"));
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al procesar el cierre de sesion");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ResponseDto<ForgotPasswordResDto>> forgotPassword(@RequestBody ForgotPasswordReqDto request) {
        var response = new ResponseDto<ForgotPasswordResDto>();
        try {
            Date startDatetime = Calendar.getInstance().getTime();

            ErrorDetailDto validationError = authValidator.validateForgotPassword(request);
            if (validationError != null) {
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            ForgotPasswordResDto result = authService.forgotPassword(request);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Solicitud de restablecimiento procesada exitosamente");
            response.setHeader(headerDto);
            response.setResponse(result);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "correo=" + (request != null ? request.getCorreo() : "null"));
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "correo=" + (request != null ? request.getCorreo() : "null"));
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al solicitar el restablecimiento de contrasena");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResponseDto<String>> resetPassword(@RequestBody ResetPasswordReqDto request) {
        var response = new ResponseDto<String>();
        try {
            Date startDatetime = Calendar.getInstance().getTime();

            ErrorDetailDto validationError = authValidator.validateResetPassword(request);
            if (validationError != null) {
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            authService.resetPassword(request);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Contrasena actualizada exitosamente");
            response.setHeader(headerDto);
            response.setResponse("La contrasena ha sido cambiada correctamente.");
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "resetPassword");
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "resetPassword");
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al restablecer la contrasena");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }
}