package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.application.repository.IDistritoLimaRepository;
import com.paseape.apipaseape.domain.entity.DistritoLima;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CancelarSolicitudReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudInmediataReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudProgramadaReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.RegistrarUbicacionesBatchReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.UbicacionPuntoReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudPaseoValidatorTest {

    @Mock
    private IDistritoLimaRepository distritoLimaRepository;

    private SolicitudPaseoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SolicitudPaseoValidator(distritoLimaRepository);
    }

    @Test
    @DisplayName("validateCrearInmediata - Falla si el cuerpo es nulo")
    void testValidateCrearInmediata_NullBody() {
        ResponseDto<?> error = validator.validateCrearInmediata(null);
        assertNotNull(error);
        assertEquals(StatusCodes.Code400, error.getStatusCode());
        assertEquals(MessageCodes.ResponseCodeBR01, error.getCode());
        assertTrue(error.getMessage().contains("no puede estar vacío"));
    }

    @Test
    @DisplayName("validateCrearInmediata - Falla si lista de mascotas es nula o vacía")
    void testValidateCrearInmediata_EmptyMascotas() {
        var req1 = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(null)
                .distritoId(21)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Principal 123")
                .build();
        ResponseDto<?> err1 = validator.validateCrearInmediata(req1);
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
        ResponseDto<?> err2 = validator.validateCrearInmediata(req2);
        assertNotNull(err2);
    }

    @Test
    @DisplayName("validateCrearInmediata - Falla si UUID de mascota tiene formato inválido")
    void testValidateCrearInmediata_InvalidMascotaUuid() {
        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of("invalid-uuid-format"))
                .distritoId(21)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Principal 123")
                .build();
        ResponseDto<?> error = validator.validateCrearInmediata(req);
        assertNotNull(error);
        assertTrue(error.getMessage().contains("no tiene un formato UUID válido"));
    }

    @Test
    @DisplayName("validateCrearInmediata - Falla si hay mascotas duplicadas")
    void testValidateCrearInmediata_DuplicateMascotas() {
        String uuid = UUID.randomUUID().toString();
        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(uuid, uuid))
                .distritoId(21)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Principal 123")
                .build();
        ResponseDto<?> error = validator.validateCrearInmediata(req);
        assertNotNull(error);
        assertTrue(error.getMessage().contains("duplicados"));
    }

    @Test
    @DisplayName("validateCrearInmediata - Falla si distritoId es inválido o no existe")
    void testValidateCrearInmediata_InvalidDistritoId() {
        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(UUID.randomUUID().toString()))
                .distritoId(0)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Principal 123")
                .build();
        ResponseDto<?> error = validator.validateCrearInmediata(req);
        assertNotNull(error);
        assertTrue(error.getMessage().contains("distrito"));

        var req2 = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(UUID.randomUUID().toString()))
                .distritoId(999)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Principal 123")
                .build();
        when(distritoLimaRepository.findById(999)).thenReturn(null);
        ResponseDto<?> error2 = validator.validateCrearInmediata(req2);
        assertNotNull(error2);
        assertTrue(error2.getMessage().contains("distrito"));
    }

    @Test
    @DisplayName("validateCrearInmediata - Falla si tarifaHoraPen es inválida")
    void testValidateCrearInmediata_InvalidTarifaHoraPen() {
        var req1 = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(UUID.randomUUID().toString()))
                .distritoId(21)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(null)
                .direccionRecojo("Av. Principal 123")
                .build();
        ResponseDto<?> err1 = validator.validateCrearInmediata(req1);
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
        ResponseDto<?> err2 = validator.validateCrearInmediata(req2);
        assertNotNull(err2);
        assertTrue(err2.getMessage().contains("tarifa_hora_pen"));
    }

    @Test
    @DisplayName("validateCrearInmediata - Éxito con datos válidos")
    void testValidateCrearInmediata_Success() {
        when(distritoLimaRepository.findById(21)).thenReturn(DistritoLima.builder().id(21).build());

        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                .distritoId(21)
                .tipoDuracionPaseoId(2)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Calle Los Álamos 456")
                .referenciaDireccion("Frente al parque")
                .build();
        ResponseDto<?> error = validator.validateCrearInmediata(req);
        assertNull(error);
    }

    @Test
    @DisplayName("validateCrearProgramada - Éxito con todos los campos válidos")
    void testValidateCrearProgramada_Success() {
        when(distritoLimaRepository.findById(21)).thenReturn(DistritoLima.builder().id(21).build());

        var req = CrearSolicitudProgramadaReqDto.builder()
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
        ResponseDto<?> error = validator.validateCrearProgramada(req);
        assertNull(error);
    }

    @Test
    @DisplayName("validateUuid - Falla si uuid es nulo o formato inválido")
    void testValidateUuid() {
        assertNotNull(validator.validateUuid(null, "uuid"));
        assertNotNull(validator.validateUuid("   ", "uuid"));
        assertNotNull(validator.validateUuid("abc-123", "uuid"));
        assertNull(validator.validateUuid(UUID.randomUUID().toString(), "uuid"));
    }

    @Test
    @DisplayName("validateRegistrarUbicaciones - Valida lotes GPS y autenticación correctamente")
    void testValidateRegistrarUbicaciones() {
        String uuid = UUID.randomUUID().toString();

        // 1. Email nulo o vacío
        assertNotNull(validator.validateRegistrarUbicaciones(null, uuid, null));
        assertNotNull(validator.validateRegistrarUbicaciones("   ", uuid, null));

        // 2. Lote nulo
        assertNotNull(validator.validateRegistrarUbicaciones("paseador@paseape.com", uuid, null));

        // 3. Lote vacío
        assertNotNull(validator.validateRegistrarUbicaciones("paseador@paseape.com", uuid, RegistrarUbicacionesBatchReqDto.builder().ubicaciones(List.of()).build()));

        // 4. Punto válido
        var p = UbicacionPuntoReqDto.builder()
                .latitud(BigDecimal.valueOf(-12.046374))
                .longitud(BigDecimal.valueOf(-77.042793))
                .fechaHoraGps(LocalDateTime.now(ZoneOffset.UTC))
                .precisionM(BigDecimal.valueOf(5.0))
                .velocidadMs(BigDecimal.valueOf(1.2))
                .rumboGrados(BigDecimal.valueOf(180.0))
                .build();
        var reqValido = RegistrarUbicacionesBatchReqDto.builder().ubicaciones(List.of(p)).build();
        assertNull(validator.validateRegistrarUbicaciones("paseador@paseape.com", uuid, reqValido));
    }
}
