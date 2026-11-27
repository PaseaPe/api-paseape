package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IPaseadorRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.RegistrarCalificacionReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class CalificacionValidator {

    private final IUsuarioRepository usuarioRepository;
    private final IClienteRepository clienteRepository;
    private final IPaseadorRepository paseadorRepository;

    private static final Pattern UUID_PATTERN = Pattern.compile(Constant.REGEX_UUID);

    public ResponseDto<?> validateRegistrarCalificacion(
            String userEmail,
            String solicitudUuid,
            RegistrarCalificacionReqDto reqDto
    ) {
        try {
            validateClienteAutenticado(userEmail);
            validateUuid(solicitudUuid, "solicitud de paseo");

            if (reqDto == null || reqDto.getPuntuacion() == null) {
                throw new BadRequestException("La puntuación es obligatoria.");
            }
            if (reqDto.getPuntuacion() < Constant.MIN_PUNTUACION_CALIFICACION
                    || reqDto.getPuntuacion() > Constant.MAX_PUNTUACION_CALIFICACION) {
                throw new BadRequestException("La puntuación debe estar entre 1 y 5 estrellas.");
            }
            if (reqDto.getComentario() != null && !reqDto.getComentario().trim().isEmpty()) {
                int len = reqDto.getComentario().trim().length();
                if (len < Constant.MIN_LONGITUD_OPINION || len > Constant.MAX_LONGITUD_OPINION) {
                    throw new BadRequestException("La opinión debe tener entre 5 y 1000 caracteres.");
                }
            }
        } catch (BadRequestException brex) {
            return BuildErrorForValidation(
                    brex.statusCode != null ? brex.statusCode : StatusCodes.Code400,
                    brex.code != null ? brex.code : MessageCodes.ResponseCodeBR01,
                    brex.getMessage()
            );
        }
        return null;
    }

    public ResponseDto<?> validateConsultarCalificacionesPaseadorAutenticado(String userEmail, int page, int size) {
        try {
            Usuario usuario = validateUsuarioActivo(userEmail);
            if (usuario.getTipoUsuario() == null
                    || !Integer.valueOf(Constant.ID_PASEADOR).equals(usuario.getTipoUsuario().getId())) {
                throw new BadRequestException(MessageCodes.ResponseCodeBR03, "Solo los paseadores pueden consultar su panel de calificaciones.");
            }
            Paseador paseador = paseadorRepository.findByUsuarioId(usuario.getId());
            if (paseador == null) {
                throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El usuario autenticado no tiene perfil de paseador.");
            }
            validatePaginacion(page, size);
        } catch (BadRequestException brex) {
            return BuildErrorForValidation(
                    brex.statusCode != null ? brex.statusCode : StatusCodes.Code400,
                    brex.code != null ? brex.code : MessageCodes.ResponseCodeBR01,
                    brex.getMessage()
            );
        }
        return null;
    }

    public ResponseDto<?> validateConsultarCalificacionesPaseadorPublico(
            String userEmail,
            String paseadorUuid,
            int page,
            int size
    ) {
        try {
            validateUsuarioActivo(userEmail);
            validateUuid(paseadorUuid, "paseador");
            validatePaginacion(page, size);
        } catch (BadRequestException brex) {
            return BuildErrorForValidation(
                    brex.statusCode != null ? brex.statusCode : StatusCodes.Code400,
                    brex.code != null ? brex.code : MessageCodes.ResponseCodeBR01,
                    brex.getMessage()
            );
        }
        return null;
    }

    private Usuario validateUsuarioActivo(String userEmail) throws BadRequestException {
        if (ObjectUtils.isEmpty(userEmail)) {
            throw new BadRequestException(MessageCodes.ResponseCodeE01, "No se encontró una sesión activa autenticada.");
        }
        Usuario usuario = usuarioRepository.findByCorreo(userEmail.trim().toLowerCase());
        if (usuario == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el usuario autenticado.");
        }
        if (usuario.getUsuarioEstado() != null
                && !Integer.valueOf(Constant.ID_ACTIVO).equals(usuario.getUsuarioEstado().getId())) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR04, "La cuenta de usuario no se encuentra activa.");
        }
        return usuario;
    }

    private void validateClienteAutenticado(String userEmail) throws BadRequestException {
        Usuario usuario = validateUsuarioActivo(userEmail);
        if (usuario.getTipoUsuario() == null
                || !Integer.valueOf(Constant.ID_CLIENTE).equals(usuario.getTipoUsuario().getId())) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "Solo los clientes pueden realizar esta operación.");
        }
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId());
        if (cliente == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El usuario autenticado no tiene perfil de cliente.");
        }
    }

    private void validatePaginacion(int page, int size) throws BadRequestException {
        if (page < 0) {
            throw new BadRequestException("El número de página no puede ser negativo.");
        }
        if (size < 1 || size > Constant.SIZE_MAXIMO) {
            throw new BadRequestException("El tamaño de página debe estar entre 1 y " + Constant.SIZE_MAXIMO + ".");
        }
    }

    private void validateUuid(String uuid, String nombreEntidad) throws BadRequestException {
        if (ObjectUtils.isEmpty(uuid)) {
            throw new BadRequestException("El identificador de " + nombreEntidad + " no debe estar vacío.");
        }
        if (!UUID_PATTERN.matcher(uuid.trim()).matches()) {
            throw new BadRequestException("El identificador de " + nombreEntidad + " no tiene un formato UUID válido.");
        }
    }

    private ResponseDto<?> BuildErrorForValidation(int statusCode, String code, String message) {
        var response = new ResponseDto<>();
        response.setStatusCode(statusCode);
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
}
