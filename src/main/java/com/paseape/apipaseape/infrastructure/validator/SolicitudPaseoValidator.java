package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudInmediataReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class SolicitudPaseoValidator {

    private static final Pattern UUID_PATTERN = Pattern.compile(Constant.REGEX_UUID);

    public ErrorDetailDto validarCrearSolicitudInmediata(CrearSolicitudInmediataReqDto reqDto) {
        if (reqDto == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El cuerpo de la solicitud no puede estar vacío.");
        }

        List<String> mascotaUuids = reqDto.getMascotaUuids();
        if (mascotaUuids == null || mascotaUuids.isEmpty()) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "La lista de mascotas es obligatoria y debe contener al menos un elemento.");
        }

        Set<String> uniqueUuids = new HashSet<>();
        for (String mascotaUuid : mascotaUuids) {
            if (!StringUtils.hasText(mascotaUuid)) {
                return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El identificador de la mascota no puede estar vacío.");
            }
            String trimmedUuid = mascotaUuid.trim();
            if (!UUID_PATTERN.matcher(trimmedUuid).matches()) {
                return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El identificador de mascota '" + mascotaUuid + "' no tiene un formato UUID válido.");
            }
            if (!uniqueUuids.add(trimmedUuid.toLowerCase())) {
                return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "La lista de mascotas no puede contener elementos duplicados.");
            }
        }

        if (reqDto.getDistritoId() == null || reqDto.getDistritoId() <= 0) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'distrito_id' es obligatorio y debe ser un valor positivo.");
        }

        if (reqDto.getTipoDuracionPaseoId() == null || reqDto.getTipoDuracionPaseoId() <= 0) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'tipo_duracion_paseo_id' es obligatorio y debe ser un valor positivo.");
        }

        if (reqDto.getTipoMetodoPagoId() == null || reqDto.getTipoMetodoPagoId() <= 0) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'tipo_metodo_pago_id' es obligatorio y debe ser un valor positivo.");
        }

        if (!StringUtils.hasText(reqDto.getDireccionRecojo())) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'direccion_recojo' es obligatorio y no puede estar vacío.");
        }

        if (reqDto.getDireccionRecojo().trim().length() > 255) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'direccion_recojo' no debe superar los 255 caracteres.");
        }

        if (StringUtils.hasText(reqDto.getReferenciaDireccion()) && reqDto.getReferenciaDireccion().trim().length() > 255) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'referencia_direccion' no debe superar los 255 caracteres.");
        }

        return null;
    }

    public ErrorDetailDto validarUuid(String uuid, String nombreCampo) {
        if (!StringUtils.hasText(uuid)) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El identificador " + nombreCampo + " es obligatorio.");
        }
        if (!UUID_PATTERN.matcher(uuid.trim()).matches()) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El formato del identificador " + nombreCampo + " no es válido.");
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
