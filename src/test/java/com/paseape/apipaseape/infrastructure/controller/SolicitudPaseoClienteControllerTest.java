package com.paseape.apipaseape.infrastructure.controller;

import com.paseape.apipaseape.application.service.SolicitudPaseoService;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudInmediataReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
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

        when(solicitudPaseoValidator.validarCrearSolicitudInmediata(req)).thenReturn(null);
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
    @DisplayName("crearSolicitudInmediata - Retorna ErrorDetailDto si el validador detecta errores sin llamar al servicio")
    void testCrearSolicitudInmediata_ValidationError() throws BadRequestException {
        var req = CrearSolicitudInmediataReqDto.builder().build();
        ErrorDetailDto valErr = ErrorDetailDto.builder()
                .statusCode(StatusCodes.Code400)
                .code(MessageCodes.ResponseCodeBR01)
                .message("La lista de mascotas es obligatoria")
                .build();

        when(solicitudPaseoValidator.validarCrearSolicitudInmediata(req)).thenReturn(valErr);

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

        when(solicitudPaseoValidator.validarCrearSolicitudInmediata(req)).thenReturn(null);
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
}
