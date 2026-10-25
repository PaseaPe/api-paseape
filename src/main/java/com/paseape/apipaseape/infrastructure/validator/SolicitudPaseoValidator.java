package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CancelarSolicitudReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudInmediataReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.RechazarSolicitudReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudProgramadaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
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

        if (reqDto.getTarifaHoraPen() == null || reqDto.getTarifaHoraPen().compareTo(BigDecimal.ZERO) <= 0) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'tarifa_hora_pen' es obligatorio y debe ser mayor a 0.");
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

    public ErrorDetailDto validarCrearSolicitudProgramada(CrearSolicitudProgramadaReqDto reqDto) {
        if (reqDto == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El cuerpo de la solicitud no puede estar vacío.");
        }

        if (!StringUtils.hasText(reqDto.getPaseadorUuid())) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'paseador_uuid' es obligatorio.");
        }
        String trimmedPaseadorUuid = reqDto.getPaseadorUuid().trim();
        if (!UUID_PATTERN.matcher(trimmedPaseadorUuid).matches()) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El identificador del paseador '" + reqDto.getPaseadorUuid() + "' no tiene un formato UUID válido.");
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

        if (reqDto.getFechaProgramada() == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'fecha_programada' es obligatorio.");
        }

        if (reqDto.getBloqueHorarioId() == null || reqDto.getBloqueHorarioId() <= 0) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'bloque_horario_id' es obligatorio y debe ser un valor positivo.");
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

    public ErrorDetailDto validarConsultaBandejaCliente(String tab, Integer page, Integer size) {
        if (!StringUtils.hasText(tab)) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El parámetro 'tab' es estrictamente obligatorio.");
        }
        String tabUpper = tab.trim().toUpperCase();
        if (!Constant.TAB_CLIENTE_ACTIVOS.equals(tabUpper) &&
                !Constant.TAB_CLIENTE_PENDIENTES.equals(tabUpper) &&
                !Constant.TAB_CLIENTE_HISTORIAL.equals(tabUpper)) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, Constant.MSJ_ERROR_TAB_CLIENTE_INVALIDO);
        }
        if (page == null || page < 0) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El número de página no puede ser negativo.");
        }
        if (size == null || size < 1 || size > Constant.SIZE_MAXIMO) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El tamaño de página debe estar entre 1 y " + Constant.SIZE_MAXIMO + ".");
        }
        return null;
    }

    public ErrorDetailDto validarCancelarSolicitudCliente(CancelarSolicitudReqDto reqDto) {
        if (reqDto != null && StringUtils.hasText(reqDto.getMotivo()) && reqDto.getMotivo().trim().length() > 500) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El motivo de cancelación no debe superar los 500 caracteres.");
        }
        return null;
    }

    public ErrorDetailDto validarConsultaBandejaPaseador(String tab, Integer page, Integer size) {
        if (!StringUtils.hasText(tab)) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El parámetro 'tab' es estrictamente obligatorio.");
        }
        String tabUpper = tab.trim().toUpperCase();
        if (!Constant.TAB_INMEDIATAS.equals(tabUpper) &&
            !Constant.TAB_MIS_PASEOS.equals(tabUpper) &&
            !Constant.TAB_EN_CURSO.equals(tabUpper) &&
            !Constant.TAB_HISTORIAL.equals(tabUpper)) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, Constant.MSJ_ERROR_TAB_INVALIDO);
        }
        if (page == null || page < 0) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El número de página no puede ser negativo.");
        }
        if (size == null || size < 1 || size > Constant.SIZE_MAXIMO) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El tamaño de página debe estar entre 1 y " + Constant.SIZE_MAXIMO + ".");
        }
        return null;
    }

    public ErrorDetailDto validarCancelarSolicitud(CancelarSolicitudReqDto reqDto) {
        if (reqDto == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El cuerpo de la solicitud no puede estar vacío.");
        }
        if (!StringUtils.hasText(reqDto.getMotivo()) || reqDto.getMotivo().trim().length() < 10) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, Constant.MSJ_ERROR_MOTIVO_CANCELACION_OBLIGATORIO);
        }
        if (reqDto.getMotivo().trim().length() > 500) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El motivo de cancelación no debe superar los 500 caracteres.");
        }
        return null;
    }

    public ErrorDetailDto validarRechazarSolicitud(RechazarSolicitudReqDto reqDto) {
        if (reqDto != null && StringUtils.hasText(reqDto.getMotivoRechazo()) && reqDto.getMotivoRechazo().trim().length() > 500) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El motivo de rechazo no debe superar los 500 caracteres.");
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
