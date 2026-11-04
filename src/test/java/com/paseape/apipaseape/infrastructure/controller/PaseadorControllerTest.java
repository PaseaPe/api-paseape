package com.paseape.apipaseape.infrastructure.controller;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.application.service.DisponibilidadPaseadorService;
import com.paseape.apipaseape.application.service.PaseadorService;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.response.DisponibilidadBloqueFechaResDto;
import com.paseape.apipaseape.infrastructure.dto.response.DisponibilidadPaseadorFechaResDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
import com.paseape.apipaseape.infrastructure.dto.response.HeaderDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.shared.RequestService;
import com.paseape.apipaseape.infrastructure.validator.PaseadorValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaseadorControllerTest {

    @Mock
    private PaseadorService paseadorService;
    @Mock
    private DisponibilidadPaseadorService disponibilidadPaseadorService;
    @Mock
    private IUsuarioRepository usuarioRepository;
    @Mock
    private IClienteRepository clienteRepository;
    @Mock
    private PaseadorValidator paseadorValidator;
    @Mock
    private RequestService requestService;

    @InjectMocks
    private PaseadorController controller;

    @BeforeEach
    void setUp() {
        when(requestService.getResponseHeader(any(Date.class), any(Date.class)))
                .thenReturn(HeaderDto.builder().startDatetime(new Date()).endDatetime(new Date()).build());
    }

    @Test
    @DisplayName("GET /paseadores/{paseadorUuid}/disponibilidad - Éxito retorna 200 y ResponseDto")
    void testObtenerDisponibilidadPorFecha_Exito() throws BadRequestException {
        String uuid = UUID.randomUUID().toString();
        LocalDate fecha = LocalDate.now().plusDays(2);

        when(paseadorValidator.validarConsultaDisponibilidadPorFecha(uuid, fecha)).thenReturn(null);

        DisponibilidadPaseadorFechaResDto resDto = DisponibilidadPaseadorFechaResDto.builder()
                .paseadorUuid(uuid)
                .fecha(fecha)
                .diaSemanaId(3)
                .diaSemanaDescripcion("MIERCOLES")
                .bloques(List.of(
                        DisponibilidadBloqueFechaResDto.builder()
                                .bloqueHorarioId(1)
                                .bloqueCodigo("B08_09")
                                .horaInicio("08:00")
                                .horaFin("09:00")
                                .maxMascotas(3)
                                .mascotasOcupadas(0)
                                .cuposDisponibles(3)
                                .estaLleno(false)
                                .disponible(true)
                                .motivoNoDisponible(null)
                                .build()
                ))
                .build();

        when(paseadorService.obtenerDisponibilidadPorFecha(uuid, fecha)).thenReturn(resDto);

        ResponseEntity<ResponseDto<DisponibilidadPaseadorFechaResDto>> response =
                controller.obtenerDisponibilidadPorFecha(uuid, fecha);

        assertNotNull(response);
        assertEquals(StatusCodes.Code200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(MessageCodes.ResponseCodeS00, response.getBody().getCode());
        assertEquals(Constant.MSJ_DISPONIBILIDAD_FECHA_OBTENIDA, response.getBody().getMessage());
        assertEquals(uuid, response.getBody().getResponse().getPaseadorUuid());
        assertEquals(1, response.getBody().getResponse().getBloques().size());
    }

    @Test
    @DisplayName("GET /paseadores/{paseadorUuid}/disponibilidad - Error de validación retorna 400 sin llamar al servicio")
    void testObtenerDisponibilidadPorFecha_ErrorValidacion() throws BadRequestException {
        String uuidInvalido = "invalido";
        LocalDate fecha = LocalDate.now();

        when(paseadorValidator.validarConsultaDisponibilidadPorFecha(uuidInvalido, fecha))
                .thenReturn(ErrorDetailDto.builder()
                        .statusCode(StatusCodes.Code400)
                        .code(MessageCodes.ResponseCodeBR01)
                        .message("El formato del UUID del paseador no es válido.")
                        .build());

        ResponseEntity<ResponseDto<DisponibilidadPaseadorFechaResDto>> response =
                controller.obtenerDisponibilidadPorFecha(uuidInvalido, fecha);

        assertNotNull(response);
        assertEquals(StatusCodes.Code400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(MessageCodes.ResponseCodeBR01, response.getBody().getCode());
        assertEquals("El formato del UUID del paseador no es válido.", response.getBody().getMessage());
        verify(paseadorService, never()).obtenerDisponibilidadPorFecha(anyString(), any());
    }

    @Test
    @DisplayName("GET /paseadores/{paseadorUuid}/disponibilidad - BadRequestException del servicio retorna 400")
    void testObtenerDisponibilidadPorFecha_BadRequestException() throws BadRequestException {
        String uuid = UUID.randomUUID().toString();
        LocalDate fecha = LocalDate.now();

        when(paseadorValidator.validarConsultaDisponibilidadPorFecha(uuid, fecha)).thenReturn(null);
        when(paseadorService.obtenerDisponibilidadPorFecha(uuid, fecha))
                .thenThrow(new BadRequestException(Constant.MSJ_ERROR_PASEADOR_NO_DISPONIBLE));

        ResponseEntity<ResponseDto<DisponibilidadPaseadorFechaResDto>> response =
                controller.obtenerDisponibilidadPorFecha(uuid, fecha);

        assertNotNull(response);
        assertEquals(StatusCodes.Code400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(Constant.MSJ_ERROR_PASEADOR_NO_DISPONIBLE, response.getBody().getMessage());
    }
}
