package com.paseape.apipaseape.infrastructure.controller;

import com.paseape.apipaseape.application.service.SolicitudPaseoService;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudInmediataReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.HeaderDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudPaseoResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.security.JwtTokenProvider;
import com.paseape.apipaseape.infrastructure.shared.RequestService;
import com.paseape.apipaseape.infrastructure.validator.SolicitudPaseoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudPaseoClienteControllerTest {

    @Mock
    private SolicitudPaseoService solicitudPaseoService;
    @Mock
    private SolicitudPaseoValidator solicitudPaseoValidator;
    @Mock
    private RequestService requestService;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private SolicitudPaseoClienteController controller;

    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockPrincipal = () -> "cliente@paseape.com";
        when(requestService.getResponseHeader(any(Date.class), any(Date.class)))
                .thenReturn(HeaderDto.builder().build());
    }

    @Test
    @DisplayName("crearSolicitudInmediata - Éxito retorna HTTP 201 y ResponseDto")
    void testCrearSolicitudInmediata_Exito() throws BadRequestException {
        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(UUID.randomUUID().toString()))
                .distritoId(21)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Larco 100")
                .build();

        var resDto = SolicitudPaseoResDto.builder()
                .uuid(UUID.randomUUID().toString())
                .codigoReserva("PAS-INM-202610-1234")
                .build();

        when(solicitudPaseoValidator.validateCrearInmediata(req)).thenReturn(null);
        when(solicitudPaseoService.crearSolicitudInmediata("cliente@paseape.com", req)).thenReturn(resDto);

        ResponseEntity<ResponseDto<SolicitudPaseoResDto>> responseEntity = controller.crearSolicitudInmediata(
                mockPrincipal,
                null,
                req
        );

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code201, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        assertEquals(MessageCodes.ResponseCodeS00, responseEntity.getBody().getCode());
        assertEquals(Constant.MSJ_SOLICITUD_CREADA_EXITO, responseEntity.getBody().getMessage());
        assertEquals("PAS-INM-202610-1234", responseEntity.getBody().getResponse().getCodigoReserva());

        verify(solicitudPaseoService, times(1)).crearSolicitudInmediata("cliente@paseape.com", req);
    }

    @Test
    @DisplayName("crearSolicitudInmediata - Retorna ResponseDto de error si el validador detecta errores sin llamar al servicio")
    void testCrearSolicitudInmediata_ValidationError() throws BadRequestException {
        var req = CrearSolicitudInmediataReqDto.builder().build();
        var valErr = new ResponseDto<>();
        valErr.setStatusCode(StatusCodes.Code400);
        valErr.setCode(MessageCodes.ResponseCodeBR01);
        valErr.setMessage("La lista de mascotas es obligatoria");

        when(solicitudPaseoValidator.validateCrearInmediata(req)).thenReturn(valErr);

        ResponseEntity<ResponseDto<SolicitudPaseoResDto>> responseEntity = controller.crearSolicitudInmediata(
                mockPrincipal,
                null,
                req
        );

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code400, responseEntity.getStatusCode().value());
        assertEquals(MessageCodes.ResponseCodeBR01, responseEntity.getBody().getCode());
        assertEquals("La lista de mascotas es obligatoria", responseEntity.getBody().getMessage());

        verify(solicitudPaseoService, never()).crearSolicitudInmediata(any(), any());
    }

    @Test
    @DisplayName("crearSolicitudInmediata - Maneja BadRequestException del servicio")
    void testCrearSolicitudInmediata_ServiceBadRequest() throws BadRequestException {
        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(UUID.randomUUID().toString()))
                .build();

        when(solicitudPaseoValidator.validateCrearInmediata(req)).thenReturn(null);
        when(solicitudPaseoService.crearSolicitudInmediata("cliente@paseape.com", req))
                .thenThrow(new BadRequestException(MessageCodes.ResponseCodeBR03, "La mascota no pertenece al cliente autenticado."));

        ResponseEntity<ResponseDto<SolicitudPaseoResDto>> responseEntity = controller.crearSolicitudInmediata(
                mockPrincipal,
                null,
                req
        );

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code400, responseEntity.getStatusCode().value());
        assertEquals(MessageCodes.ResponseCodeBR03, responseEntity.getBody().getCode());
        assertTrue(responseEntity.getBody().getMessage().contains("no pertenece al cliente"));
    }

    @Test
    @DisplayName("crearSolicitudProgramada - Éxito retorna HTTP 201 y ResponseDto")
    void testCrearSolicitudProgramada_Exito() throws BadRequestException {
        var req = com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudProgramadaReqDto.builder()
                .paseadorUuid(UUID.randomUUID().toString())
                .mascotaUuids(List.of(UUID.randomUUID().toString()))
                .distritoId(21)
                .fechaProgramada(java.time.LocalDate.now().plusDays(2))
                .bloqueHorarioId(4)
                .tipoDuracionPaseoId(2)
                .tipoMetodoPagoId(1)
                .direccionRecojo("Av. Pardo 555")
                .build();

        var resDto = SolicitudPaseoResDto.builder()
                .uuid(UUID.randomUUID().toString())
                .codigoReserva("PAS-PROG-202610-ABCD")
                .build();

        when(solicitudPaseoValidator.validateCrearProgramada(req)).thenReturn(null);
        when(solicitudPaseoService.crearSolicitudProgramada("cliente@paseape.com", req)).thenReturn(resDto);

        ResponseEntity<ResponseDto<SolicitudPaseoResDto>> responseEntity = controller.crearSolicitudProgramada(
                mockPrincipal,
                null,
                req
        );

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code201, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        assertEquals(MessageCodes.ResponseCodeS00, responseEntity.getBody().getCode());
        assertEquals(Constant.MSJ_SOLICITUD_PROGRAMADA_CREADA_EXITO, responseEntity.getBody().getMessage());
        assertEquals("PAS-PROG-202610-ABCD", responseEntity.getBody().getResponse().getCodigoReserva());

        verify(solicitudPaseoService, times(1)).crearSolicitudProgramada("cliente@paseape.com", req);
    }

    @Test
    @DisplayName("crearSolicitudProgramada - Retorna ResponseDto de error si el validador detecta errores sin llamar al servicio")
    void testCrearSolicitudProgramada_ValidationError() throws BadRequestException {
        var req = com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudProgramadaReqDto.builder().build();
        var valErr = new ResponseDto<>();
        valErr.setStatusCode(StatusCodes.Code400);
        valErr.setCode(MessageCodes.ResponseCodeBR01);
        valErr.setMessage("El campo 'paseador_uuid' es obligatorio");

        when(solicitudPaseoValidator.validateCrearProgramada(req)).thenReturn(valErr);

        ResponseEntity<ResponseDto<SolicitudPaseoResDto>> responseEntity = controller.crearSolicitudProgramada(
                mockPrincipal,
                null,
                req
        );

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code400, responseEntity.getStatusCode().value());
        assertEquals(MessageCodes.ResponseCodeBR01, responseEntity.getBody().getCode());
        assertEquals("El campo 'paseador_uuid' es obligatorio", responseEntity.getBody().getMessage());

        verify(solicitudPaseoService, never()).crearSolicitudProgramada(any(), any());
    }

    @Test
    @DisplayName("crearSolicitudProgramada - Maneja BadRequestException del servicio")
    void testCrearSolicitudProgramada_ServiceBadRequest() throws BadRequestException {
        var req = com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudProgramadaReqDto.builder()
                .paseadorUuid(UUID.randomUUID().toString())
                .mascotaUuids(List.of(UUID.randomUUID().toString()))
                .build();

        when(solicitudPaseoValidator.validateCrearProgramada(req)).thenReturn(null);
        when(solicitudPaseoService.crearSolicitudProgramada("cliente@paseape.com", req))
                .thenThrow(new BadRequestException(MessageCodes.ResponseCodeBR03, "El bloque horario no cuenta con cupo suficiente."));

        ResponseEntity<ResponseDto<SolicitudPaseoResDto>> responseEntity = controller.crearSolicitudProgramada(
                mockPrincipal,
                null,
                req
        );

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code400, responseEntity.getStatusCode().value());
        assertEquals(MessageCodes.ResponseCodeBR03, responseEntity.getBody().getCode());
        assertTrue(responseEntity.getBody().getMessage().contains("no cuenta con cupo suficiente"));
    }

    @Test
    @DisplayName("obtenerUbicacionActual - Éxito con coordenadas en vivo retorna 200")
    void testObtenerUbicacionActual_Exito() throws BadRequestException {
        String uuid = UUID.randomUUID().toString();
        var ubicacionDto = com.paseape.apipaseape.infrastructure.dto.response.UbicacionEnVivoResDto.builder()
                .uuid(UUID.randomUUID().toString())
                .solicitudPaseoUuid(uuid)
                .latitud(BigDecimal.valueOf(-12.1025))
                .longitud(BigDecimal.valueOf(-76.9880))
                .senalDegradada(false)
                .build();

        when(solicitudPaseoValidator.validateObtenerUbicacionEnVivo("cliente@paseape.com", uuid)).thenReturn(null);
        when(solicitudPaseoService.obtenerUbicacionEnVivo(uuid)).thenReturn(ubicacionDto);

        ResponseEntity<ResponseDto<com.paseape.apipaseape.infrastructure.dto.response.UbicacionEnVivoResDto>> responseEntity = controller.obtenerUbicacionActual(
                mockPrincipal,
                null,
                uuid
        );

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code200, responseEntity.getStatusCode().value());
        assertEquals(MessageCodes.ResponseCodeS00, responseEntity.getBody().getCode());
        assertEquals(Constant.MSJ_UBICACION_EN_VIVO_OBTENIDA, responseEntity.getBody().getMessage());
        assertNotNull(responseEntity.getBody().getResponse());
        assertEquals(BigDecimal.valueOf(-12.1025), responseEntity.getBody().getResponse().getLatitud());
    }

    @Test
    @DisplayName("obtenerUbicacionActual - Espera de primera señal GPS retorna 200 con response null")
    void testObtenerUbicacionActual_EsperaPrimeraSenal() throws BadRequestException {
        String uuid = UUID.randomUUID().toString();

        when(solicitudPaseoValidator.validateObtenerUbicacionEnVivo("cliente@paseape.com", uuid)).thenReturn(null);
        when(solicitudPaseoService.obtenerUbicacionEnVivo(uuid)).thenReturn(null);

        ResponseEntity<ResponseDto<com.paseape.apipaseape.infrastructure.dto.response.UbicacionEnVivoResDto>> responseEntity = controller.obtenerUbicacionActual(
                mockPrincipal,
                null,
                uuid
        );

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code200, responseEntity.getStatusCode().value());
        assertEquals(MessageCodes.ResponseCodeS00, responseEntity.getBody().getCode());
        assertEquals(Constant.MSJ_ESPERA_PRIMERA_SENAL_GPS, responseEntity.getBody().getMessage());
        assertNull(responseEntity.getBody().getResponse());
    }
}
