package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CambiarDistritoCoberturaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaseadorValidatorTest {

    private PaseadorValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PaseadorValidator();
    }

    @Test
    @DisplayName("validarConsultaDisponibilidadPorFecha - Falla si paseadorUuid es nulo o vacío")
    void testValidarConsultaDisponibilidad_UuidNuloOVacio() {
        ErrorDetailDto errNull = validator.validarConsultaDisponibilidadPorFecha(null, LocalDate.now());
        assertNotNull(errNull);
        assertEquals(StatusCodes.Code400, errNull.getStatusCode());
        assertEquals(MessageCodes.ResponseCodeBR01, errNull.getCode());

        ErrorDetailDto errBlank = validator.validarConsultaDisponibilidadPorFecha("   ", LocalDate.now());
        assertNotNull(errBlank);
        assertEquals(StatusCodes.Code400, errBlank.getStatusCode());
        assertEquals(MessageCodes.ResponseCodeBR01, errBlank.getCode());
    }

    @Test
    @DisplayName("validarConsultaDisponibilidadPorFecha - Falla si paseadorUuid no tiene formato UUID válido")
    void testValidarConsultaDisponibilidad_UuidFormatoInvalido() {
        ErrorDetailDto err = validator.validarConsultaDisponibilidadPorFecha("12345-invalido", LocalDate.now());
        assertNotNull(err);
        assertEquals(StatusCodes.Code400, err.getStatusCode());
        assertEquals(MessageCodes.ResponseCodeBR01, err.getCode());
        assertTrue(err.getMessage().contains("formato del UUID"));
    }

    @Test
    @DisplayName("validarConsultaDisponibilidadPorFecha - Falla si fecha es anterior a hoy en Lima")
    void testValidarConsultaDisponibilidad_FechaPasada() {
        String uuidValido = UUID.randomUUID().toString();
        LocalDate ayerLima = LocalDate.now(ZoneId.of(Constant.ZONE_ID_LIMA)).minusDays(1);

        ErrorDetailDto err = validator.validarConsultaDisponibilidadPorFecha(uuidValido, ayerLima);
        assertNotNull(err);
        assertEquals(StatusCodes.Code400, err.getStatusCode());
        assertEquals(MessageCodes.ResponseCodeBR01, err.getCode());
        assertTrue(err.getMessage().contains("no puede ser anterior"));
    }

    @Test
    @DisplayName("validarConsultaDisponibilidadPorFecha - Éxito con fecha null, hoy o futura")
    void testValidarConsultaDisponibilidad_Exito() {
        String uuidValido = UUID.randomUUID().toString();
        LocalDate hoyLima = LocalDate.now(ZoneId.of(Constant.ZONE_ID_LIMA));
        LocalDate futuroLima = hoyLima.plusDays(3);

        assertNull(validator.validarConsultaDisponibilidadPorFecha(uuidValido, null));
        assertNull(validator.validarConsultaDisponibilidadPorFecha(uuidValido, hoyLima));
        assertNull(validator.validarConsultaDisponibilidadPorFecha(uuidValido, futuroLima));
    }

    @Test
    @DisplayName("validateCambiarDistritoCobertura - Validaciones existentes")
    void testValidateCambiarDistritoCobertura() {
        assertNotNull(validator.validateCambiarDistritoCobertura(null));

        var reqSinUuid = CambiarDistritoCoberturaReqDto.builder().distritoId(21).build();
        assertNotNull(validator.validateCambiarDistritoCobertura(reqSinUuid));

        var reqDistritoInvalido = CambiarDistritoCoberturaReqDto.builder().usuarioUuid(UUID.randomUUID().toString()).distritoId(0).build();
        assertNotNull(validator.validateCambiarDistritoCobertura(reqDistritoInvalido));

        var reqOk = CambiarDistritoCoberturaReqDto.builder().usuarioUuid(UUID.randomUUID().toString()).distritoId(21).build();
        assertNull(validator.validateCambiarDistritoCobertura(reqOk));
    }
}
