package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.infrastructure.dto.request.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import static com.paseape.apipaseape.infrastructure.constant.Constant.*;

@Component
public class AuthValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final int MIN_CONTRASENA_LENGTH = 6;
    private static final int MAX_CONTRASENA_LENGTH = 50;

    public ErrorDetailDto validateGoogleAuthRequest(GoogleAuthReqDto request) {
        if (request == null || !StringUtils.hasText(request.getIdToken())) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El parametro 'id_token' es estrictamente obligatorio.");
        }
        return null;
    }

    public ErrorDetailDto validateLoginLocal(LoginReqDto request) {
        if (request == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El cuerpo de la solicitud no puede estar vacio.");
        }
        if (!StringUtils.hasText(request.getCorreo()) || !EMAIL_PATTERN.matcher(request.getCorreo().trim()).matches()) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El correo electronico es obligatorio y debe tener un formato valido.");
        }
        if (!StringUtils.hasText(request.getContrasena()) ||
                request.getContrasena().length() < MIN_CONTRASENA_LENGTH ||
                request.getContrasena().length() > MAX_CONTRASENA_LENGTH) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "La contrasena debe contener entre " + MIN_CONTRASENA_LENGTH + " y " + MAX_CONTRASENA_LENGTH + " caracteres.");
        }
        return null;
    }

    public ErrorDetailDto validateRegisterGoogle(UsuarioReqDto request) {
        if (request == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El cuerpo de la solicitud no puede estar vacio.");
        }
        if (!StringUtils.hasText(request.getIdToken())) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El token de autenticacion de Google (id_token) es obligatorio.");
        }

        ErrorDetailDto rolError = validateTipoUsuario(request.getTipoUsuarioId());
        if (rolError != null) {
            return rolError;
        }

        return validateSubtipoData(request);
    }

    public ErrorDetailDto validateRegisterLocal(UsuarioReqDto request) {
        if (request == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El cuerpo de la solicitud no puede estar vacio.");
        }
        if (!StringUtils.hasText(request.getCorreo()) || !EMAIL_PATTERN.matcher(request.getCorreo().trim()).matches()) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El correo electronico es obligatorio y debe tener un formato valido.");
        }
        if (!StringUtils.hasText(request.getContrasena()) ||
                request.getContrasena().length() < MIN_CONTRASENA_LENGTH ||
                request.getContrasena().length() > MAX_CONTRASENA_LENGTH) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "La contrasena debe contener entre " + MIN_CONTRASENA_LENGTH + " y " + MAX_CONTRASENA_LENGTH + " caracteres.");
        }
        if (!StringUtils.hasText(request.getNombres())) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "Los nombres del usuario son obligatorios.");
        }
        if (!StringUtils.hasText(request.getApellidos())) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "Los apellidos del usuario son obligatorios.");
        }

        ErrorDetailDto rolError = validateTipoUsuario(request.getTipoUsuarioId());
        if (rolError != null) {
            return rolError;
        }

        return validateSubtipoData(request);
    }

    private ErrorDetailDto validateTipoUsuario(Integer tipoUsuarioId) {
        if (tipoUsuarioId == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'tipo_usuario_id' es estrictamente obligatorio.");
        }
        if (tipoUsuarioId == ID_ADMINISTRADOR) {
            return buildError(StatusCodes.Code403, MessageCodes.ResponseCodeBR04, "El registro de cuentas administrativas no esta permitido a traves del API.");
        }
        if (tipoUsuarioId != ID_CLIENTE && tipoUsuarioId != ID_PASEADOR) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR02, "Tipo de usuario no reconocido. Solo se permite 1 (CLIENTE) o 2 (PASEADOR).");
        }
        return null;
    }

    private ErrorDetailDto validateSubtipoData(UsuarioReqDto request) {
        if (request.getTipoUsuarioId() == ID_CLIENTE) {
            if (request.getMascota() != null) {
                return validateMascota(request.getMascota());
            }
        } else if (request.getTipoUsuarioId() == ID_PASEADOR) {
            if (request.getTipoDocumentoId() == null) {
                return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El tipo de documento es obligatorio para el perfil paseador.");
            }
            if (!StringUtils.hasText(request.getNumeroDocumento())) {
                return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El numero de documento es obligatorio para el perfil paseador.");
            }
            if (request.getTarifaHoraPen() != null && request.getTarifaHoraPen().compareTo(BigDecimal.ZERO) < 0) {
                return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "La tarifa por hora no puede ser negativa.");
            }
        }
        return null;
    }

    public ErrorDetailDto validateMascota(MascotaReqDto mascota) {
        if (!StringUtils.hasText(mascota.getNombre())) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El nombre de la mascota es obligatorio.");
        }
        if (mascota.getTipoMascotaId() == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El tipo de mascota es obligatorio.");
        }
        if (mascota.getTipoRazaId() == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "La raza de la mascota es obligatoria.");
        }
        if (mascota.getTipoGeneroMascotaId() == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El genero de la mascota es obligatorio.");
        }
        if (mascota.getTipoTamanoMascotaId() == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El tamano de la mascota es obligatorio.");
        }
        if (mascota.getTipoNivelEnergiaId() == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El nivel de energia de la mascota es obligatorio.");
        }
        if (mascota.getPesoKg() != null && mascota.getPesoKg().compareTo(BigDecimal.ZERO) <= 0) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El peso de la mascota debe ser mayor a 0 kg.");
        }
        return null;
    }

    public ErrorDetailDto validateForgotPassword(ForgotPasswordReqDto request) {
        if (request == null || !StringUtils.hasText(request.getCorreo()) || !EMAIL_PATTERN.matcher(request.getCorreo().trim()).matches()) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El correo electronico es obligatorio y debe tener formato valido.");
        }
        return null;
    }

    public ErrorDetailDto validateResetPassword(ResetPasswordReqDto request) {
        if (request == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El cuerpo de la solicitud no puede estar vacio.");
        }
        if (!StringUtils.hasText(request.getToken())) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El token de restablecimiento es estrictamente obligatorio.");
        }
        if (!StringUtils.hasText(request.getNuevaContrasena()) ||
                request.getNuevaContrasena().length() < MIN_CONTRASENA_LENGTH ||
                request.getNuevaContrasena().length() > MAX_CONTRASENA_LENGTH) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "La nueva contrasena debe contener entre " + MIN_CONTRASENA_LENGTH + " y " + MAX_CONTRASENA_LENGTH + " caracteres.");
        }
        return null;
    }

    private ErrorDetailDto buildError(int statusCode, String code, String message) {
        return ErrorDetailDto.builder()
                .statusCode(statusCode)
                .code(code)
                .message(message)
                .build();
    }
}