package com.paseape.apipaseape.infrastructure.validator;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarAdministradorReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarEstadoUsuarioReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarVerificacionPaseadorReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.CrearAdministradorReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;

import java.util.regex.Pattern;

import static com.paseape.apipaseape.infrastructure.constant.Constant.*;

@Component
public class AdminUsuarioValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int MIN_CONTRASENA_LENGTH = 6;
    private static final int MAX_CONTRASENA_LENGTH = 50;
    private static final int MAX_NOMBRE_APELLIDO_LENGTH = 100;
    private static final int MAX_CORREO_LENGTH = 150;
    private static final int MAX_TELEFONO_LENGTH = 15;
    private static final int MAX_AREA_LENGTH = 100;
    private static final int MAX_FOTO_URL_LENGTH = 255;
    private static final int UUID_LENGTH = 36;

    // =========================================================================
    // 1. VALIDACIÓN: CREAR ADMINISTRADOR
    // =========================================================================
    public ErrorDetailDto validateCrearAdministrador(CrearAdministradorReqDto request) {
        if (request == null) {
            return buildError("El cuerpo de la solicitud no puede estar vacio.");
        }
        if (!StringUtils.hasText(request.getNombres()) || request.getNombres().trim().length() > MAX_NOMBRE_APELLIDO_LENGTH) {
            return buildError("Los nombres son obligatorios y no deben superar 100 caracteres.");
        }
        if (!StringUtils.hasText(request.getApellidos()) || request.getApellidos().trim().length() > MAX_NOMBRE_APELLIDO_LENGTH) {
            return buildError("Los apellidos son obligatorios y no deben superar 100 caracteres.");
        }
        if (!StringUtils.hasText(request.getCorreo()) || request.getCorreo().trim().length() > MAX_CORREO_LENGTH) {
            return buildError("El correo electronico es obligatorio y no debe superar 150 caracteres.");
        }
        if (!EMAIL_PATTERN.matcher(request.getCorreo().trim()).matches()) {
            return buildError("El formato del correo electronico no es valido.");
        }
        if (!StringUtils.hasText(request.getContrasena())
                || request.getContrasena().length() < MIN_CONTRASENA_LENGTH
                || request.getContrasena().length() > MAX_CONTRASENA_LENGTH) {
            return buildError("La contrasena debe contener entre " + MIN_CONTRASENA_LENGTH + " y " + MAX_CONTRASENA_LENGTH + " caracteres.");
        }
        if (StringUtils.hasText(request.getTelefono()) && request.getTelefono().trim().length() > MAX_TELEFONO_LENGTH) {
            return buildError("El telefono no debe superar 15 caracteres.");
        }
        if (StringUtils.hasText(request.getAreaDepartamento()) && request.getAreaDepartamento().trim().length() > MAX_AREA_LENGTH) {
            return buildError("El area o departamento no debe superar 100 caracteres.");
        }
        if (StringUtils.hasText(request.getFotoPerfilUrl()) && request.getFotoPerfilUrl().trim().length() > MAX_FOTO_URL_LENGTH) {
            return buildError("La URL de la foto de perfil no debe superar 255 caracteres.");
        }
        if (request.getSuperadmin() != null && request.getSuperadmin() != 0 && request.getSuperadmin() != 1) {
            return buildError("El campo 'superadmin' debe ser 0 o 1.");
        }
        return null;
    }

    // =========================================================================
    // 2. VALIDACIÓN: ACTUALIZAR ADMINISTRADOR
    // =========================================================================
    public ErrorDetailDto validateActualizarAdministrador(String adminUuid, ActualizarAdministradorReqDto request) {
        ErrorDetailDto uuidError = validateUuid(adminUuid, "uuid");
        if (uuidError != null) {
            return uuidError;
        }
        if (request == null) {
            return buildError("El cuerpo de la solicitud no puede estar vacio.");
        }
        if (StringUtils.hasText(request.getNombres()) && request.getNombres().trim().length() > MAX_NOMBRE_APELLIDO_LENGTH) {
            return buildError("Los nombres no deben superar 100 caracteres.");
        }
        if (StringUtils.hasText(request.getApellidos()) && request.getApellidos().trim().length() > MAX_NOMBRE_APELLIDO_LENGTH) {
            return buildError("Los apellidos no deben superar 100 caracteres.");
        }
        if (StringUtils.hasText(request.getTelefono()) && request.getTelefono().trim().length() > MAX_TELEFONO_LENGTH) {
            return buildError("El telefono no debe superar 15 caracteres.");
        }
        if (StringUtils.hasText(request.getAreaDepartamento()) && request.getAreaDepartamento().trim().length() > MAX_AREA_LENGTH) {
            return buildError("El area o departamento no debe superar 100 caracteres.");
        }
        if (StringUtils.hasText(request.getFotoPerfilUrl()) && request.getFotoPerfilUrl().trim().length() > MAX_FOTO_URL_LENGTH) {
            return buildError("La URL de la foto de perfil no debe superar 255 caracteres.");
        }
        if (request.getSuperadmin() != null && request.getSuperadmin() != 0 && request.getSuperadmin() != 1) {
            return buildError("El campo 'superadmin' debe ser 0 o 1.");
        }
        if (request.getUsuarioEstadoId() != null) {
            int estadoId = request.getUsuarioEstadoId();
            if (estadoId != ID_ACTIVO && estadoId != ID_INACTIVO && estadoId != ID_SUSPENDIDO && estadoId != ID_BLOQUEADO) {
                return buildError("Estado de usuario no permitido. Valores validos: 1 (ACTIVO), 2 (INACTIVO), 4 (SUSPENDIDO), 5 (BLOQUEADO).");
            }
        }
        return null;
    }

    // =========================================================================
    // 3. VALIDACIÓN: ACTUALIZAR ESTADO DE USUARIO (HU35)
    // =========================================================================
    public ErrorDetailDto validateActualizarEstado(ActualizarEstadoUsuarioReqDto request) {
        if (request == null) {
            return buildError("El cuerpo de la solicitud no puede estar vacio.");
        }

        if (request.getUsuarioEstadoId() == null) {
            return buildError("El campo 'usuario_estado_id' es estrictamente obligatorio.");
        }

        Integer usuarioEstadoId = request.getUsuarioEstadoId();
        if (usuarioEstadoId != ID_ACTIVO
                && usuarioEstadoId != ID_INACTIVO
                && usuarioEstadoId != ID_SUSPENDIDO
                && usuarioEstadoId != ID_BLOQUEADO) {
            return buildError("Estado de usuario no permitido. Valores validos: 1 (ACTIVO), 2 (INACTIVO), 4 (SUSPENDIDO), 5 (BLOQUEADO).");
        }

        return null;
    }

    public ErrorDetailDto validateActualizarEstado(String usuarioUuid, ActualizarEstadoUsuarioReqDto request) {
        ErrorDetailDto uuidError = validateUuid(usuarioUuid, "usuario_uuid");
        if (uuidError != null) {
            return uuidError;
        }
        return validateActualizarEstado(request);
    }

    // =========================================================================
    // 4. VALIDACIÓN: ACTUALIZAR VERIFICACIÓN DE PASEADOR (HU36)
    // =========================================================================
    public ErrorDetailDto validateActualizarVerificacion(ActualizarVerificacionPaseadorReqDto request) {
        if (request == null) {
            return buildError("El cuerpo de la solicitud no puede estar vacio.");
        }

        if (request.getEstadoVerificacionId() == null) {
            return buildError("El campo 'estado_verificacion_id' es estrictamente obligatorio.");
        }

        Integer estadoVerificacionId = request.getEstadoVerificacionId();
        if (estadoVerificacionId != ID_EN_REVISION
                && estadoVerificacionId != ID_APROBADO
                && estadoVerificacionId != ID_RECHAZADO
                && estadoVerificacionId != ID_OBSERVADO) {
            return buildError("Estado de verificacion no permitido. Valores validos: 2 (EN_REVISION), 3 (APROBADO), 4 (RECHAZADO), 5 (OBSERVADO).");
        }

        return null;
    }

    public ErrorDetailDto validateActualizarVerificacion(String paseadorUuid, ActualizarVerificacionPaseadorReqDto request) {
        ErrorDetailDto uuidError = validateUuid(paseadorUuid, "paseador_uuid");
        if (uuidError != null) {
            return uuidError;
        }
        return validateActualizarVerificacion(request);
    }

    // =========================================================================
    // 5. MÉTODOS AUXILIARES DE VALIDACIÓN Y CONSTRUCCIÓN DE ERROR
    // =========================================================================
    public ErrorDetailDto validateUuid(String uuid, String fieldName) {
        if (!StringUtils.hasText(uuid)) {
            return buildError("El parametro '" + fieldName + "' es estrictamente obligatorio.");
        }
        if (uuid.trim().length() != UUID_LENGTH) {
            return buildError("El formato de '" + fieldName + "' no es valido (debe contener 36 caracteres).");
        }
        return null;
    }

    private ErrorDetailDto buildError(String message) {
        return ErrorDetailDto.builder()
                .statusCode(StatusCodes.Code400)
                .code(MessageCodes.ResponseCodeBR01)
                .message(message)
                .build();
    }
}