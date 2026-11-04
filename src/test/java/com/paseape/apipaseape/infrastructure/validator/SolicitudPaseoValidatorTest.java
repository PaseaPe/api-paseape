package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudInmediataReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SolicitudPaseoValidatorTest {

    private SolicitudPaseoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SolicitudPaseoValidator();
    }

    @Test
    @DisplayName("validarCrearSolicitudInmediata - Falla si el cuerpo es nulo")
    void testValidarCrearSolicitud_NullBody() {
        ErrorDetailDto error = validator.validarCrearSolicitudInmediata(null);
        assertNotNull(error);
        assertEquals(StatusCodes.Code400, error.getStatusCode());
        assertEquals(MessageCodes.ResponseCodeBR01, error.getCode());
        assertTrue(error.getMessage().contains("no puede estar vacío"));
    }

    @Test
    @DisplayName("validarCrearSolicitudInmediata - Falla si lista de mascotas es nula o vacía")
    void testValidarCrearSolicitud_EmptyMascotas() {
        var req1 = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(null)
                .distritoId(21)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Principal 123")
                .build();
        ErrorDetailDto err1 = validator.validarCrearSolicitudInmediata(req1);
        assertNotNull(err1);
        assertTrue(err1.getMessage().contains("mascotas"));

        var req2 = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of())
                .distritoId(21)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Principal 123")
                .build();
        ErrorDetailDto err2 = validator.validarCrearSolicitudInmediata(req2);
        assertNotNull(err2);
    }

    @Test
    @DisplayName("validarCrearSolicitudInmediata - Falla si UUID de mascota tiene formato inválido")
    void testValidarCrearSolicitud_InvalidMascotaUuid() {
        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of("invalid-uuid-format"))
                .distritoId(21)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Principal 123")
                .build();
        ErrorDetailDto error = validator.validarCrearSolicitudInmediata(req);
        assertNotNull(error);
        assertTrue(error.getMessage().contains("no tiene un formato UUID válido"));
    }

    @Test
    @DisplayName("validarCrearSolicitudInmediata - Falla si hay mascotas duplicadas")
    void testValidarCrearSolicitud_DuplicateMascotas() {
        String uuid = UUID.randomUUID().toString();
        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(uuid, uuid))
                .distritoId(21)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Principal 123")
                .build();
        ErrorDetailDto error = validator.validarCrearSolicitudInmediata(req);
        assertNotNull(error);
        assertTrue(error.getMessage().contains("duplicados"));
    }

    @Test
    @DisplayName("validarCrearSolicitudInmediata - Falla si distritoId es inválido")
    void testValidarCrearSolicitud_InvalidDistritoId() {
        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(UUID.randomUUID().toString()))
                .distritoId(0)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Principal 123")
                .build();
        ErrorDetailDto error = validator.validarCrearSolicitudInmediata(req);
        assertNotNull(error);
        assertTrue(error.getMessage().contains("distrito_id"));
    }

    @Test
    @DisplayName("validarCrearSolicitudInmediata - Falla si tipoDuracionPaseoId es inválido")
    void testValidarCrearSolicitud_InvalidTipoDuracionPaseoId() {
        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(UUID.randomUUID().toString()))
                .distritoId(21)
                .tipoDuracionPaseoId(null)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Principal 123")
                .build();
        ErrorDetailDto error = validator.validarCrearSolicitudInmediata(req);
        assertNotNull(error);
        assertTrue(error.getMessage().contains("tipo_duracion_paseo_id"));
    }

    @Test
    @DisplayName("validarCrearSolicitudInmediata - Falla si tipoMetodoPagoId es inválido")
    void testValidarCrearSolicitud_InvalidTipoMetodoPagoId() {
        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(UUID.randomUUID().toString()))
                .distritoId(21)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(-1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Principal 123")
                .build();
        ErrorDetailDto error = validator.validarCrearSolicitudInmediata(req);
        assertNotNull(error);
        assertTrue(error.getMessage().contains("tipo_metodo_pago_id"));
    }

    @Test
    @DisplayName("validarCrearSolicitudInmediata - Falla si tarifaHoraPen es nula o menor o igual a cero")
    void testValidarCrearSolicitud_InvalidTarifaHoraPen() {
        var req1 = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(UUID.randomUUID().toString()))
                .distritoId(21)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(null)
                .direccionRecojo("Av. Principal 123")
                .build();
        ErrorDetailDto err1 = validator.validarCrearSolicitudInmediata(req1);
        assertNotNull(err1);
        assertTrue(err1.getMessage().contains("tarifa_hora_pen"));

        var req2 = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(UUID.randomUUID().toString()))
                .distritoId(21)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(BigDecimal.ZERO)
                .direccionRecojo("Av. Principal 123")
                .build();
        ErrorDetailDto err2 = validator.validarCrearSolicitudInmediata(req2);
        assertNotNull(err2);
        assertTrue(err2.getMessage().contains("tarifa_hora_pen"));

        var req3 = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(UUID.randomUUID().toString()))
                .distritoId(21)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("-10.00"))
                .direccionRecojo("Av. Principal 123")
                .build();
        ErrorDetailDto err3 = validator.validarCrearSolicitudInmediata(req3);
        assertNotNull(err3);
        assertTrue(err3.getMessage().contains("tarifa_hora_pen"));
    }

    @Test
    @DisplayName("validarCrearSolicitudInmediata - Falla si direccionRecojo está vacía")
    void testValidarCrearSolicitud_EmptyDireccion() {
        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(UUID.randomUUID().toString()))
                .distritoId(21)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("   ")
                .build();
        ErrorDetailDto error = validator.validarCrearSolicitudInmediata(req);
        assertNotNull(error);
        assertTrue(error.getMessage().contains("direccion_recojo"));
    }

    @Test
    @DisplayName("validarCrearSolicitudInmediata - Éxito con datos válidos")
    void testValidarCrearSolicitud_Success() {
        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                .distritoId(21)
                .tipoDuracionPaseoId(2)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Calle Los Álamos 456")
                .referenciaDireccion("Frente al parque")
                .build();
        ErrorDetailDto error = validator.validarCrearSolicitudInmediata(req);
        assertNull(error);
    }

    @Test
    @DisplayName("validarUuid - Falla si uuid es nulo o formato inválido")
    void testValidarUuid() {
        assertNotNull(validator.validarUuid(null, "uuid"));
        assertNotNull(validator.validarUuid("   ", "uuid"));
        assertNotNull(validator.validarUuid("abc-123", "uuid"));
        assertNull(validator.validarUuid(UUID.randomUUID().toString(), "uuid"));
    }

    @Test
    @DisplayName("validarCrearSolicitudProgramada - Falla si el cuerpo es nulo")
    void testValidarCrearSolicitudProgramada_NullBody() {
        ErrorDetailDto error = validator.validarCrearSolicitudProgramada(null);
        assertNotNull(error);
        assertEquals(StatusCodes.Code400, error.getStatusCode());
        assertEquals(MessageCodes.ResponseCodeBR01, error.getCode());
        assertTrue(error.getMessage().contains("no puede estar vacío"));
    }

    @Test
    @DisplayName("validarCrearSolicitudProgramada - Falla si paseadorUuid es nulo o formato inválido")
    void testValidarCrearSolicitudProgramada_InvalidPaseadorUuid() {
        var req1 = com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudProgramadaReqDto.builder()
                .paseadorUuid(null)
                .build();
        ErrorDetailDto err1 = validator.validarCrearSolicitudProgramada(req1);
        assertNotNull(err1);
        assertTrue(err1.getMessage().contains("paseador_uuid"));

        var req2 = com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudProgramadaReqDto.builder()
                .paseadorUuid("invalid-format")
                .build();
        ErrorDetailDto err2 = validator.validarCrearSolicitudProgramada(req2);
        assertNotNull(err2);
        assertTrue(err2.getMessage().contains("formato UUID"));
    }

    @Test
    @DisplayName("validarCrearSolicitudProgramada - Falla si mascotaUuids es nula o vacía o tiene duplicados")
    void testValidarCrearSolicitudProgramada_InvalidMascotas() {
        String paseadorUuid = UUID.randomUUID().toString();
        var req1 = com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudProgramadaReqDto.builder()
                .paseadorUuid(paseadorUuid)
                .mascotaUuids(null)
                .build();
        ErrorDetailDto err1 = validator.validarCrearSolicitudProgramada(req1);
        assertNotNull(err1);
        assertTrue(err1.getMessage().contains("mascotas"));

        String mUuid = UUID.randomUUID().toString();
        var req2 = com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudProgramadaReqDto.builder()
                .paseadorUuid(paseadorUuid)
                .mascotaUuids(List.of(mUuid, mUuid))
                .build();
        ErrorDetailDto err2 = validator.validarCrearSolicitudProgramada(req2);
        assertNotNull(err2);
        assertTrue(err2.getMessage().contains("duplicados"));
    }

    @Test
    @DisplayName("validarCrearSolicitudProgramada - Falla si distritoId o fechaProgramada o bloqueHorario son inválidos")
    void testValidarCrearSolicitudProgramada_InvalidDistritoFechaBloque() {
        String paseadorUuid = UUID.randomUUID().toString();
        String mUuid = UUID.randomUUID().toString();

        var reqDistrito = com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudProgramadaReqDto.builder()
                .paseadorUuid(paseadorUuid)
                .mascotaUuids(List.of(mUuid))
                .distritoId(0)
                .build();
        assertNotNull(validator.validarCrearSolicitudProgramada(reqDistrito));

        var reqFecha = com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudProgramadaReqDto.builder()
                .paseadorUuid(paseadorUuid)
                .mascotaUuids(List.of(mUuid))
                .distritoId(21)
                .fechaProgramada(null)
                .build();
        assertNotNull(validator.validarCrearSolicitudProgramada(reqFecha));

        var reqBloque = com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudProgramadaReqDto.builder()
                .paseadorUuid(paseadorUuid)
                .mascotaUuids(List.of(mUuid))
                .distritoId(21)
                .fechaProgramada(java.time.LocalDate.now().plusDays(2))
                .bloqueHorarioId(-1)
                .build();
        assertNotNull(validator.validarCrearSolicitudProgramada(reqBloque));
    }

    @Test
    @DisplayName("validarCrearSolicitudProgramada - Éxito con todos los campos válidos")
    void testValidarCrearSolicitudProgramada_Success() {
        var req = com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudProgramadaReqDto.builder()
                .paseadorUuid(UUID.randomUUID().toString())
                .mascotaUuids(List.of(UUID.randomUUID().toString()))
                .distritoId(21)
                .fechaProgramada(java.time.LocalDate.now().plusDays(3))
                .bloqueHorarioId(5)
                .tipoDuracionPaseoId(2)
                .tipoMetodoPagoId(1)
                .direccionRecojo("Av. Angamos 1234")
                .referenciaDireccion("Cerca al parque")
                .build();
        ErrorDetailDto error = validator.validarCrearSolicitudProgramada(req);
        assertNull(error);
    }
}
