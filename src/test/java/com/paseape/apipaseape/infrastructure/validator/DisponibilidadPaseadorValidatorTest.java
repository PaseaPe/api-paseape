package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarBloqueDisponibilidadReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarCapacidadBaseReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.DisponibilidadItemReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.GuardarDisponibilidadReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DisponibilidadPaseadorValidatorTest {

    private DisponibilidadPaseadorValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DisponibilidadPaseadorValidator();
    }

    @Test
    @DisplayName("validarGuardarDisponibilidad - Falla si request o bloques es nulo o vacío")
    void testGuardarDisponibilidad_NullOrEmpty() {
        ErrorDetailDto err1 = validator.validarGuardarDisponibilidad(null);
        assertNotNull(err1);
        assertEquals(StatusCodes.Code400, err1.getStatusCode());
        assertEquals(MessageCodes.ResponseCodeBR01, err1.getCode());

        ErrorDetailDto err2 = validator.validarGuardarDisponibilidad(GuardarDisponibilidadReqDto.builder().bloques(null).build());
        assertNotNull(err2);

        ErrorDetailDto err3 = validator.validarGuardarDisponibilidad(GuardarDisponibilidadReqDto.builder().bloques(List.of()).build());
        assertNotNull(err3);
    }

    @Test
    @DisplayName("validarGuardarDisponibilidad - Falla si diaSemanaId es inválido")
    void testGuardarDisponibilidad_InvalidDiaSemanaId() {
        var req = GuardarDisponibilidadReqDto.builder()
                .bloques(List.of(DisponibilidadItemReqDto.builder().diaSemanaId(0).bloqueHorarioId(1).build()))
                .build();
        ErrorDetailDto err = validator.validarGuardarDisponibilidad(req);
        assertNotNull(err);
        assertTrue(err.getMessage().contains("dia_semana_id"));

        var req2 = GuardarDisponibilidadReqDto.builder()
                .bloques(List.of(DisponibilidadItemReqDto.builder().diaSemanaId(8).bloqueHorarioId(1).build()))
                .build();
        ErrorDetailDto err2 = validator.validarGuardarDisponibilidad(req2);
        assertNotNull(err2);
    }

    @Test
    @DisplayName("validarGuardarDisponibilidad - Falla si bloqueHorarioId es inválido")
    void testGuardarDisponibilidad_InvalidBloqueHorarioId() {
        var req = GuardarDisponibilidadReqDto.builder()
                .bloques(List.of(DisponibilidadItemReqDto.builder().diaSemanaId(1).bloqueHorarioId(0).build()))
                .build();
        ErrorDetailDto err = validator.validarGuardarDisponibilidad(req);
        assertNotNull(err);
        assertTrue(err.getMessage().contains("bloque_horario_id"));
    }

    @Test
    @DisplayName("validarGuardarDisponibilidad - Falla si maxMascotas está fuera de rango [1..6]")
    void testGuardarDisponibilidad_InvalidMaxMascotas() {
        var req = GuardarDisponibilidadReqDto.builder()
                .bloques(List.of(DisponibilidadItemReqDto.builder().diaSemanaId(1).bloqueHorarioId(1).maxMascotas(0).build()))
                .build();
        ErrorDetailDto err = validator.validarGuardarDisponibilidad(req);
        assertNotNull(err);

        var req2 = GuardarDisponibilidadReqDto.builder()
                .bloques(List.of(DisponibilidadItemReqDto.builder().diaSemanaId(1).bloqueHorarioId(1).maxMascotas(7).build()))
                .build();
        ErrorDetailDto err2 = validator.validarGuardarDisponibilidad(req2);
        assertNotNull(err2);
    }

    @Test
    @DisplayName("validarGuardarDisponibilidad - Falla si hay bloques duplicados")
    void testGuardarDisponibilidad_DuplicateSlots() {
        var req = GuardarDisponibilidadReqDto.builder()
                .bloques(List.of(
                        DisponibilidadItemReqDto.builder().diaSemanaId(1).bloqueHorarioId(1).build(),
                        DisponibilidadItemReqDto.builder().diaSemanaId(1).bloqueHorarioId(1).build()
                ))
                .build();
        ErrorDetailDto err = validator.validarGuardarDisponibilidad(req);
        assertNotNull(err);
        assertTrue(err.getMessage().contains("duplicados"));
    }

    @Test
    @DisplayName("validarGuardarDisponibilidad - Éxito con datos correctos")
    void testGuardarDisponibilidad_Success() {
        var req = GuardarDisponibilidadReqDto.builder()
                .bloques(List.of(
                        DisponibilidadItemReqDto.builder().diaSemanaId(1).bloqueHorarioId(1).maxMascotas(3).build(),
                        DisponibilidadItemReqDto.builder().diaSemanaId(1).bloqueHorarioId(2).build(),
                        DisponibilidadItemReqDto.builder().diaSemanaId(7).bloqueHorarioId(17).maxMascotas(6).build()
                ))
                .build();
        ErrorDetailDto err = validator.validarGuardarDisponibilidad(req);
        assertNull(err);
    }

    @Test
    @DisplayName("validarActualizarBloque - Falla si request o campos obligatorios faltan")
    void testActualizarBloque_RequiredFields() {
        assertNotNull(validator.validarActualizarBloque(null));

        var req1 = ActualizarBloqueDisponibilidadReqDto.builder().diaSemanaId(null).bloqueHorarioId(1).accion(Constant.ACCION_BLOQUE_CANCELAR).build();
        assertNotNull(validator.validarActualizarBloque(req1));

        var req2 = ActualizarBloqueDisponibilidadReqDto.builder().diaSemanaId(1).bloqueHorarioId(null).accion(Constant.ACCION_BLOQUE_CANCELAR).build();
        assertNotNull(validator.validarActualizarBloque(req2));

        var req3 = ActualizarBloqueDisponibilidadReqDto.builder().diaSemanaId(1).bloqueHorarioId(1).accion("   ").build();
        assertNotNull(validator.validarActualizarBloque(req3));

        var req4 = ActualizarBloqueDisponibilidadReqDto.builder().diaSemanaId(1).bloqueHorarioId(1).accion("ACCION_INVALIDA").build();
        assertNotNull(validator.validarActualizarBloque(req4));
    }

    @Test
    @DisplayName("validarActualizarBloque - GUARDAR_CUPO requiere maxMascotas válido")
    void testActualizarBloque_GuardarCupo() {
        var req1 = ActualizarBloqueDisponibilidadReqDto.builder()
                .diaSemanaId(1).bloqueHorarioId(1).accion(Constant.ACCION_BLOQUE_GUARDAR_CUPO).maxMascotas(null)
                .build();
        assertNotNull(validator.validarActualizarBloque(req1));

        var req2 = ActualizarBloqueDisponibilidadReqDto.builder()
                .diaSemanaId(1).bloqueHorarioId(1).accion(Constant.ACCION_BLOQUE_GUARDAR_CUPO).maxMascotas(0)
                .build();
        assertNotNull(validator.validarActualizarBloque(req2));

        var req3 = ActualizarBloqueDisponibilidadReqDto.builder()
                .diaSemanaId(1).bloqueHorarioId(1).accion(Constant.ACCION_BLOQUE_GUARDAR_CUPO).maxMascotas(7)
                .build();
        assertNotNull(validator.validarActualizarBloque(req3));

        var reqOk = ActualizarBloqueDisponibilidadReqDto.builder()
                .diaSemanaId(1).bloqueHorarioId(1).accion(Constant.ACCION_BLOQUE_GUARDAR_CUPO).maxMascotas(4)
                .build();
        assertNull(validator.validarActualizarBloque(reqOk));
    }

    @Test
    @DisplayName("validarActualizarBloque - REESTABLECER y CANCELAR exitosos sin maxMascotas")
    void testActualizarBloque_ReestablecerYCancelar() {
        var reqReestablecer = ActualizarBloqueDisponibilidadReqDto.builder()
                .diaSemanaId(2).bloqueHorarioId(3).accion(Constant.ACCION_BLOQUE_REESTABLECER)
                .build();
        assertNull(validator.validarActualizarBloque(reqReestablecer));

        var reqCancelar = ActualizarBloqueDisponibilidadReqDto.builder()
                .diaSemanaId(2).bloqueHorarioId(3).accion(Constant.ACCION_BLOQUE_CANCELAR)
                .build();
        assertNull(validator.validarActualizarBloque(reqCancelar));
    }

    @Test
    @DisplayName("validarActualizarCapacidadBase - Validaciones de nulidad y rangos")
    void testActualizarCapacidadBase() {
        assertNotNull(validator.validarActualizarCapacidadBase(null));

        var req1 = ActualizarCapacidadBaseReqDto.builder().maxMascotasPredeterminado(null).build();
        assertNotNull(validator.validarActualizarCapacidadBase(req1));

        var req2 = ActualizarCapacidadBaseReqDto.builder().maxMascotasPredeterminado(0).build();
        assertNotNull(validator.validarActualizarCapacidadBase(req2));

        var req3 = ActualizarCapacidadBaseReqDto.builder().maxMascotasPredeterminado(7).build();
        assertNotNull(validator.validarActualizarCapacidadBase(req3));

        var reqOk = ActualizarCapacidadBaseReqDto.builder().maxMascotasPredeterminado(4).build();
        assertNull(validator.validarActualizarCapacidadBase(reqOk));
    }
}
