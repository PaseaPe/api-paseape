package com.paseape.apipaseape.infrastructure.controller;

import com.paseape.apipaseape.application.service.SolicitudPaseoService;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.response.HeaderDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudPaseoRecorridoResDto;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudPaseoRecorridoControllerTest {

    @Mock
    private SolicitudPaseoService solicitudPaseoService;
    @Mock
    private SolicitudPaseoValidator solicitudPaseoValidator;
    @Mock
    private RequestService requestService;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private SolicitudPaseoRecorridoController controller;

    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockPrincipal = () -> "usuario@paseape.com";
        when(requestService.getResponseHeader(any(Date.class), any(Date.class)))
                .thenReturn(HeaderDto.builder().build());
    }

    @Test
    @DisplayName("obtenerRecorridoConsolidado - Éxito retorna HTTP 200 y SolicitudPaseoRecorridoResDto")
    void testObtenerRecorridoConsolidado_Exito() throws BadRequestException {
        String uuid = UUID.randomUUID().toString();
        var resDto = SolicitudPaseoRecorridoResDto.builder()
                .uuid(UUID.randomUUID().toString())
                .solicitudPaseoUuid(uuid)
                .distanciaMetros(BigDecimal.valueOf(2500.50))
                .duracionSegundos(3600)
                .velocidadPromedioKmh(BigDecimal.valueOf(2.50))
                .coordenadasCodificadas("_p~iF~ps|U_ulLnnqC")
                .coordenadasTotalPuntos(150)
                .tipoOrigenTrazaDescripcion("GPS_TIEMPO_REAL")
                .build();

        when(solicitudPaseoValidator.validateObtenerRecorridoConsolidado("usuario@paseape.com", uuid)).thenReturn(null);
        when(solicitudPaseoService.obtenerRecorridoConsolidado(uuid)).thenReturn(resDto);

        ResponseEntity<ResponseDto<SolicitudPaseoRecorridoResDto>> responseEntity = controller.obtenerRecorridoConsolidado(
                mockPrincipal,
                null,
                uuid
        );

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        assertEquals(MessageCodes.ResponseCodeS00, responseEntity.getBody().getCode());
        assertEquals(Constant.MSJ_RECORRIDO_OBTENIDO_EXITO, responseEntity.getBody().getMessage());
        assertEquals(150, responseEntity.getBody().getResponse().getCoordenadasTotalPuntos());

        verify(solicitudPaseoService, times(1)).obtenerRecorridoConsolidado(uuid);
    }

    @Test
    @DisplayName("obtenerRecorridoConsolidado - Falla de validación retorna HTTP 400 sin llamar al servicio")
    void testObtenerRecorridoConsolidado_ValidationError() throws BadRequestException {
        String uuid = "invalid-uuid";
        var valErr = new ResponseDto<>();
        valErr.setStatusCode(StatusCodes.Code400);
        valErr.setCode(MessageCodes.ResponseCodeBR01);
        valErr.setMessage("El identificador de la solicitud no tiene un formato UUID válido.");

        when(solicitudPaseoValidator.validateObtenerRecorridoConsolidado("usuario@paseape.com", uuid)).thenReturn(valErr);

        ResponseEntity<ResponseDto<SolicitudPaseoRecorridoResDto>> responseEntity = controller.obtenerRecorridoConsolidado(
                mockPrincipal,
                null,
                uuid
        );

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code400, responseEntity.getStatusCode().value());
        assertEquals(MessageCodes.ResponseCodeBR01, responseEntity.getBody().getCode());
        assertEquals("El identificador de la solicitud no tiene un formato UUID válido.", responseEntity.getBody().getMessage());

        verify(solicitudPaseoService, never()).obtenerRecorridoConsolidado(any());
    }

    @Test
    @DisplayName("obtenerRecorridoConsolidado - Manejo de BadRequestException del servicio")
    void testObtenerRecorridoConsolidado_ServiceBadRequest() throws BadRequestException {
        String uuid = UUID.randomUUID().toString();

        when(solicitudPaseoValidator.validateObtenerRecorridoConsolidado("usuario@paseape.com", uuid)).thenReturn(null);
        when(solicitudPaseoService.obtenerRecorridoConsolidado(uuid))
                .thenThrow(new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el recorrido histórico consolidado para este paseo."));

        ResponseEntity<ResponseDto<SolicitudPaseoRecorridoResDto>> responseEntity = controller.obtenerRecorridoConsolidado(
                mockPrincipal,
                null,
                uuid
        );

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code400, responseEntity.getStatusCode().value());
        assertEquals(MessageCodes.ResponseCodeBR98, responseEntity.getBody().getCode());
        assertTrue(responseEntity.getBody().getMessage().contains("No se encontró el recorrido"));
    }
}
