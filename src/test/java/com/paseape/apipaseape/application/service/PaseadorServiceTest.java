package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.IDiaSemanaRepository;
import com.paseape.apipaseape.application.repository.IDisponibilidadPaseadorRepository;
import com.paseape.apipaseape.application.repository.IPaseadorRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.BloqueHorario;
import com.paseape.apipaseape.domain.entity.DiaSemana;
import com.paseape.apipaseape.domain.entity.DisponibilidadPaseador;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.domain.entity.PaseadorEstadoVerificacion;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.dto.response.DisponibilidadPaseadorFechaResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.IDigitalDocumentDtoMapper;
import com.paseape.apipaseape.infrastructure.mapper.dto.IPaseadorDtoMapper;
import com.paseape.apipaseape.infrastructure.mapper.dto.IUsuarioDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaseadorServiceTest {

    @Mock
    private IPaseadorRepository paseadorRepository;
    @Mock
    private IUsuarioRepository usuarioRepository;
    @Mock
    private IPaseadorDtoMapper paseadorDtoMapper;
    @Mock
    private IUsuarioDtoMapper usuarioDtoMapper;
    @Mock
    private IDisponibilidadPaseadorRepository disponibilidadRepository;
    @Mock
    private ISolicitudPaseoRepository solicitudPaseoRepository;
    @Mock
    private IDiaSemanaRepository diaSemanaRepository;

    private PaseadorService service;

    private Paseador paseadorAprobado;

    private DigitalDocumentService digitalDocumentService;

    private IDigitalDocumentDtoMapper digitalDocumentDtoMapper;

    @BeforeEach
    void setUp() {
        service = new PaseadorService(
                digitalDocumentService,
                paseadorRepository,
                usuarioRepository,
                paseadorDtoMapper,
                usuarioDtoMapper,
                digitalDocumentDtoMapper,
                disponibilidadRepository,
                solicitudPaseoRepository,
                diaSemanaRepository
        );

        paseadorAprobado = Paseador.builder()
                .id(1L)
                .uuid(UUID.randomUUID().toString())
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .estadoVerificacion(PaseadorEstadoVerificacion.builder().id(Constant.ID_APROBADO).build())
                .maxMascotasPredeterminado(3)
                .build();
    }

    @Test
    @DisplayName("obtenerDisponibilidadPorFecha - Falla si paseadorUuid es nulo o vacío")
    void testObtenerDisponibilidadPorFecha_UuidInvalido() {
        assertThrows(BadRequestException.class, () -> service.obtenerDisponibilidadPorFecha(null, LocalDate.now()));
        assertThrows(BadRequestException.class, () -> service.obtenerDisponibilidadPorFecha("   ", LocalDate.now()));
    }

    @Test
    @DisplayName("obtenerDisponibilidadPorFecha - Falla si paseador no existe")
    void testObtenerDisponibilidadPorFecha_PaseadorNoExiste() {
        String uuid = UUID.randomUUID().toString();
        when(paseadorRepository.findByUuid(uuid)).thenReturn(null);
        when(usuarioRepository.findByUuid(uuid)).thenReturn(null);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.obtenerDisponibilidadPorFecha(uuid, LocalDate.now()));
        assertEquals(Constant.MSJ_ERROR_PASEADOR_NO_DISPONIBLE, ex.getMessage());
    }

    @Test
    @DisplayName("obtenerDisponibilidadPorFecha - Falla si paseador está inactivo o no aprobado")
    void testObtenerDisponibilidadPorFecha_PaseadorInactivoONoAprobado() {
        String uuid = UUID.randomUUID().toString();
        Paseador inactivo = Paseador.builder()
                .id(2L)
                .uuid(uuid)
                .estado(Constant.ESTADO_LOGICO_INACTIVO)
                .estadoVerificacion(PaseadorEstadoVerificacion.builder().id(Constant.ID_APROBADO).build())
                .build();
        when(paseadorRepository.findByUuid(uuid)).thenReturn(inactivo);

        assertThrows(BadRequestException.class, () -> service.obtenerDisponibilidadPorFecha(uuid, LocalDate.now()));

        Paseador noAprobado = Paseador.builder()
                .id(3L)
                .uuid(uuid)
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .estadoVerificacion(PaseadorEstadoVerificacion.builder().id(Constant.ID_PENDIENTE).build())
                .build();
        when(paseadorRepository.findByUuid(uuid)).thenReturn(noAprobado);

        assertThrows(BadRequestException.class, () -> service.obtenerDisponibilidadPorFecha(uuid, LocalDate.now()));
    }

    @Test
    @DisplayName("obtenerDisponibilidadPorFecha - Éxito para fecha futura con cupos libres y cupos agotados")
    void testObtenerDisponibilidadPorFecha_FechaFutura_Exito() throws BadRequestException {
        String uuid = paseadorAprobado.getUuid();
        LocalDate fechaFutura = LocalDate.now(ZoneId.of(Constant.ZONE_ID_LIMA)).plusDays(4);
        int numeroDia = fechaFutura.getDayOfWeek().getValue();

        when(paseadorRepository.findByUuid(uuid)).thenReturn(paseadorAprobado);
        when(diaSemanaRepository.findByNumeroDia(numeroDia))
                .thenReturn(DiaSemana.builder().id(numeroDia).descripcion("VIERNES").numeroDia(numeroDia).build());

        BloqueHorario bloque1 = BloqueHorario.builder()
                .id(1)
                .codigo("B08_09")
                .horaInicio(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(9, 0))
                .orden(1)
                .build();

        BloqueHorario bloque2 = BloqueHorario.builder()
                .id(2)
                .codigo("B09_10")
                .horaInicio(LocalTime.of(9, 0))
                .horaFin(LocalTime.of(10, 0))
                .orden(2)
                .build();

        DisponibilidadPaseador slot1 = DisponibilidadPaseador.builder()
                .id(101L)
                .paseadorId(paseadorAprobado.getId())
                .diaSemanaId(numeroDia)
                .bloqueHorarioId(1)
                .bloqueHorario(bloque1)
                .maxMascotas(3)
                .estado(true)
                .build();

        DisponibilidadPaseador slot2 = DisponibilidadPaseador.builder()
                .id(102L)
                .paseadorId(paseadorAprobado.getId())
                .diaSemanaId(numeroDia)
                .bloqueHorarioId(2)
                .bloqueHorario(bloque2)
                .maxMascotas(3)
                .estado(true)
                .build();

        when(disponibilidadRepository.findAllByPaseadorIdAndDiaSemanaId(paseadorAprobado.getId(), numeroDia))
                .thenReturn(List.of(slot1, slot2));

        // Bloque 1 tiene 1 ocupada (quedan 2 cupos)
        // Bloque 2 tiene 3 ocupadas (quedan 0 cupos -> SIN_CUPOS)
        when(solicitudPaseoRepository.countMascotasOcupadasAgrupadoPorBloque(eq(paseadorAprobado.getId()), eq(fechaFutura), anyList()))
                .thenReturn(Map.of(1, 1, 2, 3));

        DisponibilidadPaseadorFechaResDto response = service.obtenerDisponibilidadPorFecha(uuid, fechaFutura);

        assertNotNull(response);
        assertEquals(uuid, response.getPaseadorUuid());
        assertEquals(fechaFutura, response.getFecha());
        assertEquals(numeroDia, response.getDiaSemanaId());
        assertEquals("VIERNES", response.getDiaSemanaDescripcion());
        assertEquals(2, response.getBloques().size());

        var b1 = response.getBloques().get(0);
        assertEquals(1, b1.getBloqueHorarioId());
        assertEquals("B08_09", b1.getBloqueCodigo());
        assertEquals("08:00", b1.getHoraInicio());
        assertEquals("09:00", b1.getHoraFin());
        assertEquals(3, b1.getMaxMascotas());
        assertEquals(1, b1.getMascotasOcupadas());
        assertEquals(2, b1.getCuposDisponibles());
        assertFalse(b1.getEstaLleno());
        assertTrue(b1.getDisponible());
        assertNull(b1.getMotivoNoDisponible());

        var b2 = response.getBloques().get(1);
        assertEquals(2, b2.getBloqueHorarioId());
        assertEquals("B09_10", b2.getBloqueCodigo());
        assertEquals(3, b2.getMaxMascotas());
        assertEquals(3, b2.getMascotasOcupadas());
        assertEquals(0, b2.getCuposDisponibles());
        assertTrue(b2.getEstaLleno());
        assertFalse(b2.getDisponible());
        assertEquals(Constant.MOTIVO_SIN_CUPOS, b2.getMotivoNoDisponible());
    }

    @Test
    @DisplayName("obtenerDisponibilidadPorFecha - Éxito para fecha de hoy con descarte de bloques expirados")
    void testObtenerDisponibilidadPorFecha_FechaHoy_HorarioExpirado() throws BadRequestException {
        String uuid = paseadorAprobado.getUuid();
        LocalDate hoyLima = LocalDate.now(ZoneId.of(Constant.ZONE_ID_LIMA));
        int numeroDia = hoyLima.getDayOfWeek().getValue();

        when(paseadorRepository.findByUuid(uuid)).thenReturn(paseadorAprobado);
        when(diaSemanaRepository.findByNumeroDia(numeroDia))
                .thenReturn(DiaSemana.builder().id(numeroDia).descripcion("HOY").numeroDia(numeroDia).build());

        // Bloque en la madrugada ya expiró
        BloqueHorario bloqueMadrugada = BloqueHorario.builder()
                .id(1)
                .codigo("B00_01")
                .horaInicio(LocalTime.of(0, 0))
                .horaFin(LocalTime.of(1, 0))
                .orden(1)
                .build();

        // Bloque tarde en la noche
        BloqueHorario bloqueNoche = BloqueHorario.builder()
                .id(17)
                .codigo("B23_24")
                .horaInicio(LocalTime.of(23, 58))
                .horaFin(LocalTime.of(23, 59))
                .orden(17)
                .build();

        DisponibilidadPaseador slotMadrugada = DisponibilidadPaseador.builder()
                .id(201L)
                .paseadorId(paseadorAprobado.getId())
                .diaSemanaId(numeroDia)
                .bloqueHorarioId(1)
                .bloqueHorario(bloqueMadrugada)
                .maxMascotas(4)
                .estado(true)
                .build();

        DisponibilidadPaseador slotNoche = DisponibilidadPaseador.builder()
                .id(202L)
                .paseadorId(paseadorAprobado.getId())
                .diaSemanaId(numeroDia)
                .bloqueHorarioId(17)
                .bloqueHorario(bloqueNoche)
                .maxMascotas(4)
                .estado(true)
                .build();

        when(disponibilidadRepository.findAllByPaseadorIdAndDiaSemanaId(paseadorAprobado.getId(), numeroDia))
                .thenReturn(List.of(slotMadrugada, slotNoche));

        when(solicitudPaseoRepository.countMascotasOcupadasAgrupadoPorBloque(eq(paseadorAprobado.getId()), eq(hoyLima), anyList()))
                .thenReturn(Map.of());

        DisponibilidadPaseadorFechaResDto response = service.obtenerDisponibilidadPorFecha(uuid, hoyLima);

        assertNotNull(response);
        assertEquals(2, response.getBloques().size());

        var bMadrugada = response.getBloques().get(0);
        assertFalse(bMadrugada.getDisponible());
        assertEquals(Constant.MOTIVO_HORARIO_EXPIRADO, bMadrugada.getMotivoNoDisponible());

        var bNoche = response.getBloques().get(1);
        LocalTime ahoraLima = LocalTime.now(ZoneId.of(Constant.ZONE_ID_LIMA));
        if (bloqueNoche.getHoraInicio().isAfter(ahoraLima)) {
            assertTrue(bNoche.getDisponible());
            assertNull(bNoche.getMotivoNoDisponible());
        }
    }
}
