package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudInmediataReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
                .direccionRecojo("Av. Principal 123")
                .build();
        ErrorDetailDto error = validator.validarCrearSolicitudInmediata(req);
        assertNotNull(error);
        assertTrue(error.getMessage().contains("tipo_metodo_pago_id"));
    }

    @Test
    @DisplayName("validarCrearSolicitudInmediata - Falla si direccionRecojo está vacía")
    void testValidarCrearSolicitud_EmptyDireccion() {
        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(UUID.randomUUID().toString()))
                .distritoId(21)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
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
}
