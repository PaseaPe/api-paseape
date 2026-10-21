package com.paseape.apipaseape.infrastructure.controller;

import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.application.service.DisponibilidadPaseadorService;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarBloqueDisponibilidadReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarCapacidadBaseReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.DisponibilidadItemReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.GuardarDisponibilidadReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.DisponibilidadPaseadorResDto;
import com.paseape.apipaseape.infrastructure.dto.response.DisponibilidadSlotResDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
import com.paseape.apipaseape.infrastructure.dto.response.HeaderDto;
import com.paseape.apipaseape.infrastructure.dto.response.PaseadorPerfilResDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.security.JwtTokenProvider;
import com.paseape.apipaseape.infrastructure.shared.RequestService;
import com.paseape.apipaseape.infrastructure.validator.DisponibilidadPaseadorValidator;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisponibilidadPaseadorControllerTest {

    @Mock
    private DisponibilidadPaseadorService disponibilidadService;
    @Mock
    private DisponibilidadPaseadorValidator disponibilidadValidator;
    @Mock
    private RequestService requestService;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private IUsuarioRepository usuarioRepository;

    @InjectMocks
    private DisponibilidadPaseadorController controller;

    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockPrincipal = () -> "walker@paseape.com";
        when(requestService.getResponseHeader(any(Date.class), any(Date.class)))
                .thenReturn(HeaderDto.builder().startDatetime(new Date()).endDatetime(new Date()).build());
    }

    @Test
    @DisplayName("GET /api/v1/paseador/disponibilidad - Éxito al obtener disponibilidad")
    void testObtenerMiDisponibilidad_Success() throws Exception {
        when(usuarioRepository.findByCorreo("walker@paseape.com"))
                .thenReturn(Usuario.builder().id(5L).correo("walker@paseape.com").build());

        var dto = DisponibilidadPaseadorResDto.builder()
                .maxMascotasPredeterminado(3)
                .slots(List.of(DisponibilidadSlotResDto.builder().id(1L).diaSemanaId(1).bloqueHorarioId(1).activo(true).build()))
                .build();
        when(disponibilidadService.obtenerMiDisponibilidad(5L)).thenReturn(dto);

        ResponseEntity<ResponseDto<DisponibilidadPaseadorResDto>> responseEntity =
                controller.obtenerMiDisponibilidad(mockPrincipal, null);

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        assertEquals(MessageCodes.ResponseCodeS00, responseEntity.getBody().getCode());
        assertEquals(3, responseEntity.getBody().getResponse().getMaxMascotasPredeterminado());
    }

    @Test
    @DisplayName("PUT /api/v1/paseador/disponibilidad - Falla de validación retorna 400")
    void testSincronizarDisponibilidad_ValidationError() throws Exception {
        var req = GuardarDisponibilidadReqDto.builder().bloques(List.of()).build();
        when(disponibilidadValidator.validarGuardarDisponibilidad(req))
                .thenReturn(ErrorDetailDto.builder().statusCode(StatusCodes.Code400).code(MessageCodes.ResponseCodeBR01).message("Error").build());

        ResponseEntity<ResponseDto<DisponibilidadPaseadorResDto>> responseEntity =
                controller.sincronizarDisponibilidad(mockPrincipal, null, req);

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code400, responseEntity.getStatusCode().value());
        assertEquals(MessageCodes.ResponseCodeBR01, responseEntity.getBody().getCode());
        verify(disponibilidadService, never()).sincronizarDisponibilidad(any(), any());
    }

    @Test
    @DisplayName("PUT /api/v1/paseador/disponibilidad - Éxito al sincronizar")
    void testSincronizarDisponibilidad_Success() throws Exception {
        var req = GuardarDisponibilidadReqDto.builder()
                .bloques(List.of(DisponibilidadItemReqDto.builder().diaSemanaId(1).bloqueHorarioId(2).build()))
                .build();

        when(disponibilidadValidator.validarGuardarDisponibilidad(req)).thenReturn(null);
        when(usuarioRepository.findByCorreo("walker@paseape.com"))
                .thenReturn(Usuario.builder().id(5L).correo("walker@paseape.com").build());

        var resDto = DisponibilidadPaseadorResDto.builder().maxMascotasPredeterminado(3).slots(List.of()).build();
        when(disponibilidadService.sincronizarDisponibilidad(5L, req)).thenReturn(resDto);

        ResponseEntity<ResponseDto<DisponibilidadPaseadorResDto>> responseEntity =
                controller.sincronizarDisponibilidad(mockPrincipal, null, req);

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code200, responseEntity.getStatusCode().value());
        assertEquals(MessageCodes.ResponseCodeS00, responseEntity.getBody().getCode());
    }

    @Test
    @DisplayName("PATCH /api/v1/paseador/disponibilidad/bloque - Éxito al actualizar bloque")
    void testActualizarBloque_Success() throws Exception {
        var req = ActualizarBloqueDisponibilidadReqDto.builder()
                .diaSemanaId(1)
                .bloqueHorarioId(2)
                .accion(Constant.ACCION_BLOQUE_GUARDAR_CUPO)
                .maxMascotas(4)
                .build();

        when(disponibilidadValidator.validarActualizarBloque(req)).thenReturn(null);
        when(usuarioRepository.findByCorreo("walker@paseape.com"))
                .thenReturn(Usuario.builder().id(5L).correo("walker@paseape.com").build());

        var resDto = DisponibilidadSlotResDto.builder().id(10L).maxMascotas(4).activo(true).build();
        when(disponibilidadService.actualizarBloqueDisponibilidad(5L, req)).thenReturn(resDto);

        ResponseEntity<ResponseDto<DisponibilidadSlotResDto>> responseEntity =
                controller.actualizarBloque(mockPrincipal, null, req);

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code200, responseEntity.getStatusCode().value());
        assertEquals(MessageCodes.ResponseCodeS00, responseEntity.getBody().getCode());
        assertEquals(4, responseEntity.getBody().getResponse().getMaxMascotas());
    }

    @Test
    @DisplayName("PATCH /api/v1/paseador/disponibilidad/capacidad-base - Éxito al actualizar capacidad base")
    void testActualizarCapacidadBase_Success() throws Exception {
        var req = ActualizarCapacidadBaseReqDto.builder().maxMascotasPredeterminado(5).build();

        when(disponibilidadValidator.validarActualizarCapacidadBase(req)).thenReturn(null);
        when(usuarioRepository.findByCorreo("walker@paseape.com"))
                .thenReturn(Usuario.builder().id(5L).correo("walker@paseape.com").build());

        var resDto = PaseadorPerfilResDto.builder().uuid("some-uuid").build();
        when(disponibilidadService.actualizarCapacidadBase(5L, req)).thenReturn(resDto);

        ResponseEntity<ResponseDto<PaseadorPerfilResDto>> responseEntity =
                controller.actualizarCapacidadBase(mockPrincipal, null, req);

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code200, responseEntity.getStatusCode().value());
        assertEquals(MessageCodes.ResponseCodeS00, responseEntity.getBody().getCode());
    }
}
