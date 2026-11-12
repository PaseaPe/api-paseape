package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IPaseadorRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.domain.entity.SolicitudPaseo;
import com.paseape.apipaseape.domain.entity.TipoUsuario;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudPaseoDetalleValidatorTest {

    @Mock
    private IUsuarioRepository usuarioRepository;
    @Mock
    private IClienteRepository clienteRepository;
    @Mock
    private IPaseadorRepository paseadorRepository;
    @Mock
    private ISolicitudPaseoRepository solicitudPaseoRepository;

    @InjectMocks
    private SolicitudPaseoDetalleValidator validator;

    @Test
    @DisplayName("validateObtenerDetalle - Éxito para cliente dueño retorna null")
    void testValidateObtenerDetalle_ClienteDuenoExito() {
        String uuid = UUID.randomUUID().toString();
        Usuario usuario = Usuario.builder().id(10L).correo("cliente@paseape.com").tipoUsuario(new TipoUsuario(Constant.ID_CLIENTE)).build();
        Cliente cliente = Cliente.builder().id(1L).usuario(usuario).build();
        SolicitudPaseo solicitud = SolicitudPaseo.builder().id(100L).uuid(uuid).cliente(cliente).build();

        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(usuario);
        when(solicitudPaseoRepository.findByUuidWithDetails(uuid)).thenReturn(solicitud);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);
        when(paseadorRepository.findByUsuarioId(10L)).thenReturn(null);

        ResponseDto<?> error = validator.validateObtenerDetalle("cliente@paseape.com", uuid);
        assertNull(error);
    }

    @Test
    @DisplayName("validateObtenerDetalle - Éxito para paseador asignado retorna null")
    void testValidateObtenerDetalle_PaseadorAsignadoExito() {
        String uuid = UUID.randomUUID().toString();
        Usuario usuario = Usuario.builder().id(20L).correo("paseador@paseape.com").tipoUsuario(new TipoUsuario(Constant.ID_PASEADOR)).build();
        Paseador paseador = Paseador.builder().id(5L).usuario(usuario).build();
        Cliente cliente = Cliente.builder().id(1L).build();
        SolicitudPaseo solicitud = SolicitudPaseo.builder().id(100L).uuid(uuid).cliente(cliente).paseador(paseador).build();

        when(usuarioRepository.findByCorreo("paseador@paseape.com")).thenReturn(usuario);
        when(solicitudPaseoRepository.findByUuidWithDetails(uuid)).thenReturn(solicitud);
        when(clienteRepository.findByUsuarioId(20L)).thenReturn(null);
        when(paseadorRepository.findByUsuarioId(20L)).thenReturn(paseador);

        ResponseDto<?> error = validator.validateObtenerDetalle("paseador@paseape.com", uuid);
        assertNull(error);
    }

    @Test
    @DisplayName("validateObtenerDetalle - Éxito para administrador retorna null")
    void testValidateObtenerDetalle_AdminExito() {
        String uuid = UUID.randomUUID().toString();
        Usuario usuario = Usuario.builder().id(99L).correo("admin@paseape.com").tipoUsuario(new TipoUsuario(Constant.ID_ADMINISTRADOR)).build();
        SolicitudPaseo solicitud = SolicitudPaseo.builder().id(100L).uuid(uuid).build();

        when(usuarioRepository.findByCorreo("admin@paseape.com")).thenReturn(usuario);
        when(solicitudPaseoRepository.findByUuidWithDetails(uuid)).thenReturn(solicitud);

        ResponseDto<?> error = validator.validateObtenerDetalle("admin@paseape.com", uuid);
        assertNull(error);
    }

    @Test
    @DisplayName("validateObtenerDetalle - Falla cuando UUID tiene formato inválido")
    void testValidateObtenerDetalle_UuidInvalido() {
        Usuario usuario = Usuario.builder().id(10L).correo("cliente@paseape.com").build();
        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(usuario);

        ResponseDto<?> error = validator.validateObtenerDetalle("cliente@paseape.com", "uuid-mal-formado");

        assertNotNull(error);
        assertEquals(StatusCodes.Code400, error.getStatusCode());
        assertEquals(MessageCodes.ResponseCodeBR01, error.getCode());
    }

    @Test
    @DisplayName("validateObtenerDetalle - Falla cuando solicitud no existe")
    void testValidateObtenerDetalle_SolicitudNoExiste() {
        String uuid = UUID.randomUUID().toString();
        Usuario usuario = Usuario.builder().id(10L).correo("cliente@paseape.com").build();
        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(usuario);
        when(solicitudPaseoRepository.findByUuidWithDetails(uuid)).thenReturn(null);

        ResponseDto<?> error = validator.validateObtenerDetalle("cliente@paseape.com", uuid);

        assertNotNull(error);
        assertEquals(StatusCodes.Code400, error.getStatusCode());
        assertEquals(MessageCodes.ResponseCodeBR98, error.getCode());
    }

    @Test
    @DisplayName("validateObtenerDetalle - Falla cuando usuario tercero no tiene permiso")
    void testValidateObtenerDetalle_SinPermiso() {
        String uuid = UUID.randomUUID().toString();
        Usuario usuarioTercero = Usuario.builder().id(30L).correo("otro@paseape.com").tipoUsuario(new TipoUsuario(Constant.ID_CLIENTE)).build();
        Cliente clienteTercero = Cliente.builder().id(999L).usuario(usuarioTercero).build();
        Cliente clienteDueno = Cliente.builder().id(1L).build();
        SolicitudPaseo solicitud = SolicitudPaseo.builder().id(100L).uuid(uuid).cliente(clienteDueno).build();

        when(usuarioRepository.findByCorreo("otro@paseape.com")).thenReturn(usuarioTercero);
        when(solicitudPaseoRepository.findByUuidWithDetails(uuid)).thenReturn(solicitud);
        when(clienteRepository.findByUsuarioId(30L)).thenReturn(clienteTercero);
        when(paseadorRepository.findByUsuarioId(30L)).thenReturn(null);

        ResponseDto<?> error = validator.validateObtenerDetalle("otro@paseape.com", uuid);

        assertNotNull(error);
        assertEquals(StatusCodes.Code400, error.getStatusCode());
        assertEquals(MessageCodes.ResponseCodeBR03, error.getCode());
    }
}
