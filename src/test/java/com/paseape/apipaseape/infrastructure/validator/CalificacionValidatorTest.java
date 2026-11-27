package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IPaseadorRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.domain.entity.TipoUsuario;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.domain.entity.UsuarioEstado;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.RegistrarCalificacionReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalificacionValidatorTest {

    @Mock
    private IUsuarioRepository usuarioRepository;
    @Mock
    private IClienteRepository clienteRepository;
    @Mock
    private IPaseadorRepository paseadorRepository;

    @InjectMocks
    private CalificacionValidator validator;

    private Usuario clienteUsuario;
    private Cliente cliente;
    private Usuario paseadorUsuario;
    private Paseador paseador;

    @BeforeEach
    void setUp() {
        clienteUsuario = Usuario.builder()
                .id(10L)
                .correo("cliente@paseape.com")
                .tipoUsuario(new TipoUsuario(Constant.ID_CLIENTE))
                .usuarioEstado(new UsuarioEstado(Constant.ID_ACTIVO))
                .build();
        cliente = Cliente.builder().id(1L).usuario(clienteUsuario).build();

        paseadorUsuario = Usuario.builder()
                .id(20L)
                .correo("paseador@paseape.com")
                .tipoUsuario(new TipoUsuario(Constant.ID_PASEADOR))
                .usuarioEstado(new UsuarioEstado(Constant.ID_ACTIVO))
                .build();
        paseador = Paseador.builder().id(5L).usuario(paseadorUsuario).build();
    }

    @Test
    @DisplayName("validateRegistrarCalificacion - Válido retorna null")
    void testValidateRegistrarCalificacion_Valido() {
        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);

        ResponseDto<?> err = validator.validateRegistrarCalificacion(
                "cliente@paseape.com",
                UUID.randomUUID().toString(),
                RegistrarCalificacionReqDto.builder().puntuacion(5).build()
        );
        assertNull(err);
    }

    @Test
    @DisplayName("validateRegistrarCalificacion - Rechaza puntuación fuera de rango 1..5")
    void testValidateRegistrarCalificacion_PuntuacionInvalida() {
        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);
        String uuid = UUID.randomUUID().toString();

        ResponseDto<?> err0 = validator.validateRegistrarCalificacion(
                "cliente@paseape.com", uuid, RegistrarCalificacionReqDto.builder().puntuacion(0).build());
        ResponseDto<?> err6 = validator.validateRegistrarCalificacion(
                "cliente@paseape.com", uuid, RegistrarCalificacionReqDto.builder().puntuacion(6).build());

        assertNotNull(err0);
        assertEquals(StatusCodes.Code400, err0.getStatusCode());
        assertNotNull(err6);
        assertEquals(StatusCodes.Code400, err6.getStatusCode());
    }

    @Test
    @DisplayName("validateRegistrarCalificacion - Rechaza opinión opcional menor a 5 o mayor a 1000 caracteres y acepta válida o vacía")
    void testValidateRegistrarCalificacion_OpinionOpcional() {
        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);
        String uuid = UUID.randomUUID().toString();

        ResponseDto<?> errCorto = validator.validateRegistrarCalificacion(
                "cliente@paseape.com", uuid, RegistrarCalificacionReqDto.builder().puntuacion(5).comentario("Bien").build());
        ResponseDto<?> errLargo = validator.validateRegistrarCalificacion(
                "cliente@paseape.com", uuid, RegistrarCalificacionReqDto.builder().puntuacion(5).comentario("a".repeat(1001)).build());
        ResponseDto<?> okValido = validator.validateRegistrarCalificacion(
                "cliente@paseape.com", uuid, RegistrarCalificacionReqDto.builder().puntuacion(5).comentario("Excelente paseo").build());
        ResponseDto<?> okVacio = validator.validateRegistrarCalificacion(
                "cliente@paseape.com", uuid, RegistrarCalificacionReqDto.builder().puntuacion(5).comentario("   ").build());

        assertNotNull(errCorto);
        assertNotNull(errLargo);
        assertNull(okValido);
        assertNull(okVacio);
    }

    @Test
    @DisplayName("validateConsultarCalificacionesPaseadorAutenticado - Válido y paginación inválida")
    void testValidateConsultarCalificacionesPaseadorAutenticado() {
        when(usuarioRepository.findByCorreo("paseador@paseape.com")).thenReturn(paseadorUsuario);
        when(paseadorRepository.findByUsuarioId(20L)).thenReturn(paseador);

        assertNull(validator.validateConsultarCalificacionesPaseadorAutenticado("paseador@paseape.com", 0, 10));
        assertNotNull(validator.validateConsultarCalificacionesPaseadorAutenticado("paseador@paseape.com", -1, 10));
        assertNotNull(validator.validateConsultarCalificacionesPaseadorAutenticado("paseador@paseape.com", 0, 100));
    }

    @Test
    @DisplayName("validateConsultarCalificacionesPaseadorPublico - Rechaza UUID inválido")
    void testValidateConsultarCalificacionesPaseadorPublico_UuidInvalido() {
        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);

        ResponseDto<?> err = validator.validateConsultarCalificacionesPaseadorPublico(
                "cliente@paseape.com", "no-es-uuid", 0, 10);

        assertNotNull(err);
        assertEquals(StatusCodes.Code400, err.getStatusCode());
    }
}
