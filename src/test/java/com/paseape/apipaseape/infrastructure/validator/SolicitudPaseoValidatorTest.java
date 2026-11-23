package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.application.repository.IBloqueHorarioRepository;
import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IDistritoLimaRepository;
import com.paseape.apipaseape.application.repository.IPaseadorRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.BloqueHorario;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.DistritoLima;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.domain.entity.SolicitudPaseo;
import com.paseape.apipaseape.domain.entity.SolicitudPaseoEstado;
import com.paseape.apipaseape.domain.entity.TipoDuracionPaseo;
import com.paseape.apipaseape.domain.entity.TipoModalidadPaseo;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudInmediataReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudProgramadaReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.RegistrarUbicacionesBatchReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.UbicacionPuntoReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudPaseoValidatorTest {

    @Mock
    private IDistritoLimaRepository distritoLimaRepository;
    @Mock
    private IUsuarioRepository usuarioRepository;
    @Mock
    private IPaseadorRepository paseadorRepository;
    @Mock
    private IClienteRepository clienteRepository;
    @Mock
    private ISolicitudPaseoRepository solicitudPaseoRepository;
    @Mock
    private IBloqueHorarioRepository bloqueHorarioRepository;

    private SolicitudPaseoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SolicitudPaseoValidator(
                distritoLimaRepository,
                usuarioRepository,
                paseadorRepository,
                clienteRepository,
                solicitudPaseoRepository,
                bloqueHorarioRepository
        );
    }

    private SolicitudPaseoValidator crearValidatorConFechaHora(LocalDate fechaActual, LocalTime horaActual) {
        return new SolicitudPaseoValidator(
                distritoLimaRepository,
                usuarioRepository,
                paseadorRepository,
                clienteRepository,
                solicitudPaseoRepository,
                bloqueHorarioRepository
        ) {
            @Override
            protected LocalDate obtenerFechaActualLima() {
                return fechaActual;
            }

            @Override
            protected LocalTime obtenerHoraActualLima() {
                return horaActual;
            }
        };
    }

    @Test
    @DisplayName("validateCrearInmediata - Falla si el cuerpo es nulo")
    void testValidateCrearInmediata_NullBody() {
        ResponseDto<?> error = validator.validateCrearInmediata(null);
        assertNotNull(error);
        assertEquals(StatusCodes.Code400, error.getStatusCode());
        assertEquals(MessageCodes.ResponseCodeBR01, error.getCode());
        assertTrue(error.getMessage().contains("no puede estar vacío"));
    }

    @Test
    @DisplayName("validateCrearInmediata - Falla si lista de mascotas es nula o vacía")
    void testValidateCrearInmediata_EmptyMascotas() {
        var req1 = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(null)
                .distritoId(21)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Principal 123")
                .build();
        ResponseDto<?> err1 = validator.validateCrearInmediata(req1);
        assertNotNull(err1);
        assertTrue(err1.getMessage().contains("mascotas"));

        var req2 = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of())
                .distritoId(21)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Principal 123")
                .build();
        ResponseDto<?> err2 = validator.validateCrearInmediata(req2);
        assertNotNull(err2);
    }

    @Test
    @DisplayName("validateCrearInmediata - Falla si UUID de mascota tiene formato inválido")
    void testValidateCrearInmediata_InvalidMascotaUuid() {
        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of("invalid-uuid-format"))
                .distritoId(21)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Principal 123")
                .build();
        ResponseDto<?> error = validator.validateCrearInmediata(req);
        assertNotNull(error);
        assertTrue(error.getMessage().contains("no tiene un formato UUID válido"));
    }

    @Test
    @DisplayName("validateCrearInmediata - Falla si hay mascotas duplicadas")
    void testValidateCrearInmediata_DuplicateMascotas() {
        String uuid = UUID.randomUUID().toString();
        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(uuid, uuid))
                .distritoId(21)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Principal 123")
                .build();
        ResponseDto<?> error = validator.validateCrearInmediata(req);
        assertNotNull(error);
        assertTrue(error.getMessage().contains("duplicados"));
    }

    @Test
    @DisplayName("validateCrearInmediata - Falla si distritoId es inválido o no existe")
    void testValidateCrearInmediata_InvalidDistritoId() {
        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(UUID.randomUUID().toString()))
                .distritoId(0)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Principal 123")
                .build();
        ResponseDto<?> error = validator.validateCrearInmediata(req);
        assertNotNull(error);
        assertTrue(error.getMessage().contains("distrito"));

        var req2 = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(UUID.randomUUID().toString()))
                .distritoId(999)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Principal 123")
                .build();
        when(distritoLimaRepository.findById(999)).thenReturn(null);
        ResponseDto<?> error2 = validator.validateCrearInmediata(req2);
        assertNotNull(error2);
        assertTrue(error2.getMessage().contains("distrito"));
    }

    @Test
    @DisplayName("validateCrearInmediata - Falla si tarifaHoraPen es inválida")
    void testValidateCrearInmediata_InvalidTarifaHoraPen() {
        var req1 = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(UUID.randomUUID().toString()))
                .distritoId(21)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(null)
                .direccionRecojo("Av. Principal 123")
                .build();
        ResponseDto<?> err1 = validator.validateCrearInmediata(req1);
        assertNotNull(err1);
        assertTrue(err1.getMessage().contains("tarifa_hora_pen"));

        var req2 = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(UUID.randomUUID().toString()))
                .distritoId(21)
                .tipoDuracionPaseoId(1)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(BigDecimal.ZERO)
                .direccionRecojo("Av. Principal 123")
                .build();
        ResponseDto<?> err2 = validator.validateCrearInmediata(req2);
        assertNotNull(err2);
        assertTrue(err2.getMessage().contains("tarifa_hora_pen"));
    }

    @Test
    @DisplayName("validateCrearInmediata - Éxito con datos válidos")
    void testValidateCrearInmediata_Success() {
        when(distritoLimaRepository.findById(21)).thenReturn(DistritoLima.builder().id(21).build());

        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                .distritoId(21)
                .tipoDuracionPaseoId(2)
                .tipoMetodoPagoId(1)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Calle Los Álamos 456")
                .referenciaDireccion("Frente al parque")
                .build();
        ResponseDto<?> error = validator.validateCrearInmediata(req);
        assertNull(error);
    }

    @Test
    @DisplayName("validateCrearProgramada - Éxito con todos los campos válidos")
    void testValidateCrearProgramada_Success() {
        when(distritoLimaRepository.findById(21)).thenReturn(DistritoLima.builder().id(21).build());

        var req = CrearSolicitudProgramadaReqDto.builder()
                .paseadorUuid(UUID.randomUUID().toString())
                .mascotaUuids(List.of(UUID.randomUUID().toString()))
                .distritoId(21)
                .fechaProgramada(java.time.LocalDate.now().plusDays(3))
                .bloqueHorarioId(5)
                .tipoDuracionPaseoId(2)
                .tipoMetodoPagoId(1)
                .direccionRecojo("Av. Angamos 1234")
                .referenciaDireccion("Cerca al parque")
                .build();
        ResponseDto<?> error = validator.validateCrearProgramada(req);
        assertNull(error);
    }

    @Test
    @DisplayName("validateUuid - Falla si uuid es nulo o formato inválido")
    void testValidateUuid() {
        assertNotNull(validator.validateUuid(null, "uuid"));
        assertNotNull(validator.validateUuid("   ", "uuid"));
        assertNotNull(validator.validateUuid("abc-123", "uuid"));
        assertNull(validator.validateUuid(UUID.randomUUID().toString(), "uuid"));
    }

    @Test
    @DisplayName("validateRegistrarUbicaciones - Valida lotes GPS y autenticación correctamente")
    void testValidateRegistrarUbicaciones() {
        String uuid = UUID.randomUUID().toString();

        assertNotNull(validator.validateRegistrarUbicaciones(null, uuid, null));
        assertNotNull(validator.validateRegistrarUbicaciones("   ", uuid, null));
        assertNotNull(validator.validateRegistrarUbicaciones("paseador@paseape.com", uuid, null));
        assertNotNull(validator.validateRegistrarUbicaciones("paseador@paseape.com", uuid, RegistrarUbicacionesBatchReqDto.builder().ubicaciones(List.of()).build()));

        var p = UbicacionPuntoReqDto.builder()
                .latitud(BigDecimal.valueOf(-12.046374))
                .longitud(BigDecimal.valueOf(-77.042793))
                .fechaHoraGps(LocalDateTime.now(ZoneOffset.UTC))
                .precisionM(BigDecimal.valueOf(5.0))
                .velocidadMs(BigDecimal.valueOf(1.2))
                .rumboGrados(BigDecimal.valueOf(180.0))
                .build();
        var reqValido = RegistrarUbicacionesBatchReqDto.builder().ubicaciones(List.of(p)).build();

        when(usuarioRepository.findByCorreo("paseador@paseape.com")).thenReturn(Usuario.builder().id(10L).build());
        when(paseadorRepository.findByUsuarioId(10L)).thenReturn(Paseador.builder().id(20L).estado(1).build());
        when(solicitudPaseoRepository.findByUuidWithDetails(uuid)).thenReturn(
                SolicitudPaseo.builder()
                        .id(100L)
                        .paseador(Paseador.builder().id(20L).build())
                        .solicitudPaseoEstado(new SolicitudPaseoEstado(Constant.ID_ESTADO_SOLICITUD_EN_CURSO))
                        .build()
        );

        assertNull(validator.validateRegistrarUbicaciones("paseador@paseape.com", uuid, reqValido));
    }

    @Test
    @DisplayName("validateObtenerUbicacionEnVivo - Valida que el paseo esté en curso y pertenezca al cliente")
    void testValidateObtenerUbicacionEnVivo() {
        String uuid = UUID.randomUUID().toString();
        String email = "cliente@paseape.com";

        // 1. Email o UUID inválidos
        assertNotNull(validator.validateObtenerUbicacionEnVivo(null, uuid));
        assertNotNull(validator.validateObtenerUbicacionEnVivo(email, "invalid-uuid"));

        // 2. Cliente no existe
        when(usuarioRepository.findByCorreo(email)).thenReturn(Usuario.builder().id(5L).build());
        when(clienteRepository.findByUsuarioId(5L)).thenReturn(null);
        assertNotNull(validator.validateObtenerUbicacionEnVivo(email, uuid));

        // 3. Solicitud no en curso (por ejemplo, ACEPTADA = 2)
        when(clienteRepository.findByUsuarioId(5L)).thenReturn(Cliente.builder().id(15L).estado(1).build());
        when(solicitudPaseoRepository.findByUuidWithDetails(uuid)).thenReturn(
                SolicitudPaseo.builder()
                        .id(100L)
                        .cliente(Cliente.builder().id(15L).build())
                        .solicitudPaseoEstado(new SolicitudPaseoEstado(Constant.ID_ESTADO_SOLICITUD_ACEPTADA))
                        .build()
        );
        ResponseDto<?> errNoIniciado = validator.validateObtenerUbicacionEnVivo(email, uuid);
        assertNotNull(errNoIniciado);
        assertTrue(errNoIniciado.getMessage().contains("no ha comenzado"));

        // 4. Éxito cuando está en estado EN_CURSO (5)
        when(solicitudPaseoRepository.findByUuidWithDetails(uuid)).thenReturn(
                SolicitudPaseo.builder()
                        .id(100L)
                        .cliente(Cliente.builder().id(15L).build())
                        .solicitudPaseoEstado(new SolicitudPaseoEstado(Constant.ID_ESTADO_SOLICITUD_EN_CURSO))
                        .build()
        );
        assertNull(validator.validateObtenerUbicacionEnVivo(email, uuid));
    }

    @Test
    @DisplayName("validateObtenerRecorridoConsolidado - Valida autorización y estado finalizado")
    void testValidateObtenerRecorridoConsolidado() {
        String uuid = UUID.randomUUID().toString();
        String email = "cliente@paseape.com";

        when(usuarioRepository.findByCorreo(email)).thenReturn(Usuario.builder().id(5L).build());

        // Solicitud no finalizada
        when(solicitudPaseoRepository.findByUuidWithDetails(uuid)).thenReturn(
                SolicitudPaseo.builder()
                        .id(100L)
                        .cliente(Cliente.builder().usuario(Usuario.builder().id(5L).build()).build())
                        .solicitudPaseoEstado(new SolicitudPaseoEstado(Constant.ID_ESTADO_SOLICITUD_EN_CURSO))
                        .build()
        );
        ResponseDto<?> errNoFin = validator.validateObtenerRecorridoConsolidado(email, uuid);
        assertNotNull(errNoFin);
        assertTrue(errNoFin.getMessage().contains("finalizado"));

        // Éxito cuando está finalizada
        when(solicitudPaseoRepository.findByUuidWithDetails(uuid)).thenReturn(
                SolicitudPaseo.builder()
                        .id(100L)
                        .cliente(Cliente.builder().usuario(Usuario.builder().id(5L).build()).build())
                        .solicitudPaseoEstado(new SolicitudPaseoEstado(Constant.ID_ESTADO_SOLICITUD_FINALIZADA))
                        .build()
        );
        assertNull(validator.validateObtenerRecorridoConsolidado(email, uuid));
    }

    @Test
    @DisplayName("validateFinalizarPaseo - Valida sesión, uuid, existencia de paseador y titularidad de la solicitud")
    void testValidateFinalizarPaseo() {
        String validUuid = UUID.randomUUID().toString();
        String email = "paseador@paseape.com";

        // 1. Email o UUID inválidos
        assertNotNull(validator.validateFinalizarPaseo(null, validUuid));
        assertNotNull(validator.validateFinalizarPaseo("", validUuid));
        assertNotNull(validator.validateFinalizarPaseo(email, null));
        assertNotNull(validator.validateFinalizarPaseo(email, "uuid-invalido"));

        // 2. Usuario no encontrado
        when(usuarioRepository.findByCorreo(email)).thenReturn(null);
        ResponseDto<?> errUser = validator.validateFinalizarPaseo(email, validUuid);
        assertNotNull(errUser);
        assertEquals(MessageCodes.ResponseCodeBR98, errUser.getCode());

        // 3. Paseador no registrado
        when(usuarioRepository.findByCorreo(email)).thenReturn(Usuario.builder().id(20L).build());
        when(paseadorRepository.findByUsuarioId(20L)).thenReturn(null);
        ResponseDto<?> errPaseador = validator.validateFinalizarPaseo(email, validUuid);
        assertNotNull(errPaseador);
        assertEquals(MessageCodes.ResponseCodeBR03, errPaseador.getCode());

        // 4. Paseador inactivo
        when(paseadorRepository.findByUsuarioId(20L)).thenReturn(Paseador.builder().id(30L).estado(0).build());
        ResponseDto<?> errInactivo = validator.validateFinalizarPaseo(email, validUuid);
        assertNotNull(errInactivo);
        assertEquals(MessageCodes.ResponseCodeBR04, errInactivo.getCode());

        // 5. Solicitud no encontrada
        when(paseadorRepository.findByUsuarioId(20L)).thenReturn(Paseador.builder().id(30L).estado(1).build());
        when(solicitudPaseoRepository.findByUuidWithDetails(validUuid)).thenReturn(null);
        when(solicitudPaseoRepository.findByUuid(validUuid)).thenReturn(null);
        ResponseDto<?> errSolNotFound = validator.validateFinalizarPaseo(email, validUuid);
        assertNotNull(errSolNotFound);
        assertEquals(MessageCodes.ResponseCodeBR98, errSolNotFound.getCode());

        // 6. Solicitud no pertenece al paseador
        when(solicitudPaseoRepository.findByUuidWithDetails(validUuid)).thenReturn(
                SolicitudPaseo.builder()
                        .id(100L)
                        .paseador(Paseador.builder().id(999L).build())
                        .build()
        );
        ResponseDto<?> errNotOwner = validator.validateFinalizarPaseo(email, validUuid);
        assertNotNull(errNotOwner);
        assertEquals(MessageCodes.ResponseCodeBR03, errNotOwner.getCode());

        // 7. Éxito
        when(solicitudPaseoRepository.findByUuidWithDetails(validUuid)).thenReturn(
                SolicitudPaseo.builder()
                        .id(100L)
                        .paseador(Paseador.builder().id(30L).build())
                        .build()
        );
        assertNull(validator.validateFinalizarPaseo(email, validUuid));
    }

    @Test
    @DisplayName("validateIniciarPaseo - Falla si el uuid es inválido o la solicitud no existe")
    void testValidateIniciarPaseo_UuidInvalidoONoExiste() {
        assertNotNull(validator.validateIniciarPaseo(null));
        assertNotNull(validator.validateIniciarPaseo("uuid-invalido"));

        String uuid = UUID.randomUUID().toString();
        when(solicitudPaseoRepository.findByUuidWithDetails(uuid)).thenReturn(null);
        when(solicitudPaseoRepository.findByUuid(uuid)).thenReturn(null);

        ResponseDto<?> errNotFound = validator.validateIniciarPaseo(uuid);
        assertNotNull(errNotFound);
        assertEquals(MessageCodes.ResponseCodeBR98, errNotFound.getCode());
    }

    @Test
    @DisplayName("validateIniciarPaseo - Solicitud inmediata pasa sin validar fecha ni rango horario")
    void testValidateIniciarPaseo_InmediataSinValidacionHoraria() {
        String uuid = UUID.randomUUID().toString();
        SolicitudPaseoValidator customValidator = crearValidatorConFechaHora(LocalDate.of(2026, 11, 19), LocalTime.of(23, 30));

        when(solicitudPaseoRepository.findByUuidWithDetails(uuid)).thenReturn(
                SolicitudPaseo.builder()
                        .id(100L)
                        .tipoModalidadPaseo(new TipoModalidadPaseo(Constant.ID_MODALIDAD_INMEDIATO))
                        .solicitudPaseoEstado(new SolicitudPaseoEstado(Constant.ID_ESTADO_SOLICITUD_ACEPTADA))
                        .fechaProgramada(LocalDate.of(2026, 11, 10))
                        .bloqueHorario(null)
                        .build()
        );

        assertNull(customValidator.validateIniciarPaseo(uuid));
    }

    @Test
    @DisplayName("validateIniciarPaseo - Solicitud programada falla si la fecha es futura o pasada")
    void testValidateIniciarPaseo_ProgramadaFechaIncorrecta() {
        String uuid = UUID.randomUUID().toString();
        LocalDate hoy = LocalDate.of(2026, 11, 19);
        SolicitudPaseoValidator customValidator = crearValidatorConFechaHora(hoy, LocalTime.of(10, 15));
        BloqueHorario bloque = BloqueHorario.builder()
                .id(5).orden(5).horaInicio(LocalTime.of(10, 0)).horaFin(LocalTime.of(11, 0)).build();

        // Fecha futura
        when(solicitudPaseoRepository.findByUuidWithDetails(uuid)).thenReturn(
                SolicitudPaseo.builder()
                        .id(101L)
                        .tipoModalidadPaseo(new TipoModalidadPaseo(Constant.ID_MODALIDAD_PROGRAMADO))
                        .solicitudPaseoEstado(new SolicitudPaseoEstado(Constant.ID_ESTADO_SOLICITUD_ACEPTADA))
                        .fechaProgramada(hoy.plusDays(1))
                        .bloqueHorario(bloque)
                        .build()
        );
        ResponseDto<?> errFutura = customValidator.validateIniciarPaseo(uuid);
        assertNotNull(errFutura);
        assertEquals(MessageCodes.ResponseCodeBR03, errFutura.getCode());
        assertTrue(errFutura.getMessage().contains("fecha programada"));

        // Fecha pasada
        when(solicitudPaseoRepository.findByUuidWithDetails(uuid)).thenReturn(
                SolicitudPaseo.builder()
                        .id(101L)
                        .tipoModalidadPaseo(new TipoModalidadPaseo(Constant.ID_MODALIDAD_PROGRAMADO))
                        .solicitudPaseoEstado(new SolicitudPaseoEstado(Constant.ID_ESTADO_SOLICITUD_ACEPTADA))
                        .fechaProgramada(hoy.minusDays(1))
                        .bloqueHorario(bloque)
                        .build()
        );
        ResponseDto<?> errPasada = customValidator.validateIniciarPaseo(uuid);
        assertNotNull(errPasada);
        assertEquals(MessageCodes.ResponseCodeBR03, errPasada.getCode());
        assertTrue(errPasada.getMessage().contains("expirado"));
    }

    @Test
    @DisplayName("validateIniciarPaseo - Solicitud programada de 1 bloque valida tolerancia de 10 min y límite superior")
    void testValidateIniciarPaseo_ProgramadaUnBloque() {
        String uuid = UUID.randomUUID().toString();
        LocalDate hoy = LocalDate.of(2026, 11, 19);
        BloqueHorario bloque = BloqueHorario.builder()
                .id(5).orden(5).horaInicio(LocalTime.of(10, 0)).horaFin(LocalTime.of(11, 0)).build();
        SolicitudPaseo solicitud = SolicitudPaseo.builder()
                .id(102L)
                .tipoModalidadPaseo(new TipoModalidadPaseo(Constant.ID_MODALIDAD_PROGRAMADO))
                .solicitudPaseoEstado(new SolicitudPaseoEstado(Constant.ID_ESTADO_SOLICITUD_ACEPTADA))
                .fechaProgramada(hoy)
                .bloqueHorario(bloque)
                .tipoDuracionPaseo(TipoDuracionPaseo.builder().id(2).bloquesRequeridos(1).minutos(60).build())
                .build();
        when(solicitudPaseoRepository.findByUuidWithDetails(uuid)).thenReturn(solicitud);

        // 09:49 -> Antes de la tolerancia (10 min)
        ResponseDto<?> errAntes = crearValidatorConFechaHora(hoy, LocalTime.of(9, 49)).validateIniciarPaseo(uuid);
        assertNotNull(errAntes);
        assertEquals(MessageCodes.ResponseCodeBR03, errAntes.getCode());

        // 09:50 -> Justo en el límite de tolerancia
        assertNull(crearValidatorConFechaHora(hoy, LocalTime.of(9, 50)).validateIniciarPaseo(uuid));

        // 10:30 -> Dentro del bloque
        assertNull(crearValidatorConFechaHora(hoy, LocalTime.of(10, 30)).validateIniciarPaseo(uuid));

        // 11:01 -> Después del fin del bloque
        ResponseDto<?> errDespues = crearValidatorConFechaHora(hoy, LocalTime.of(11, 1)).validateIniciarPaseo(uuid);
        assertNotNull(errDespues);
        assertEquals(MessageCodes.ResponseCodeBR03, errDespues.getCode());
    }

    @Test
    @DisplayName("validateIniciarPaseo - Solicitud programada de 2 bloques permite iniciar hasta el fin del segundo bloque")
    void testValidateIniciarPaseo_ProgramadaDosBloques() {
        String uuid = UUID.randomUUID().toString();
        LocalDate hoy = LocalDate.of(2026, 11, 19);
        BloqueHorario bloque1 = BloqueHorario.builder()
                .id(5).orden(5).horaInicio(LocalTime.of(10, 0)).horaFin(LocalTime.of(11, 0)).build();
        BloqueHorario bloque2 = BloqueHorario.builder()
                .id(6).orden(6).horaInicio(LocalTime.of(11, 0)).horaFin(LocalTime.of(12, 0)).build();

        SolicitudPaseo solicitud = SolicitudPaseo.builder()
                .id(103L)
                .tipoModalidadPaseo(new TipoModalidadPaseo(Constant.ID_MODALIDAD_PROGRAMADO))
                .solicitudPaseoEstado(new SolicitudPaseoEstado(Constant.ID_ESTADO_SOLICITUD_ACEPTADA))
                .fechaProgramada(hoy)
                .bloqueHorario(bloque1)
                .tipoDuracionPaseo(TipoDuracionPaseo.builder().id(3).bloquesRequeridos(2).minutos(90).build())
                .build();
        when(solicitudPaseoRepository.findByUuidWithDetails(uuid)).thenReturn(solicitud);
        when(bloqueHorarioRepository.findByOrden(6)).thenReturn(bloque2);

        // 11:45 -> Dentro del segundo bloque
        assertNull(crearValidatorConFechaHora(hoy, LocalTime.of(11, 45)).validateIniciarPaseo(uuid));

        // 12:01 -> Después del fin del segundo bloque
        ResponseDto<?> errDespues = crearValidatorConFechaHora(hoy, LocalTime.of(12, 1)).validateIniciarPaseo(uuid);
        assertNotNull(errDespues);
        assertEquals(MessageCodes.ResponseCodeBR03, errDespues.getCode());
    }
}
