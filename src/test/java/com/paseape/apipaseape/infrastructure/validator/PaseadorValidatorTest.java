package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CambiarDistritoCoberturaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaseadorValidatorTest {

    @Mock
    private IUsuarioRepository usuarioRepository;

    @Mock
    private StorageValidator storageValidator;

    @InjectMocks
    private PaseadorValidator validator;

    @Test
    @DisplayName("validateSubirFotoUsuario - Falla cuando principal es nulo o su nombre está vacío")
    void testValidateSubirFotoUsuario_PrincipalInvalido() {
        MultipartFile file = mock(MultipartFile.class);

        ErrorDetailDto errNull = validator.validateSubirFotoUsuario(null, file);
        assertNotNull(errNull);
        assertEquals(StatusCodes.Code401, errNull.getStatusCode());
        assertEquals(MessageCodes.ResponseCodeE01, errNull.getCode());

        Principal principalVacio = () -> "   ";
        ErrorDetailDto errBlank = validator.validateSubirFotoUsuario(principalVacio, file);
        assertNotNull(errBlank);
        assertEquals(StatusCodes.Code401, errBlank.getStatusCode());
        assertEquals(MessageCodes.ResponseCodeE01, errBlank.getCode());

        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(storageValidator);
    }

    @Test
    @DisplayName("validateSubirFotoUsuario - Falla cuando el usuario no existe en la BD")
    void testValidateSubirFotoUsuario_UsuarioNoExiste() {
        String correo = "paseador@paseape.com";
        Principal principal = () -> correo;
        MultipartFile file = mock(MultipartFile.class);

        when(usuarioRepository.findByCorreo(correo)).thenReturn(null);

        ErrorDetailDto err = validator.validateSubirFotoUsuario(principal, file);

        assertNotNull(err);
        assertEquals(StatusCodes.Code404, err.getStatusCode());
        assertEquals(MessageCodes.ResponseCodeBR98, err.getCode());
        verifyNoInteractions(storageValidator);
    }

    @Test
    @DisplayName("validateSubirFotoUsuario - Retorna error cuando storageValidator rechaza el archivo")
    void testValidateSubirFotoUsuario_ErrorArchivo() {
        String correo = "paseador@paseape.com";
        Principal principal = () -> correo;
        MultipartFile file = mock(MultipartFile.class);
        Usuario usuario = Usuario.builder().id(10L).correo(correo).build();

        ErrorDetailDto fileError = ErrorDetailDto.builder()
                .statusCode(StatusCodes.Code400)
                .code(MessageCodes.ResponseCodeBR01)
                .message("Tipo de archivo no permitido")
                .build();

        when(usuarioRepository.findByCorreo(correo)).thenReturn(usuario);
        when(storageValidator.validateUpload(file)).thenReturn(fileError);

        ErrorDetailDto err = validator.validateSubirFotoUsuario(principal, file);

        assertNotNull(err);
        assertEquals(StatusCodes.Code400, err.getStatusCode());
        assertEquals(MessageCodes.ResponseCodeBR01, err.getCode());
        assertEquals("Tipo de archivo no permitido", err.getMessage());
    }

    @Test
    @DisplayName("validateSubirFotoUsuario - Retorna null cuando todas las validaciones son exitosas")
    void testValidateSubirFotoUsuario_Exito() {
        String correo = "paseador@paseape.com";
        Principal principal = () -> correo;
        MultipartFile file = mock(MultipartFile.class);
        Usuario usuario = Usuario.builder().id(10L).correo(correo).build();

        when(usuarioRepository.findByCorreo(correo)).thenReturn(usuario);
        when(storageValidator.validateUpload(file)).thenReturn(null);

        ErrorDetailDto err = validator.validateSubirFotoUsuario(principal, file);

        assertNull(err);
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