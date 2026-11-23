package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CambiarDistritoCoberturaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class PaseadorValidator {
    private final IUsuarioRepository usuarioRepository;
    private final StorageValidator storageValidator;

    public ErrorDetailDto validateSubirFotoUsuario(Principal principal, MultipartFile file) {
        if (principal == null || !StringUtils.hasText(principal.getName())) {
            return buildError(StatusCodes.Code401, MessageCodes.ResponseCodeE01, "No se encontro una sesion activa autenticada");
        }

        Usuario usuario = usuarioRepository.findByCorreo(principal.getName());
        if (usuario == null) {
            return buildError(StatusCodes.Code404, MessageCodes.ResponseCodeBR98, "Usuario no encontrado");
        }

        ErrorDetailDto fileError = storageValidator.validateUpload(file);
        if (fileError != null) {
            return fileError;
        }

        return null;
    }

    public ErrorDetailDto validateCambiarDistritoCobertura(CambiarDistritoCoberturaReqDto request) {
        if (request == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El cuerpo de la solicitud no puede estar vacio.");
        }
        if (!StringUtils.hasText(request.getUsuarioUuid())) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'usuario_uuid' es estrictamente obligatorio.");
        }
        if (request.getDistritoId() == null || request.getDistritoId() <= 0) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'distrito_id' es obligatorio y debe ser mayor a 0.");
        }
        return null;
    }

    public ErrorDetailDto validarConsultaDisponibilidadPorFecha(String paseadorUuid, LocalDate fecha) {
        if (!StringUtils.hasText(paseadorUuid)) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El identificador del paseador es estrictamente obligatorio.");
        }
        if (!paseadorUuid.trim().matches(Constant.REGEX_UUID)) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El formato del UUID del paseador no es válido.");
        }
        if (fecha != null) {
            LocalDate hoyLima = LocalDate.now(ZoneId.of(Constant.ZONE_ID_LIMA));
            if (fecha.isBefore(hoyLima)) {
                return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "La fecha de consulta no puede ser anterior a la fecha actual.");
            }
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
