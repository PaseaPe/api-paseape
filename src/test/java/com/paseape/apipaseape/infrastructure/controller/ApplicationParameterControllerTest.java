package com.paseape.apipaseape.infrastructure.controller;

import com.paseape.apipaseape.application.service.ApplicationParameterService;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.response.HeaderDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.dto.response.parameter.ApplicationParameterResDto;
import com.paseape.apipaseape.infrastructure.dto.response.parameter.CatalogoItemResDto;
import com.paseape.apipaseape.infrastructure.dto.response.parameter.TipoRazaItemResDto;
import com.paseape.apipaseape.infrastructure.shared.RequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationParameterControllerTest {

    @Mock
    private ApplicationParameterService applicationParameterService;
    @Mock
    private RequestService requestService;

    @InjectMocks
    private ApplicationParameterController controller;

    @BeforeEach
    void setUp() {
        when(requestService.getResponseHeader(any(Date.class), any(Date.class)))
                .thenReturn(HeaderDto.builder().startDatetime(new Date()).endDatetime(new Date()).build());
    }

    @Test
    @DisplayName("GET /application-parameters - Éxito al recuperar todos los parámetros")
    void testGetAllParameters_Success() {
        var data = ApplicationParameterResDto.builder().build();
        when(applicationParameterService.getAllParameters()).thenReturn(data);

        ResponseEntity<ResponseDto<ApplicationParameterResDto>> responseEntity = controller.getAllParameters();

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        assertEquals(MessageCodes.ResponseCodeS00, responseEntity.getBody().getCode());
        verify(applicationParameterService).getAllParameters();
    }

    @Test
    @DisplayName("GET /application-parameters/razas/{tipoMascotaId} - Éxito al recuperar razas")
    void testGetRazasByTipoMascota_Success() {
        List<TipoRazaItemResDto> razas = List.of(
                TipoRazaItemResDto.builder().id(1).descripcion("Labrador").build()
        );
        when(applicationParameterService.getRazasByTipoMascotaId(1)).thenReturn(razas);

        ResponseEntity<ResponseDto<List<TipoRazaItemResDto>>> responseEntity = controller.getRazasByTipoMascota(1);

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        assertEquals(MessageCodes.ResponseCodeS00, responseEntity.getBody().getCode());
        assertEquals(1, responseEntity.getBody().getResponse().size());
    }

    @Test
    @DisplayName("GET /application-parameters/horario - Éxito al recuperar parámetros de horario")
    void testGetHorarioParameters_Success() {
        List<CatalogoItemResDto> dias = List.of(
                CatalogoItemResDto.builder().id(1).descripcion("LUNES").build(),
                CatalogoItemResDto.builder().id(2).descripcion("MARTES").build()
        );
        List<CatalogoItemResDto> bloques = List.of(
                CatalogoItemResDto.builder().id(1).descripcion("B06_07").build(),
                CatalogoItemResDto.builder().id(2).descripcion("B07_08").build()
        );
        var data = ApplicationParameterResDto.builder()
                .diasSemana(dias)
                .bloquesHorario(bloques)
                .build();
        when(applicationParameterService.getHorarioParameters()).thenReturn(data);

        ResponseEntity<ResponseDto<ApplicationParameterResDto>> responseEntity = controller.getHorarioParameters();

        assertNotNull(responseEntity);
        assertEquals(StatusCodes.Code200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        assertEquals(MessageCodes.ResponseCodeS00, responseEntity.getBody().getCode());
        assertEquals(2, responseEntity.getBody().getResponse().getDiasSemana().size());
        assertEquals(2, responseEntity.getBody().getResponse().getBloquesHorario().size());
    }
}
