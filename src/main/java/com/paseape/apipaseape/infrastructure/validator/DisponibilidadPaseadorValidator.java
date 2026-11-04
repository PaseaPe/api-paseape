package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarBloqueDisponibilidadReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarCapacidadBaseReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.DisponibilidadItemReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.GuardarDisponibilidadReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

@Component
public class DisponibilidadPaseadorValidator {

    public ErrorDetailDto validarGuardarDisponibilidad(GuardarDisponibilidadReqDto reqDto) {
        if (reqDto == null || reqDto.getBloques() == null || reqDto.getBloques().isEmpty()) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "La lista de bloques de disponibilidad no puede ser nula ni vacía.");
        }

        Set<String> combinaciones = new HashSet<>();
        for (DisponibilidadItemReqDto item : reqDto.getBloques()) {
            if (item == null) {
                return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El elemento de bloque no puede ser nulo.");
            }
            if (item.getDiaSemanaId() == null || item.getDiaSemanaId() < 1 || item.getDiaSemanaId() > 7) {
                return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'dia_semana_id' es obligatorio y debe encontrarse entre 1 y 7.");
            }
            if (item.getBloqueHorarioId() == null || item.getBloqueHorarioId() <= 0) {
                return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'bloque_horario_id' es obligatorio y debe ser un valor positivo.");
            }
            if (item.getMaxMascotas() != null) {
                if (item.getMaxMascotas() < Constant.MIN_MASCOTAS_PERMITIDAS || item.getMaxMascotas() > Constant.MAX_MASCOTAS_PERMITIDAS) {
                    return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El cupo de mascotas debe encontrarse entre "
                            + Constant.MIN_MASCOTAS_PERMITIDAS + " y " + Constant.MAX_MASCOTAS_PERMITIDAS + ".");
                }
            }
            String key = item.getDiaSemanaId() + "-" + item.getBloqueHorarioId();
            if (!combinaciones.add(key)) {
                return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "Existen bloques duplicados para el día "
                        + item.getDiaSemanaId() + " y bloque " + item.getBloqueHorarioId() + ".");
            }
        }
        return null;
    }

    public ErrorDetailDto validarActualizarBloque(ActualizarBloqueDisponibilidadReqDto reqDto) {
        if (reqDto == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El cuerpo de la solicitud no puede estar vacío.");
        }
        if (reqDto.getDiaSemanaId() == null || reqDto.getDiaSemanaId() < 1 || reqDto.getDiaSemanaId() > 7) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'dia_semana_id' es obligatorio y debe encontrarse entre 1 y 7.");
        }
        if (reqDto.getBloqueHorarioId() == null || reqDto.getBloqueHorarioId() <= 0) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'bloque_horario_id' es obligatorio y debe ser un valor positivo.");
        }
        if (!StringUtils.hasText(reqDto.getAccion())) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'accion' es obligatorio.");
        }

        String accion = reqDto.getAccion().trim();
        boolean validAccion = accion.equalsIgnoreCase(Constant.ACCION_BLOQUE_GUARDAR_CUPO)
                || accion.equalsIgnoreCase(Constant.ACCION_BLOQUE_REESTABLECER)
                || accion.equalsIgnoreCase(Constant.ACCION_BLOQUE_CANCELAR);

        if (!validAccion) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "La acción especificada no es válida. Valores permitidos: GUARDAR_CUPO, REESTABLECER_PREDETERMINADO, CANCELAR.");
        }

        if (accion.equalsIgnoreCase(Constant.ACCION_BLOQUE_GUARDAR_CUPO)) {
            if (reqDto.getMaxMascotas() == null) {
                return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'max_mascotas' es obligatorio para la acción GUARDAR_CUPO.");
            }
            if (reqDto.getMaxMascotas() < Constant.MIN_MASCOTAS_PERMITIDAS || reqDto.getMaxMascotas() > Constant.MAX_MASCOTAS_PERMITIDAS) {
                return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El cupo de mascotas debe encontrarse entre "
                        + Constant.MIN_MASCOTAS_PERMITIDAS + " y " + Constant.MAX_MASCOTAS_PERMITIDAS + ".");
            }
        }

        return null;
    }

    public ErrorDetailDto validarActualizarCapacidadBase(ActualizarCapacidadBaseReqDto reqDto) {
        if (reqDto == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El cuerpo de la solicitud no puede estar vacío.");
        }
        if (reqDto.getMaxMascotasPredeterminado() == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'max_mascotas_predeterminado' es obligatorio.");
        }
        if (reqDto.getMaxMascotasPredeterminado() < Constant.MIN_MASCOTAS_PERMITIDAS || reqDto.getMaxMascotasPredeterminado() > Constant.MAX_MASCOTAS_PERMITIDAS) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "La capacidad predeterminada de mascotas debe encontrarse entre "
                    + Constant.MIN_MASCOTAS_PERMITIDAS + " y " + Constant.MAX_MASCOTAS_PERMITIDAS + ".");
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
