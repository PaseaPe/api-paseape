package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.INotificacionRepository;
import com.paseape.apipaseape.application.repository.IPaseadorRepository;
import com.paseape.apipaseape.application.repository.IPaseoCalificacionRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.Notificacion;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.domain.entity.PaseoCalificacion;
import com.paseape.apipaseape.domain.entity.SolicitudPaseo;
import com.paseape.apipaseape.domain.entity.SolicitudPaseoEstado;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.dto.request.RegistrarCalificacionReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.CalificacionResDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResumenCalificacionesPaseadorResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.IPaseoCalificacionDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalificacionServiceTest {

    @Mock
    private IPaseoCalificacionRepository paseoCalificacionRepository;
    @Mock
    private ISolicitudPaseoRepository solicitudPaseoRepository;
    @Mock
    private IUsuarioRepository usuarioRepository;
    @Mock
    private IClienteRepository clienteRepository;
    @Mock
    private IPaseadorRepository paseadorRepository;
    @Mock
    private INotificacionRepository notificacionRepository;

    private IPaseoCalificacionDtoMapper dtoMapper;
    private CalificacionService service;

    private Usuario clienteUsuario;
    private Cliente cliente;
    private Usuario paseadorUsuario;
    private Paseador paseador;
    private SolicitudPaseo solicitudFinalizada;

    @BeforeEach
    void setUp() {
        dtoMapper = Mappers.getMapper(IPaseoCalificacionDtoMapper.class);
        service = new CalificacionService(
                paseoCalificacionRepository,
                solicitudPaseoRepository,
                usuarioRepository,
                clienteRepository,
                paseadorRepository,
                notificacionRepository,
                dtoMapper
        );

        clienteUsuario = Usuario.builder()
                .id(10L)
                .uuid(UUID.randomUUID().toString())
                .nombres("Diego")
                .apellidos("Aparicio")
                .correo("cliente@paseape.com")
                .build();

        cliente = Cliente.builder()
                .id(1L)
                .uuid(UUID.randomUUID().toString())
                .usuario(clienteUsuario)
                .build();

        paseadorUsuario = Usuario.builder()
                .id(20L)
                .uuid(UUID.randomUUID().toString())
                .nombres("Roberto")
                .apellidos("Gómez")
                .correo("paseador@paseape.com")
                .build();

        paseador = Paseador.builder()
                .id(5L)
                .uuid(UUID.randomUUID().toString())
                .usuario(paseadorUsuario)
                .calificacionPromedio(new BigDecimal("4.00"))
                .totalCalificaciones(1)
                .sumaCalificaciones(4)
                .build();

        solicitudFinalizada = SolicitudPaseo.builder()
                .id(100L)
                .uuid(UUID.randomUUID().toString())
                .codigoReserva("PAS-PROG-202611-0001")
                .cliente(cliente)
                .paseador(paseador)
                .solicitudPaseoEstado(new SolicitudPaseoEstado(Constant.ID_ESTADO_SOLICITUD_FINALIZADA))
                .build();
    }

    @Test
    @DisplayName("registrarCalificacion - Éxito recalcula promedio, notifica y convierte fecha a Lima")
    void testRegistrarCalificacion_Exito() throws BadRequestException {
        LocalDateTime creadoUtc = LocalDateTime.of(2026, 11, 28, 2, 0, 0);
        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);
        when(solicitudPaseoRepository.findByUuidWithDetails(solicitudFinalizada.getUuid())).thenReturn(solicitudFinalizada);
        when(paseoCalificacionRepository.findBySolicitudPaseoId(100L)).thenReturn(null);
        when(paseadorRepository.findById(5L)).thenReturn(paseador);
        when(paseoCalificacionRepository.save(any(PaseoCalificacion.class))).thenAnswer(inv -> {
            PaseoCalificacion pc = inv.getArgument(0);
            pc.setId(50L);
            pc.setCreadoEl(creadoUtc);
            return pc;
        });

        CalificacionResDto res = service.registrarCalificacion(
                "cliente@paseape.com",
                solicitudFinalizada.getUuid(),
                RegistrarCalificacionReqDto.builder().puntuacion(5).build()
        );

        assertNotNull(res);
        assertEquals(5, res.getPuntuacion());
        assertEquals(LocalDateTime.of(2026, 11, 27, 21, 0, 0), res.getCreadoEl());
        assertEquals(2, paseador.getTotalCalificaciones());
        assertEquals(9, paseador.getSumaCalificaciones());
        assertEquals(new BigDecimal("4.50"), paseador.getCalificacionPromedio());
        verify(paseadorRepository).save(paseador);
        verify(notificacionRepository).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("registrarCalificacion - Falla si el paseo no está FINALIZADA")
    void testRegistrarCalificacion_NoFinalizada() {
        solicitudFinalizada.setSolicitudPaseoEstado(new SolicitudPaseoEstado(Constant.ID_ESTADO_SOLICITUD_EN_CURSO));
        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);
        when(solicitudPaseoRepository.findByUuidWithDetails(solicitudFinalizada.getUuid())).thenReturn(solicitudFinalizada);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.registrarCalificacion("cliente@paseape.com", solicitudFinalizada.getUuid(),
                        RegistrarCalificacionReqDto.builder().puntuacion(5).build()));

        assertEquals(MessageCodes.ResponseCodeBR03, ex.code);
        verify(paseoCalificacionRepository, never()).save(any());
    }

    @Test
    @DisplayName("registrarCalificacion - Falla si la solicitud pertenece a otro cliente")
    void testRegistrarCalificacion_ClienteAjeno() {
        solicitudFinalizada.setCliente(Cliente.builder().id(999L).build());
        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);
        when(solicitudPaseoRepository.findByUuidWithDetails(solicitudFinalizada.getUuid())).thenReturn(solicitudFinalizada);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.registrarCalificacion("cliente@paseape.com", solicitudFinalizada.getUuid(),
                        RegistrarCalificacionReqDto.builder().puntuacion(5).build()));

        assertEquals(MessageCodes.ResponseCodeBR03, ex.code);
        verify(paseoCalificacionRepository, never()).save(any());
    }

    @Test
    @DisplayName("registrarCalificacion - Falla si ya existe calificación previa")
    void testRegistrarCalificacion_Duplicada() {
        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);
        when(solicitudPaseoRepository.findByUuidWithDetails(solicitudFinalizada.getUuid())).thenReturn(solicitudFinalizada);
        when(paseoCalificacionRepository.findBySolicitudPaseoId(100L))
                .thenReturn(PaseoCalificacion.builder().id(1L).build());

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.registrarCalificacion("cliente@paseape.com", solicitudFinalizada.getUuid(),
                        RegistrarCalificacionReqDto.builder().puntuacion(4).build()));

        assertEquals(MessageCodes.ResponseCodeBR03, ex.code);
        verify(paseoCalificacionRepository, never()).save(any());
    }

    @Test
    @DisplayName("registrarCalificacion - Éxito con opinión opcional guarda comentario y fechaComentario en UTC")
    void testRegistrarCalificacion_ConOpinionOpcional_Exito() throws BadRequestException {
        LocalDateTime creadoUtc = LocalDateTime.of(2026, 11, 28, 2, 0, 0);
        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);
        when(solicitudPaseoRepository.findByUuidWithDetails(solicitudFinalizada.getUuid())).thenReturn(solicitudFinalizada);
        when(paseoCalificacionRepository.findBySolicitudPaseoId(100L)).thenReturn(null);
        when(paseadorRepository.findById(5L)).thenReturn(paseador);
        when(paseoCalificacionRepository.save(any(PaseoCalificacion.class))).thenAnswer(inv -> {
            PaseoCalificacion pc = inv.getArgument(0);
            pc.setId(50L);
            pc.setCreadoEl(creadoUtc);
            return pc;
        });

        CalificacionResDto res = service.registrarCalificacion(
                "cliente@paseape.com",
                solicitudFinalizada.getUuid(),
                RegistrarCalificacionReqDto.builder()
                        .puntuacion(5)
                        .comentario("  Excelente servicio y puntualidad.  ")
                        .build()
        );

        assertNotNull(res);
        assertEquals(5, res.getPuntuacion());
        assertEquals("Excelente servicio y puntualidad.", res.getComentario());
        assertNotNull(res.getFechaComentario());
        verify(paseoCalificacionRepository).save(any(PaseoCalificacion.class));
        verify(paseadorRepository).save(paseador);
        verify(notificacionRepository).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("obtenerCalificacionesPaseadorAutenticado - Retorna resumen y opiniones paginadas en Lima")
    void testObtenerCalificacionesPaseadorAutenticado_Exito() throws BadRequestException {
        PaseoCalificacion item = PaseoCalificacion.builder()
                .id(1L)
                .uuid(UUID.randomUUID().toString())
                .solicitudPaseo(solicitudFinalizada)
                .cliente(cliente)
                .paseador(paseador)
                .puntuacion(4)
                .comentario("Muy puntual")
                .fechaComentario(LocalDateTime.of(2026, 11, 28, 3, 15, 0))
                .creadoEl(LocalDateTime.of(2026, 11, 28, 3, 10, 0))
                .build();
        Page<PaseoCalificacion> page = new PageImpl<>(List.of(item));

        when(usuarioRepository.findByCorreo("paseador@paseape.com")).thenReturn(paseadorUsuario);
        when(paseadorRepository.findByUsuarioId(20L)).thenReturn(paseador);
        when(paseoCalificacionRepository.findByPaseadorIdVisiblesPaginado(eq(5L), any(Pageable.class))).thenReturn(page);

        ResumenCalificacionesPaseadorResDto res = service.obtenerCalificacionesPaseadorAutenticado("paseador@paseape.com", 0, 10);

        assertTrue(res.getTieneCalificaciones());
        assertEquals(1, res.getTotalCalificaciones());
        assertEquals(new BigDecimal("4.00"), res.getCalificacionPromedio());
        assertEquals(1, res.getOpiniones().getItems().size());
        assertEquals("Diego Aparicio", res.getOpiniones().getItems().get(0).getClienteNombreCompleto());
        assertEquals(LocalDateTime.of(2026, 11, 27, 22, 15, 0), res.getOpiniones().getItems().get(0).getFechaComentario());
    }

    @Test
    @DisplayName("obtenerCalificacionesPorPaseadorUuid - Sin calificaciones retorna estado vacío")
    void testObtenerCalificacionesPorPaseadorUuid_SinCalificaciones() throws BadRequestException {
        paseador.setTotalCalificaciones(0);
        paseador.setCalificacionPromedio(BigDecimal.ZERO);
        when(paseadorRepository.findByUuid(paseador.getUuid())).thenReturn(paseador);

        ResumenCalificacionesPaseadorResDto res = service.obtenerCalificacionesPorPaseadorUuid(paseador.getUuid(), 0, 10);

        assertFalse(res.getTieneCalificaciones());
        assertEquals(0, res.getTotalCalificaciones());
        assertEquals(new BigDecimal("0.00"), res.getCalificacionPromedio());
        assertEquals(Constant.MSJ_PASEADOR_SIN_CALIFICACIONES, res.getMensajeEstado());
        assertTrue(res.getOpiniones().getItems().isEmpty());
        verify(paseoCalificacionRepository, never()).findByPaseadorIdVisiblesPaginado(anyLong(), any());
    }
}
