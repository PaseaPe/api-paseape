package com.paseape.apipaseape.infrastructure.controller;

import com.paseape.apipaseape.application.service.SolicitudPaseoService;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
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

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudPaseoPaseadorControllerTest {

    @Mock
    private SolicitudPaseoService solicitudPaseoService;
    @Mock
    private SolicitudPaseoValidator solicitudPaseoValidator;
    @Mock
    private RequestService requestService;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private SolicitudPaseoPaseadorController controller;

    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockPrincipal = () -> "paseador@paseape.com";
        when(requestService.getResponseHeader(any(Date.class), any(Date.class)))
                .thenReturn(HeaderDto.builder().build());
    }

    @Test
    @DisplayName("obtenerSolicitudesDisponibles - Éxito retorna HTTP 200 y lista")
    void testObtenerSolicitudesDisponibles_Exito() throws BadRequestException {
        var sp1 = SolicitudPaseoResDto.builder()
                .uuid(UUID.randomUUID().toString())
                .codigoReserva("PAS-INM-202610-0001")
                .build();

        when(solicitudPaseoService.obtenerSolicitudesInmediatasDisponibles("paseador@paseape.com"))
                .thenReturn(List.of(sp1));

        ResponseEntity<ResponseDto<List<SolicitudPaseoResDto>>> responseEntity = controller.obtenerSolicitudesDisponibles(
                mockPrincipal,
                null
        );

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code200, responseEntity.getStatusCode().value());
        assertEquals(MessageCodes.ResponseCodeS00, responseEntity.getBody().getCode());
        assertEquals(Constant.MSJ_SOLICITUDES_DISPONIBLES_OBTENIDAS, responseEntity.getBody().getMessage());
        assertEquals(1, responseEntity.getBody().getResponse().size());
    }

    @Test
    @DisplayName("tomarSolicitudInmediata - Éxito retorna HTTP 200 y solicitud actualizada")
    void testTomarSolicitudInmediata_Exito() throws BadRequestException {
        String uuid = UUID.randomUUID().toString();
        var resDto = SolicitudPaseoResDto.builder()
                .uuid(uuid)
                .codigoReserva("PAS-INM-202610-0002")
                .estadoId(Constant.ID_ESTADO_SOLICITUD_ACEPTADA)
                .build();

        when(solicitudPaseoValidator.validarUuid(uuid, "uuid")).thenReturn(null);
        when(solicitudPaseoService.tomarSolicitudInmediata("paseador@paseape.com", uuid)).thenReturn(resDto);

        ResponseEntity<ResponseDto<SolicitudPaseoResDto>> responseEntity = controller.tomarSolicitudInmediata(
                mockPrincipal,
                null,
                uuid
        );

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code200, responseEntity.getStatusCode().value());
        assertEquals(MessageCodes.ResponseCodeS00, responseEntity.getBody().getCode());
        assertEquals(Constant.MSJ_SOLICITUD_TOMADA_EXITO, responseEntity.getBody().getMessage());
        assertEquals(Constant.ID_ESTADO_SOLICITUD_ACEPTADA, responseEntity.getBody().getResponse().getEstadoId());
    }

    @Test
    @DisplayName("tomarSolicitudInmediata - Falla de validación de UUID retorna 400 sin invocar servicio")
    void testTomarSolicitudInmediata_InvalidUuid() throws BadRequestException {
        String invalidUuid = "not-a-valid-uuid";
        ErrorDetailDto valErr = ErrorDetailDto.builder()
                .statusCode(StatusCodes.Code400)
                .code(MessageCodes.ResponseCodeBR01)
                .message("El formato del identificador uuid no es válido.")
                .build();

        when(solicitudPaseoValidator.validarUuid(invalidUuid, "uuid")).thenReturn(valErr);

        ResponseEntity<ResponseDto<SolicitudPaseoResDto>> responseEntity = controller.tomarSolicitudInmediata(
                mockPrincipal,
                null,
                invalidUuid
        );

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code400, responseEntity.getStatusCode().value());
        assertEquals(MessageCodes.ResponseCodeBR01, responseEntity.getBody().getCode());
        assertTrue(responseEntity.getBody().getMessage().contains("formato"));

        verify(solicitudPaseoService, never()).tomarSolicitudInmediata(any(), any());
    }

    @Test
    @DisplayName("tomarSolicitudInmediata - Manejo de error cuando solicitud ya fue tomada (Race condition)")
    void testTomarSolicitudInmediata_AlreadyTaken() throws BadRequestException {
        String uuid = UUID.randomUUID().toString();

        when(solicitudPaseoValidator.validarUuid(uuid, "uuid")).thenReturn(null);
        when(solicitudPaseoService.tomarSolicitudInmediata("paseador@paseape.com", uuid))
                .thenThrow(new BadRequestException(MessageCodes.ResponseCodeBR03, Constant.MSJ_ERROR_SOLICITUD_NO_DISPONIBLE));

        ResponseEntity<ResponseDto<SolicitudPaseoResDto>> responseEntity = controller.tomarSolicitudInmediata(
                mockPrincipal,
                null,
                uuid
        );

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code400, responseEntity.getStatusCode().value());
        assertEquals(MessageCodes.ResponseCodeBR03, responseEntity.getBody().getCode());
        assertEquals(Constant.MSJ_ERROR_SOLICITUD_NO_DISPONIBLE, responseEntity.getBody().getMessage());
    }
}
