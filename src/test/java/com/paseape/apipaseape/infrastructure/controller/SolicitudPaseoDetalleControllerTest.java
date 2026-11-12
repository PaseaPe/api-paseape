package com.paseape.apipaseape.infrastructure.controller;

import com.paseape.apipaseape.application.service.SolicitudPaseoDetalleService;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.response.HeaderDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudPaseadorDetalleResDto;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudPaseoDetalleResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.security.JwtTokenProvider;
import com.paseape.apipaseape.infrastructure.shared.RequestService;
import com.paseape.apipaseape.infrastructure.validator.SolicitudPaseoDetalleValidator;
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
class SolicitudPaseoDetalleControllerTest {

    @Mock
    private SolicitudPaseoDetalleService solicitudPaseoDetalleService;
    @Mock
    private SolicitudPaseoDetalleValidator solicitudPaseoDetalleValidator;
    @Mock
    private RequestService requestService;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private SolicitudPaseoDetalleController controller;

    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockPrincipal = () -> "cliente@paseape.com";
        when(requestService.getResponseHeader(any(Date.class), any(Date.class)))
                .thenReturn(HeaderDto.builder().build());
    }

    @Test
    @DisplayName("obtenerDetalleSolicitud - Éxito retorna HTTP 200 y detalle completo")
    void testObtenerDetalleSolicitud_Exito() throws BadRequestException {
        String uuid = UUID.randomUUID().toString();
        var detalleDto = SolicitudPaseoDetalleResDto.builder()
                .uuid(uuid)
                .codigoReserva("PAS-PROG-202611-0001")
                .paseador(SolicitudPaseadorDetalleResDto.builder()
                        .nombreCompleto("Carlos Mendoza")
                        .telefono("987654321")
                        .build())
                .mascotas(List.of())
                .build();

        when(solicitudPaseoDetalleValidator.validateObtenerDetalle("cliente@paseape.com", uuid)).thenReturn(null);
        when(solicitudPaseoDetalleService.obtenerDetalle(uuid)).thenReturn(detalleDto);

        ResponseEntity<ResponseDto<SolicitudPaseoDetalleResDto>> responseEntity = controller.obtenerDetalleSolicitud(
                mockPrincipal,
                null,
                uuid
        );

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        assertEquals(MessageCodes.ResponseCodeS00, responseEntity.getBody().getCode());
        assertEquals(Constant.MSJ_SOLICITUD_DETALLE_OBTENIDO, responseEntity.getBody().getMessage());
        assertEquals("987654321", responseEntity.getBody().getResponse().getPaseador().getTelefono());

        verify(solicitudPaseoDetalleService, times(1)).obtenerDetalle(uuid);
    }

    @Test
    @DisplayName("obtenerDetalleSolicitud - Falla de validación retorna HTTP 400 sin invocar al servicio")
    void testObtenerDetalleSolicitud_ValidationError() throws BadRequestException {
        String uuid = "uuid-invalido";
        var valErr = new ResponseDto<>();
        valErr.setStatusCode(StatusCodes.Code400);
        valErr.setCode(MessageCodes.ResponseCodeBR01);
        valErr.setMessage("El identificador de solicitud de paseo no tiene un formato UUID válido");

        when(solicitudPaseoDetalleValidator.validateObtenerDetalle("cliente@paseape.com", uuid)).thenReturn(valErr);

        ResponseEntity<ResponseDto<SolicitudPaseoDetalleResDto>> responseEntity = controller.obtenerDetalleSolicitud(
                mockPrincipal,
                null,
                uuid
        );

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code400, responseEntity.getStatusCode().value());
        assertEquals(MessageCodes.ResponseCodeBR01, responseEntity.getBody().getCode());

        verify(solicitudPaseoDetalleService, never()).obtenerDetalle(any());
    }
}
