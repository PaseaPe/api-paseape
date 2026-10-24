package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.IBloqueHorarioRepository;
import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IDiaSemanaRepository;
import com.paseape.apipaseape.application.repository.IDisponibilidadPaseadorRepository;
import com.paseape.apipaseape.application.repository.IMascotaRepository;
import com.paseape.apipaseape.application.repository.INotificacionRepository;
import com.paseape.apipaseape.application.repository.IPaseadorRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoMascotaRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoRepository;
import com.paseape.apipaseape.application.repository.ITipoDuracionPaseoRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.BloqueHorario;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.DiaSemana;
import com.paseape.apipaseape.domain.entity.DisponibilidadPaseador;
import com.paseape.apipaseape.domain.entity.DistritoLima;
import com.paseape.apipaseape.domain.entity.Mascota;
import com.paseape.apipaseape.domain.entity.Notificacion;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.domain.entity.PaseadorEstadoVerificacion;
import com.paseape.apipaseape.domain.entity.SolicitudPaseo;
import com.paseape.apipaseape.domain.entity.SolicitudPaseoEstado;
import com.paseape.apipaseape.domain.entity.SolicitudPaseoMascota;
import com.paseape.apipaseape.domain.entity.TipoDuracionPaseo;
import com.paseape.apipaseape.domain.entity.TipoMetodoPago;
import com.paseape.apipaseape.domain.entity.TipoModalidadPaseo;
import com.paseape.apipaseape.domain.entity.TipoRaza;
import com.paseape.apipaseape.domain.entity.TipoTamanoMascota;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudInmediataReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudProgramadaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudPaseoResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.ISolicitudPaseoDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudPaseoServiceTest {

    @Mock
    private ISolicitudPaseoRepository solicitudPaseoRepository;
    @Mock
    private ISolicitudPaseoMascotaRepository solicitudPaseoMascotaRepository;
    @Mock
    private IClienteRepository clienteRepository;
    @Mock
    private IPaseadorRepository paseadorRepository;
    @Mock
    private IUsuarioRepository usuarioRepository;
    @Mock
    private IMascotaRepository mascotaRepository;
    @Mock
    private ITipoDuracionPaseoRepository tipoDuracionPaseoRepository;
    @Mock
    private IBloqueHorarioRepository bloqueHorarioRepository;
    @Mock
    private IDisponibilidadPaseadorRepository disponibilidadPaseadorRepository;
    @Mock
    private IDiaSemanaRepository diaSemanaRepository;
    @Mock
    private INotificacionRepository notificacionRepository;

    private ISolicitudPaseoDtoMapper solicitudPaseoDtoMapper;
    private SolicitudPaseoService service;

    private Usuario clienteUsuario;
    private Cliente cliente;
    private Mascota mascota1;
    private Mascota mascota2;
    private TipoDuracionPaseo duracion60Min;

    private Usuario paseadorUsuario;
    private Paseador paseador;

    @BeforeEach
    void setUp() {
        solicitudPaseoDtoMapper = Mappers.getMapper(ISolicitudPaseoDtoMapper.class);
        service = new SolicitudPaseoService(
                solicitudPaseoRepository,
                solicitudPaseoMascotaRepository,
                clienteRepository,
                paseadorRepository,
                usuarioRepository,
                mascotaRepository,
                tipoDuracionPaseoRepository,
                bloqueHorarioRepository,
                disponibilidadPaseadorRepository,
                diaSemanaRepository,
                notificacionRepository,
                solicitudPaseoDtoMapper
        );

        clienteUsuario = Usuario.builder()
                .id(10L)
                .uuid(UUID.randomUUID().toString())
                .nombres("Carlos")
                .apellidos("Mendoza")
                .correo("cliente@paseape.com")
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();

        cliente = Cliente.builder()
                .id(1L)
                .uuid(UUID.randomUUID().toString())
                .usuario(clienteUsuario)
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();

        mascota1 = Mascota.builder()
                .id(101L)
                .uuid(UUID.randomUUID().toString())
                .nombre("Bobby")
                .cliente(cliente)
                .tipoRaza(TipoRaza.builder().id(1).descripcion("MESTIZO").build())
                .tipoTamanoMascota(TipoTamanoMascota.builder().id(3).descripcion("MEDIANO").build())
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();

        mascota2 = Mascota.builder()
                .id(102L)
                .uuid(UUID.randomUUID().toString())
                .nombre("Luna")
                .cliente(cliente)
                .tipoRaza(TipoRaza.builder().id(3).descripcion("LABRADOR RETRIEVER").build())
                .tipoTamanoMascota(TipoTamanoMascota.builder().id(4).descripcion("GRANDE").build())
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();

        duracion60Min = TipoDuracionPaseo.builder()
                .id(2)
                .descripcion("60 MINUTOS")
                .minutos(60)
                .bloquesRequeridos(1)
                .build();

        paseadorUsuario = Usuario.builder()
                .id(20L)
                .uuid(UUID.randomUUID().toString())
                .nombres("Luis")
                .apellidos("García")
                .correo("paseador@paseape.com")
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();

        paseador = Paseador.builder()
                .id(2L)
                .uuid(UUID.randomUUID().toString())
                .usuario(paseadorUsuario)
                .distritoCobertura(new DistritoLima(21))
                .tarifaHoraPen(new BigDecimal("25.00"))
                .estadoVerificacion(PaseadorEstadoVerificacion.builder().id(Constant.ID_APROBADO).descripcion("APROBADO").build())
                .maxMascotasPredeterminado(3)
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();
    }

    @Test
    @DisplayName("crearSolicitudInmediata - Éxito al crear solicitud inmediata con 2 mascotas")
    void testCrearSolicitudInmediata_Exito() throws BadRequestException {
        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(mascota1.getUuid(), mascota2.getUuid()))
                .distritoId(21)
                .tipoDuracionPaseoId(2)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Primavera 123")
                .referenciaDireccion("Frente al parque")
                .build();

        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);
        when(mascotaRepository.findByUuid(mascota1.getUuid())).thenReturn(mascota1);
        when(mascotaRepository.findByUuid(mascota2.getUuid())).thenReturn(mascota2);
        when(tipoDuracionPaseoRepository.findById(2)).thenReturn(duracion60Min);

        when(solicitudPaseoRepository.save(any(SolicitudPaseo.class))).thenAnswer(invocation -> {
            SolicitudPaseo sp = invocation.getArgument(0);
            sp.setId(500L);
            return sp;
        });

        when(solicitudPaseoRepository.findByUuidWithDetails(anyString())).thenAnswer(invocation -> {
            String uuid = invocation.getArgument(0);
            return SolicitudPaseo.builder()
                    .id(500L)
                    .uuid(uuid)
                    .codigoReserva("PAS-INM-202610-ABCD")
                    .tipoModalidadPaseo(TipoModalidadPaseo.builder().id(Constant.ID_MODALIDAD_INMEDIATO).descripcion("INMEDIATO").build())
                    .tipoMetodoPago(TipoMetodoPago.builder().id(1).descripcion("YAPE / PLIN").build())
                    .tipoDuracionPaseo(duracion60Min)
                    .solicitudPaseoEstado(SolicitudPaseoEstado.builder().id(Constant.ID_ESTADO_SOLICITUD_BUSCANDO).descripcion("SOLICITADA_BUSCANDO").build())
                    .distrito(DistritoLima.builder().id(21).descripcion("MIRAFLORES").build())
                    .direccionRecojo("Av. Primavera 123")
                    .referenciaDireccion("Frente al parque")
                    .tarifaHoraPenSnapshot(Constant.TARIFA_BASE_INMEDIATO_HORA_PEN)
                    .montoTotal(new BigDecimal("50.00")) // (25 / 60) * 60 * 2 = 50.00
                    .cliente(cliente)
                    .creadoEl(LocalDateTime.now())
                    .build();
        });

        SolicitudPaseoResDto result = service.crearSolicitudInmediata("cliente@paseape.com", req);

        assertNotNull(result);
        assertEquals("PAS-INM-202610-ABCD", result.getCodigoReserva());
        assertEquals(Constant.ID_MODALIDAD_INMEDIATO, result.getTipoModalidadPaseoId());
        assertEquals(Constant.ID_ESTADO_SOLICITUD_BUSCANDO, result.getEstadoId());
        assertEquals(new BigDecimal("50.00"), result.getMontoTotal());
        assertNull(result.getPaseadorUuid());
        assertNull(result.getPaseadorNombreCompleto());
        assertEquals(cliente.getUuid(), result.getClienteUuid());
        assertEquals("Carlos Mendoza", result.getClienteNombreCompleto());
        assertEquals(2, result.getMascotas().size());

        verify(solicitudPaseoRepository, times(1)).save(any(SolicitudPaseo.class));
        verify(solicitudPaseoMascotaRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("crearSolicitudInmediata - Falla si la mascota no pertenece al cliente")
    void testCrearSolicitudInmediata_MascotaAjena() {
        Cliente otroCliente = Cliente.builder().id(99L).build();
        Mascota mascotaAjena = Mascota.builder()
                .id(105L)
                .uuid(UUID.randomUUID().toString())
                .nombre("Rex")
                .cliente(otroCliente)
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();

        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(mascotaAjena.getUuid()))
                .distritoId(21)
                .tipoDuracionPaseoId(2)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Primavera 123")
                .build();

        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);
        when(mascotaRepository.findByUuid(mascotaAjena.getUuid())).thenReturn(mascotaAjena);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.crearSolicitudInmediata("cliente@paseape.com", req));

        assertEquals(MessageCodes.ResponseCodeBR03, ex.code);
        assertTrue(ex.getMessage().contains("no pertenece al cliente autenticado"));
    }

    @Test
    @DisplayName("crearSolicitudInmediata - Falla si la mascota está inactiva")
    void testCrearSolicitudInmediata_MascotaInactiva() {
        Mascota mascotaInactiva = Mascota.builder()
                .id(106L)
                .uuid(UUID.randomUUID().toString())
                .nombre("Toby")
                .cliente(cliente)
                .estado(Constant.ESTADO_LOGICO_INACTIVO)
                .build();

        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(mascotaInactiva.getUuid()))
                .distritoId(21)
                .tipoDuracionPaseoId(2)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Primavera 123")
                .build();

        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);
        when(mascotaRepository.findByUuid(mascotaInactiva.getUuid())).thenReturn(mascotaInactiva);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.crearSolicitudInmediata("cliente@paseape.com", req));

        assertEquals(MessageCodes.ResponseCodeBR04, ex.code);
        assertTrue(ex.getMessage().contains("no se encuentra activa"));
    }

    @Test
    @DisplayName("obtenerSolicitudesInmediatasDisponibles - Éxito retornando solicitudes en cobertura")
    void testObtenerSolicitudesInmediatasDisponibles_Exito() throws BadRequestException {
        when(usuarioRepository.findByCorreo("paseador@paseape.com")).thenReturn(paseadorUsuario);
        when(paseadorRepository.findByUsuarioId(20L)).thenReturn(paseador);

        SolicitudPaseo sp1 = SolicitudPaseo.builder()
                .id(601L)
                .uuid(UUID.randomUUID().toString())
                .codigoReserva("PAS-INM-202610-1111")
                .tipoModalidadPaseo(TipoModalidadPaseo.builder().id(Constant.ID_MODALIDAD_INMEDIATO).descripcion("INMEDIATO").build())
                .solicitudPaseoEstado(SolicitudPaseoEstado.builder().id(Constant.ID_ESTADO_SOLICITUD_BUSCANDO).descripcion("SOLICITADA_BUSCANDO").build())
                .distrito(DistritoLima.builder().id(21).descripcion("MIRAFLORES").build())
                .cliente(cliente)
                .creadoEl(LocalDateTime.now())
                .build();

        when(solicitudPaseoRepository.findDisponiblesByDistrito(Constant.ID_MODALIDAD_INMEDIATO, Constant.ID_ESTADO_SOLICITUD_BUSCANDO, 21))
                .thenReturn(List.of(sp1));

        SolicitudPaseoMascota spm = SolicitudPaseoMascota.builder()
                .id(701L)
                .solicitudPaseo(sp1)
                .mascota(mascota1)
                .build();

        when(solicitudPaseoMascotaRepository.findAllBySolicitudPaseoIdInWithDetails(List.of(601L)))
                .thenReturn(List.of(spm));

        List<SolicitudPaseoResDto> disponibles = service.obtenerSolicitudesInmediatasDisponibles("paseador@paseape.com");

        assertNotNull(disponibles);
        assertEquals(1, disponibles.size());
        assertEquals("PAS-INM-202610-1111", disponibles.get(0).getCodigoReserva());
        assertEquals(1, disponibles.get(0).getMascotas().size());
        assertEquals("Bobby", disponibles.get(0).getMascotas().get(0).getNombre());
    }

    @Test
    @DisplayName("tomarSolicitudInmediata - Éxito atómico y generación de notificación")
    void testTomarSolicitudInmediata_Exito() throws BadRequestException {
        String solicitudUuid = UUID.randomUUID().toString();

        when(usuarioRepository.findByCorreo("paseador@paseape.com")).thenReturn(paseadorUsuario);
        when(paseadorRepository.findByUsuarioId(20L)).thenReturn(paseador);

        when(solicitudPaseoRepository.adjudicarSolicitudInmediata(
                eq(solicitudUuid),
                eq(2L),
                eq(Constant.ID_ESTADO_SOLICITUD_ACEPTADA),
                eq(Constant.ID_ESTADO_SOLICITUD_BUSCANDO),
                any(LocalDateTime.class),
                eq("paseador@paseape.com")
        )).thenReturn(1);

        SolicitudPaseo spAceptada = SolicitudPaseo.builder()
                .id(801L)
                .uuid(solicitudUuid)
                .codigoReserva("PAS-INM-202610-2222")
                .tipoModalidadPaseo(TipoModalidadPaseo.builder().id(Constant.ID_MODALIDAD_INMEDIATO).descripcion("INMEDIATO").build())
                .solicitudPaseoEstado(SolicitudPaseoEstado.builder().id(Constant.ID_ESTADO_SOLICITUD_ACEPTADA).descripcion("ACEPTADA").build())
                .distrito(DistritoLima.builder().id(21).descripcion("MIRAFLORES").build())
                .cliente(cliente)
                .paseador(paseador)
                .creadoEl(LocalDateTime.now())
                .build();

        when(solicitudPaseoRepository.findByUuidWithDetails(solicitudUuid)).thenReturn(spAceptada);

        SolicitudPaseoMascota spm = SolicitudPaseoMascota.builder()
                .id(901L)
                .solicitudPaseo(spAceptada)
                .mascota(mascota1)
                .build();

        when(solicitudPaseoMascotaRepository.findAllBySolicitudPaseoIdWithDetails(801L))
                .thenReturn(List.of(spm));

        SolicitudPaseoResDto result = service.tomarSolicitudInmediata("paseador@paseape.com", solicitudUuid);

        assertNotNull(result);
        assertEquals(Constant.ID_ESTADO_SOLICITUD_ACEPTADA, result.getEstadoId());
        assertEquals(paseador.getUuid(), result.getPaseadorUuid());
        assertEquals("Luis García", result.getPaseadorNombreCompleto());

        ArgumentCaptor<Notificacion> notifCaptor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository, times(1)).save(notifCaptor.capture());
        Notificacion notifGuardada = notifCaptor.getValue();

        assertNotNull(notifGuardada);
        assertEquals(Constant.ID_TIPO_NOTIFICACION_SOLICITUD_ACEPTADA, notifGuardada.getTipoNotificacion().getId());
        assertEquals(clienteUsuario.getId(), notifGuardada.getDestinatarioUsuario().getId());
        assertEquals(paseadorUsuario.getId(), notifGuardada.getRemitenteUsuario().getId());
        assertTrue(notifGuardada.getTitulo().contains("confirmado"));
    }

    @Test
    @DisplayName("tomarSolicitudInmediata - Falla si la solicitud ya fue tomada por otro paseador (0 filas afectadas)")
    void testTomarSolicitudInmediata_RaceConditionYaTomada() {
        String solicitudUuid = UUID.randomUUID().toString();

        when(usuarioRepository.findByCorreo("paseador@paseape.com")).thenReturn(paseadorUsuario);
        when(paseadorRepository.findByUsuarioId(20L)).thenReturn(paseador);

        when(solicitudPaseoRepository.adjudicarSolicitudInmediata(
                eq(solicitudUuid),
                eq(2L),
                eq(Constant.ID_ESTADO_SOLICITUD_ACEPTADA),
                eq(Constant.ID_ESTADO_SOLICITUD_BUSCANDO),
                any(LocalDateTime.class),
                eq("paseador@paseape.com")
        )).thenReturn(0);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.tomarSolicitudInmediata("paseador@paseape.com", solicitudUuid));

        assertEquals(MessageCodes.ResponseCodeBR03, ex.code);
        assertEquals(Constant.MSJ_ERROR_SOLICITUD_NO_DISPONIBLE, ex.getMessage());
        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("crearSolicitudProgramada - Éxito con 1 bloque (60 min) y persistencia de notificación")
    void testCrearSolicitudProgramada_Exito_1Bloque() throws BadRequestException {
        LocalDate fechaFutura = LocalDate.now(ZoneId.of("America/Lima")).plusDays(3);
        BloqueHorario bloque = BloqueHorario.builder()
                .id(5)
                .codigo("B10_11")
                .orden(5)
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(11, 0))
                .build();

        var req = CrearSolicitudProgramadaReqDto.builder()
                .paseadorUuid(paseador.getUuid())
                .mascotaUuids(List.of(mascota1.getUuid(), mascota2.getUuid()))
                .distritoId(21)
                .fechaProgramada(fechaFutura)
                .bloqueHorarioId(5)
                .tipoDuracionPaseoId(2)
                .tipoMetodoPagoId(1)
                .direccionRecojo("Av. José Pardo 123")
                .referenciaDireccion("Frente al parque Kennedy")
                .build();

        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);
        when(mascotaRepository.findByUuid(mascota1.getUuid())).thenReturn(mascota1);
        when(mascotaRepository.findByUuid(mascota2.getUuid())).thenReturn(mascota2);
        when(paseadorRepository.findByUuid(paseador.getUuid())).thenReturn(paseador);
        when(bloqueHorarioRepository.findById(5)).thenReturn(bloque);
        when(tipoDuracionPaseoRepository.findById(2)).thenReturn(duracion60Min);

        int numDia = fechaFutura.getDayOfWeek().getValue();
        when(diaSemanaRepository.findByNumeroDia(numDia)).thenReturn(DiaSemana.builder().id(numDia).numeroDia(numDia).build());

        DisponibilidadPaseador disp = DisponibilidadPaseador.builder()
                .id(50L)
                .paseadorId(paseador.getId())
                .diaSemanaId(numDia)
                .bloqueHorarioId(5)
                .maxMascotas(3)
                .estado(true)
                .build();

        when(disponibilidadPaseadorRepository.findByPaseadorIdAndDiaSemanaIdAndBloqueHorarioId(2L, numDia, 5))
                .thenReturn(disp);
        when(solicitudPaseoRepository.countMascotasOcupadas(eq(2L), eq(fechaFutura), eq(5), anyList()))
                .thenReturn(0);

        when(solicitudPaseoRepository.save(any(SolicitudPaseo.class))).thenAnswer(invocation -> {
            SolicitudPaseo sp = invocation.getArgument(0);
            sp.setId(900L);
            return sp;
        });

        when(solicitudPaseoRepository.findByUuidWithDetails(anyString())).thenAnswer(invocation -> {
            String uuid = invocation.getArgument(0);
            return SolicitudPaseo.builder()
                    .id(900L)
                    .uuid(uuid)
                    .codigoReserva("PAS-PROG-202610-XYZ1")
                    .tipoModalidadPaseo(TipoModalidadPaseo.builder().id(Constant.ID_MODALIDAD_PROGRAMADO).descripcion("PROGRAMADO").build())
                    .tipoMetodoPago(TipoMetodoPago.builder().id(1).descripcion("YAPE / PLIN").build())
                    .tipoDuracionPaseo(duracion60Min)
                    .bloqueHorario(bloque)
                    .fechaProgramada(fechaFutura)
                    .solicitudPaseoEstado(SolicitudPaseoEstado.builder().id(Constant.ID_ESTADO_SOLICITUD_PENDIENTE).descripcion("PENDIENTE").build())
                    .distrito(DistritoLima.builder().id(21).descripcion("MIRAFLORES").build())
                    .direccionRecojo("Av. José Pardo 123")
                    .tarifaHoraPenSnapshot(new BigDecimal("25.00"))
                    .montoTotal(new BigDecimal("50.00")) // (25 / 60) * 60 * 2 = 50.00
                    .cliente(cliente)
                    .paseador(paseador)
                    .creadoEl(LocalDateTime.now())
                    .build();
        });

        SolicitudPaseoResDto result = service.crearSolicitudProgramada("cliente@paseape.com", req);

        assertNotNull(result);
        assertEquals("PAS-PROG-202610-XYZ1", result.getCodigoReserva());
        assertEquals(Constant.ID_MODALIDAD_PROGRAMADO, result.getTipoModalidadPaseoId());
        assertEquals(Constant.ID_ESTADO_SOLICITUD_PENDIENTE, result.getEstadoId());
        assertEquals(new BigDecimal("50.00"), result.getMontoTotal());
        assertEquals(paseador.getUuid(), result.getPaseadorUuid());
        assertEquals("Luis García", result.getPaseadorNombreCompleto());
        assertEquals(cliente.getUuid(), result.getClienteUuid());
        assertEquals("Carlos Mendoza", result.getClienteNombreCompleto());
        assertEquals(2, result.getMascotas().size());

        verify(solicitudPaseoRepository, times(1)).save(any(SolicitudPaseo.class));
        verify(solicitudPaseoMascotaRepository, times(1)).saveAll(anyList());

        ArgumentCaptor<Notificacion> notifCaptor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository, times(1)).save(notifCaptor.capture());
        Notificacion notif = notifCaptor.getValue();
        assertEquals(Constant.ID_TIPO_NOTIFICACION_SOLICITUD_RECIBIDA, notif.getTipoNotificacion().getId());
        assertEquals(paseadorUsuario.getId(), notif.getDestinatarioUsuario().getId());
        assertEquals(clienteUsuario.getId(), notif.getRemitenteUsuario().getId());
        assertTrue(notif.getTitulo().contains("recibida"));
    }

    @Test
    @DisplayName("crearSolicitudProgramada - Éxito con 2 bloques consecutivos (90 min)")
    void testCrearSolicitudProgramada_Exito_2Bloques() throws BadRequestException {
        LocalDate fechaFutura = LocalDate.now(ZoneId.of("America/Lima")).plusDays(2);
        BloqueHorario bloque1 = BloqueHorario.builder().id(4).codigo("B09_10").orden(4).horaInicio(LocalTime.of(9, 0)).horaFin(LocalTime.of(10, 0)).build();
        BloqueHorario bloque2 = BloqueHorario.builder().id(5).codigo("B10_11").orden(5).horaInicio(LocalTime.of(10, 0)).horaFin(LocalTime.of(11, 0)).build();

        TipoDuracionPaseo duracion90Min = TipoDuracionPaseo.builder()
                .id(3)
                .descripcion("90 MINUTOS")
                .minutos(90)
                .bloquesRequeridos(2)
                .build();

        var req = CrearSolicitudProgramadaReqDto.builder()
                .paseadorUuid(paseador.getUuid())
                .mascotaUuids(List.of(mascota1.getUuid()))
                .distritoId(21)
                .fechaProgramada(fechaFutura)
                .bloqueHorarioId(4)
                .tipoDuracionPaseoId(3)
                .tipoMetodoPagoId(1)
                .direccionRecojo("Av. Larco 456")
                .build();

        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);
        when(mascotaRepository.findByUuid(mascota1.getUuid())).thenReturn(mascota1);
        when(paseadorRepository.findByUuid(paseador.getUuid())).thenReturn(paseador);
        when(bloqueHorarioRepository.findById(4)).thenReturn(bloque1);
        when(bloqueHorarioRepository.findByOrden(5)).thenReturn(bloque2);
        when(tipoDuracionPaseoRepository.findById(3)).thenReturn(duracion90Min);

        int numDia = fechaFutura.getDayOfWeek().getValue();
        when(diaSemanaRepository.findByNumeroDia(numDia)).thenReturn(DiaSemana.builder().id(numDia).numeroDia(numDia).build());

        DisponibilidadPaseador disp1 = DisponibilidadPaseador.builder().id(51L).paseadorId(paseador.getId()).diaSemanaId(numDia).bloqueHorarioId(4).maxMascotas(3).estado(true).build();
        DisponibilidadPaseador disp2 = DisponibilidadPaseador.builder().id(52L).paseadorId(paseador.getId()).diaSemanaId(numDia).bloqueHorarioId(5).maxMascotas(3).estado(true).build();

        when(disponibilidadPaseadorRepository.findByPaseadorIdAndDiaSemanaIdAndBloqueHorarioId(2L, numDia, 4)).thenReturn(disp1);
        when(disponibilidadPaseadorRepository.findByPaseadorIdAndDiaSemanaIdAndBloqueHorarioId(2L, numDia, 5)).thenReturn(disp2);

        when(solicitudPaseoRepository.countMascotasOcupadas(eq(2L), eq(fechaFutura), eq(4), anyList())).thenReturn(0);
        when(solicitudPaseoRepository.countMascotasOcupadas(eq(2L), eq(fechaFutura), eq(5), anyList())).thenReturn(1); // 1 ocupada + 1 nueva <= 3

        when(solicitudPaseoRepository.save(any(SolicitudPaseo.class))).thenAnswer(invocation -> {
            SolicitudPaseo sp = invocation.getArgument(0);
            sp.setId(901L);
            return sp;
        });

        SolicitudPaseoResDto result = service.crearSolicitudProgramada("cliente@paseape.com", req);

        assertNotNull(result);
        verify(solicitudPaseoRepository, times(1)).save(any(SolicitudPaseo.class));
        verify(solicitudPaseoMascotaRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("crearSolicitudProgramada - Falla si fecha programada es anterior a hoy en Lima")
    void testCrearSolicitudProgramada_FalloFechaPasada() {
        LocalDate fechaPasada = LocalDate.now(ZoneId.of("America/Lima")).minusDays(1);
        var req = CrearSolicitudProgramadaReqDto.builder()
                .paseadorUuid(paseador.getUuid())
                .mascotaUuids(List.of(mascota1.getUuid()))
                .distritoId(21)
                .fechaProgramada(fechaPasada)
                .bloqueHorarioId(5)
                .tipoDuracionPaseoId(2)
                .tipoMetodoPagoId(1)
                .direccionRecojo("Av. Larco 456")
                .build();

        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);
        when(mascotaRepository.findByUuid(mascota1.getUuid())).thenReturn(mascota1);
        when(paseadorRepository.findByUuid(paseador.getUuid())).thenReturn(paseador);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.crearSolicitudProgramada("cliente@paseape.com", req));

        assertEquals(MessageCodes.ResponseCodeBR01, ex.code);
        assertTrue(ex.getMessage().contains("anterior a la fecha actual"));
    }

    @Test
    @DisplayName("crearSolicitudProgramada - Falla si es hoy y el bloque horario ya transcurrió")
    void testCrearSolicitudProgramada_FalloHoraPasadaMismoDia() {
        LocalDate hoyLima = LocalDate.now(ZoneId.of("America/Lima"));
        LocalTime horaPasada = LocalTime.now(ZoneId.of("America/Lima")).minusHours(2);
        BloqueHorario bloquePasado = BloqueHorario.builder()
                .id(1)
                .codigo("B06_07")
                .orden(1)
                .horaInicio(horaPasada)
                .horaFin(horaPasada.plusHours(1))
                .build();

        var req = CrearSolicitudProgramadaReqDto.builder()
                .paseadorUuid(paseador.getUuid())
                .mascotaUuids(List.of(mascota1.getUuid()))
                .distritoId(21)
                .fechaProgramada(hoyLima)
                .bloqueHorarioId(1)
                .tipoDuracionPaseoId(2)
                .tipoMetodoPagoId(1)
                .direccionRecojo("Av. Larco 456")
                .build();

        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);
        when(mascotaRepository.findByUuid(mascota1.getUuid())).thenReturn(mascota1);
        when(paseadorRepository.findByUuid(paseador.getUuid())).thenReturn(paseador);
        when(bloqueHorarioRepository.findById(1)).thenReturn(bloquePasado);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.crearSolicitudProgramada("cliente@paseape.com", req));

        assertEquals(MessageCodes.ResponseCodeBR01, ex.code);
        assertTrue(ex.getMessage().contains("ya inició o ha transcurrido"));
    }

    @Test
    @DisplayName("crearSolicitudProgramada - Falla si paseador no existe")
    void testCrearSolicitudProgramada_FalloPaseadorNoExiste() {
        String uuidInexistente = UUID.randomUUID().toString();
        var req = CrearSolicitudProgramadaReqDto.builder()
                .paseadorUuid(uuidInexistente)
                .mascotaUuids(List.of(mascota1.getUuid()))
                .distritoId(21)
                .fechaProgramada(LocalDate.now().plusDays(2))
                .bloqueHorarioId(5)
                .tipoDuracionPaseoId(2)
                .tipoMetodoPagoId(1)
                .direccionRecojo("Av. Larco 456")
                .build();

        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);
        when(mascotaRepository.findByUuid(mascota1.getUuid())).thenReturn(mascota1);
        when(paseadorRepository.findByUuid(uuidInexistente)).thenReturn(null);
        when(usuarioRepository.findByUuid(uuidInexistente)).thenReturn(null);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.crearSolicitudProgramada("cliente@paseape.com", req));

        assertEquals(MessageCodes.ResponseCodeBR98, ex.code);
        assertTrue(ex.getMessage().contains("especificado no existe"));
    }

    @Test
    @DisplayName("crearSolicitudProgramada - Falla si paseador no está verificado y aprobado")
    void testCrearSolicitudProgramada_FalloPaseadorNoAprobado() {
        Paseador paseadorPendiente = Paseador.builder()
                .id(99L)
                .uuid(UUID.randomUUID().toString())
                .distritoCobertura(new DistritoLima(21))
                .tarifaHoraPen(new BigDecimal("20.00"))
                .estadoVerificacion(PaseadorEstadoVerificacion.builder().id(Constant.ID_PENDIENTE).descripcion("PENDIENTE").build())
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();

        var req = CrearSolicitudProgramadaReqDto.builder()
                .paseadorUuid(paseadorPendiente.getUuid())
                .mascotaUuids(List.of(mascota1.getUuid()))
                .distritoId(21)
                .fechaProgramada(LocalDate.now().plusDays(2))
                .bloqueHorarioId(5)
                .tipoDuracionPaseoId(2)
                .tipoMetodoPagoId(1)
                .direccionRecojo("Av. Larco 456")
                .build();

        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);
        when(mascotaRepository.findByUuid(mascota1.getUuid())).thenReturn(mascota1);
        when(paseadorRepository.findByUuid(paseadorPendiente.getUuid())).thenReturn(paseadorPendiente);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.crearSolicitudProgramada("cliente@paseape.com", req));

        assertEquals(MessageCodes.ResponseCodeBR03, ex.code);
        assertTrue(ex.getMessage().contains("verificado y aprobado"));
    }

    @Test
    @DisplayName("crearSolicitudProgramada - Falla si distrito no coincide con cobertura del paseador")
    void testCrearSolicitudProgramada_FalloDistritoNoCobertura() {
        var req = CrearSolicitudProgramadaReqDto.builder()
                .paseadorUuid(paseador.getUuid())
                .mascotaUuids(List.of(mascota1.getUuid()))
                .distritoId(31) // San Isidro
                .fechaProgramada(LocalDate.now().plusDays(2))
                .bloqueHorarioId(5)
                .tipoDuracionPaseoId(2)
                .tipoMetodoPagoId(1)
                .direccionRecojo("Av. Javier Prado 123")
                .build();

        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);
        when(mascotaRepository.findByUuid(mascota1.getUuid())).thenReturn(mascota1);
        when(paseadorRepository.findByUuid(paseador.getUuid())).thenReturn(paseador);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.crearSolicitudProgramada("cliente@paseape.com", req));

        assertEquals(MessageCodes.ResponseCodeBR03, ex.code);
        assertTrue(ex.getMessage().contains("no coincide con la zona de cobertura"));
    }

    @Test
    @DisplayName("crearSolicitudProgramada - Falla si no hay bloque consecutivo para duración de 2 bloques")
    void testCrearSolicitudProgramada_FalloSinBloqueConsecutivo() {
        LocalDate fechaFutura = LocalDate.now(ZoneId.of("America/Lima")).plusDays(2);
        BloqueHorario ultimoBloque = BloqueHorario.builder().id(17).codigo("B22_23").orden(17).horaInicio(LocalTime.of(22, 0)).build();
        TipoDuracionPaseo duracion120Min = TipoDuracionPaseo.builder().id(4).minutos(120).bloquesRequeridos(2).build();

        var req = CrearSolicitudProgramadaReqDto.builder()
                .paseadorUuid(paseador.getUuid())
                .mascotaUuids(List.of(mascota1.getUuid()))
                .distritoId(21)
                .fechaProgramada(fechaFutura)
                .bloqueHorarioId(17)
                .tipoDuracionPaseoId(4)
                .tipoMetodoPagoId(1)
                .direccionRecojo("Av. Larco 456")
                .build();

        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);
        when(mascotaRepository.findByUuid(mascota1.getUuid())).thenReturn(mascota1);
        when(paseadorRepository.findByUuid(paseador.getUuid())).thenReturn(paseador);
        when(bloqueHorarioRepository.findById(17)).thenReturn(ultimoBloque);
        when(tipoDuracionPaseoRepository.findById(4)).thenReturn(duracion120Min);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.crearSolicitudProgramada("cliente@paseape.com", req));

        assertEquals(MessageCodes.ResponseCodeBR03, ex.code);
        assertTrue(ex.getMessage().contains("consecutivo disponible"));
    }

    @Test
    @DisplayName("crearSolicitudProgramada - Falla si el paseador no tiene disponibilidad activa en el bloque")
    void testCrearSolicitudProgramada_FalloSinDisponibilidadPaseador() {
        LocalDate fechaFutura = LocalDate.now(ZoneId.of("America/Lima")).plusDays(2);
        BloqueHorario bloque = BloqueHorario.builder().id(5).codigo("B10_11").orden(5).horaInicio(LocalTime.of(10, 0)).build();

        var req = CrearSolicitudProgramadaReqDto.builder()
                .paseadorUuid(paseador.getUuid())
                .mascotaUuids(List.of(mascota1.getUuid()))
                .distritoId(21)
                .fechaProgramada(fechaFutura)
                .bloqueHorarioId(5)
                .tipoDuracionPaseoId(2)
                .tipoMetodoPagoId(1)
                .direccionRecojo("Av. Larco 456")
                .build();

        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);
        when(mascotaRepository.findByUuid(mascota1.getUuid())).thenReturn(mascota1);
        when(paseadorRepository.findByUuid(paseador.getUuid())).thenReturn(paseador);
        when(bloqueHorarioRepository.findById(5)).thenReturn(bloque);
        when(tipoDuracionPaseoRepository.findById(2)).thenReturn(duracion60Min);

        int numDia = fechaFutura.getDayOfWeek().getValue();
        when(diaSemanaRepository.findByNumeroDia(numDia)).thenReturn(DiaSemana.builder().id(numDia).numeroDia(numDia).build());
        when(disponibilidadPaseadorRepository.findByPaseadorIdAndDiaSemanaIdAndBloqueHorarioId(2L, numDia, 5)).thenReturn(null);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.crearSolicitudProgramada("cliente@paseape.com", req));

        assertEquals(MessageCodes.ResponseCodeBR03, ex.code);
        assertTrue(ex.getMessage().contains("no tiene disponibilidad activa"));
    }

    @Test
    @DisplayName("crearSolicitudProgramada - Falla si el cupo del bloque está agotado")
    void testCrearSolicitudProgramada_FalloCupoAgotado() {
        LocalDate fechaFutura = LocalDate.now(ZoneId.of("America/Lima")).plusDays(2);
        BloqueHorario bloque = BloqueHorario.builder().id(5).codigo("B10_11").orden(5).horaInicio(LocalTime.of(10, 0)).build();

        var req = CrearSolicitudProgramadaReqDto.builder()
                .paseadorUuid(paseador.getUuid())
                .mascotaUuids(List.of(mascota1.getUuid(), mascota2.getUuid())) // 2 mascotas solicitadas
                .distritoId(21)
                .fechaProgramada(fechaFutura)
                .bloqueHorarioId(5)
                .tipoDuracionPaseoId(2)
                .tipoMetodoPagoId(1)
                .direccionRecojo("Av. Larco 456")
                .build();

        when(usuarioRepository.findByCorreo("cliente@paseape.com")).thenReturn(clienteUsuario);
        when(clienteRepository.findByUsuarioId(10L)).thenReturn(cliente);
        when(mascotaRepository.findByUuid(mascota1.getUuid())).thenReturn(mascota1);
        when(mascotaRepository.findByUuid(mascota2.getUuid())).thenReturn(mascota2);
        when(paseadorRepository.findByUuid(paseador.getUuid())).thenReturn(paseador);
        when(bloqueHorarioRepository.findById(5)).thenReturn(bloque);
        when(tipoDuracionPaseoRepository.findById(2)).thenReturn(duracion60Min);

        int numDia = fechaFutura.getDayOfWeek().getValue();
        when(diaSemanaRepository.findByNumeroDia(numDia)).thenReturn(DiaSemana.builder().id(numDia).numeroDia(numDia).build());

        DisponibilidadPaseador disp = DisponibilidadPaseador.builder()
                .id(50L)
                .paseadorId(2L)
                .diaSemanaId(numDia)
                .bloqueHorarioId(5)
                .maxMascotas(3)
                .estado(true)
                .build();

        when(disponibilidadPaseadorRepository.findByPaseadorIdAndDiaSemanaIdAndBloqueHorarioId(2L, numDia, 5)).thenReturn(disp);
        when(solicitudPaseoRepository.countMascotasOcupadas(eq(2L), eq(fechaFutura), eq(5), anyList())).thenReturn(2); // 2 ocupadas + 2 nuevas = 4 > 3

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.crearSolicitudProgramada("cliente@paseape.com", req));

        assertEquals(MessageCodes.ResponseCodeBR03, ex.code);
        assertTrue(ex.getMessage().contains("no cuenta con cupo suficiente"));
    }
}
