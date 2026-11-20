package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.application.repository.ITipoMascotaRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.TipoMascota;
import com.paseape.apipaseape.domain.entity.TipoUsuario;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.domain.entity.UsuarioEstado;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.catalogo.CatalogoBaseReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.catalogo.DistritoLimaReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.catalogo.TipoRazaReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.catalogo.TipoTamanoMascotaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogoValidatorTest {

    @Mock
    private IUsuarioRepository usuarioRepository;
    @Mock
    private ITipoMascotaRepository tipoMascotaRepository;

    private CatalogoValidator validator;
    private Usuario adminActivo;

    @BeforeEach
    void setUp() {
        validator = new CatalogoValidator(usuarioRepository, tipoMascotaRepository);
        adminActivo = Usuario.builder()
                .id(1L)
                .correo("admin@paseape.com")
                .usuarioEstado(UsuarioEstado.builder().id(Constant.ID_ACTIVO).build())
                .tipoUsuario(TipoUsuario.builder().id(Constant.ID_ADMINISTRADOR).build())
                .build();
    }

    @Test
    @DisplayName("validateConsultaCatalogo - Falla si usuario no autenticado o no es administrador")
    void testValidateConsultaCatalogo_Permisos() {
        ResponseDto<?> errNull = validator.validateConsultaCatalogo(null, null);
        assertNotNull(errNull);
        assertEquals(StatusCodes.Code401, errNull.getStatusCode());

        Usuario cliente = Usuario.builder()
                .id(2L)
                .correo("cliente@paseape.com")
                .usuarioEstado(UsuarioEstado.builder().id(Constant.ID_ACTIVO).build())
                .tipoUsuario(TipoUsuario.builder().id(Constant.ID_CLIENTE).build())
                .build();
        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(cliente);

        ResponseDto<?> errRol = validator.validateConsultaCatalogo("cliente@paseape.com", null);
        assertNotNull(errRol);
        assertEquals(StatusCodes.Code403, errRol.getStatusCode());
        assertEquals(MessageCodes.ResponseCodeBR03, errRol.getCode());
    }

    @Test
    @DisplayName("validateConsultaCatalogo - Valida filtro de estado y éxito para administrador")
    void testValidateConsultaCatalogo_Estado() {
        when(usuarioRepository.findByCorreo("admin@paseape.com")).thenReturn(adminActivo);

        ResponseDto<?> errEstado = validator.validateConsultaCatalogo("admin@paseape.com", 9);
        assertNotNull(errEstado);
        assertEquals(StatusCodes.Code400, errEstado.getStatusCode());

        assertNull(validator.validateConsultaCatalogo("admin@paseape.com", null));
        assertNull(validator.validateConsultaCatalogo("admin@paseape.com", Constant.ESTADO_LOGICO_ACTIVO));
        assertNull(validator.validateConsultaCatalogo("admin@paseape.com", Constant.ESTADO_LOGICO_INACTIVO));
    }

    @Test
    @DisplayName("validateDistrito - Valida campos obligatorios, longitudes y éxito")
    void testValidateDistrito() {
        when(usuarioRepository.findByCorreo("admin@paseape.com")).thenReturn(adminActivo);

        assertNotNull(validator.validateDistrito("admin@paseape.com", null, null));
        assertNotNull(validator.validateDistrito("admin@paseape.com", null, DistritoLimaReqDto.builder().descripcion("  ").build()));
        assertNotNull(validator.validateDistrito("admin@paseape.com", null,
                DistritoLimaReqDto.builder().descripcion("Miraflores").codigoUbigeo("12345678901").build()));

        assertNull(validator.validateDistrito("admin@paseape.com", null,
                DistritoLimaReqDto.builder().descripcion("Chorrillos").codigoUbigeo("150108").build()));
    }

    @Test
    @DisplayName("validateTipoRaza - Valida existencia y estado activo de TipoMascota")
    void testValidateTipoRaza() {
        when(usuarioRepository.findByCorreo("admin@paseape.com")).thenReturn(adminActivo);
        String tipoMascotaUuid = UUID.randomUUID().toString();

        when(tipoMascotaRepository.findByUuid(tipoMascotaUuid)).thenReturn(null);
        ResponseDto<?> errNotFound = validator.validateTipoRaza("admin@paseape.com", null,
                TipoRazaReqDto.builder().tipoMascotaUuid(tipoMascotaUuid).descripcion("Pug").build());
        assertNotNull(errNotFound);
        assertEquals(MessageCodes.ResponseCodeBR98, errNotFound.getCode());

        when(tipoMascotaRepository.findByUuid(tipoMascotaUuid)).thenReturn(
                TipoMascota.builder().id(1).uuid(tipoMascotaUuid).estado(Constant.ESTADO_LOGICO_INACTIVO).build());
        ResponseDto<?> errInactivo = validator.validateTipoRaza("admin@paseape.com", null,
                TipoRazaReqDto.builder().tipoMascotaUuid(tipoMascotaUuid).descripcion("Pug").build());
        assertNotNull(errInactivo);
        assertEquals(MessageCodes.ResponseCodeBR04, errInactivo.getCode());

        when(tipoMascotaRepository.findByUuid(tipoMascotaUuid)).thenReturn(
                TipoMascota.builder().id(1).uuid(tipoMascotaUuid).estado(Constant.ESTADO_LOGICO_ACTIVO).build());
        assertNull(validator.validateTipoRaza("admin@paseape.com", null,
                TipoRazaReqDto.builder().tipoMascotaUuid(tipoMascotaUuid).descripcion("Pug").build()));
    }

    @Test
    @DisplayName("validateTipoTamanoMascota y validateCatalogoBase - Validan longitudes máximas")
    void testValidateTamanosYBase() {
        when(usuarioRepository.findByCorreo("admin@paseape.com")).thenReturn(adminActivo);

        assertNotNull(validator.validateCatalogoBase("admin@paseape.com", null,
                CatalogoBaseReqDto.builder().descripcion("a".repeat(31)).build(), 30));
        assertNull(validator.validateCatalogoBase("admin@paseape.com", null,
                CatalogoBaseReqDto.builder().descripcion("Hembra").build(), 30));

        assertNotNull(validator.validateTipoTamanoMascota("admin@paseape.com", null,
                TipoTamanoMascotaReqDto.builder().descripcion("Mediano").rangoPesoRef("x".repeat(51)).build()));
        assertNull(validator.validateTipoTamanoMascota("admin@paseape.com", null,
                TipoTamanoMascotaReqDto.builder().descripcion("Mediano").rangoPesoRef("10 a 25 kg").build()));
    }

    @Test
    @DisplayName("validateAccionPorUuid - Valida formato UUID")
    void testValidateAccionPorUuid() {
        when(usuarioRepository.findByCorreo("admin@paseape.com")).thenReturn(adminActivo);

        assertNotNull(validator.validateAccionPorUuid("admin@paseape.com", "no-es-uuid"));
        assertNull(validator.validateAccionPorUuid("admin@paseape.com", UUID.randomUUID().toString()));
    }
}
