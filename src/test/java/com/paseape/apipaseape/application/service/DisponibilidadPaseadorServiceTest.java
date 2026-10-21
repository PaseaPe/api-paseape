package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.IBloqueHorarioRepository;
import com.paseape.apipaseape.application.repository.IDiaSemanaRepository;
import com.paseape.apipaseape.application.repository.IDisponibilidadPaseadorRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoRepository;
import com.paseape.apipaseape.domain.entity.BloqueHorario;
import com.paseape.apipaseape.domain.entity.DiaSemana;
import com.paseape.apipaseape.domain.entity.DisponibilidadPaseador;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.domain.entity.SolicitudPaseo;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarBloqueDisponibilidadReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarCapacidadBaseReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.DisponibilidadItemReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.GuardarDisponibilidadReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.DisponibilidadPaseadorResDto;
import com.paseape.apipaseape.infrastructure.dto.response.DisponibilidadSlotResDto;
import com.paseape.apipaseape.infrastructure.dto.response.PaseadorPerfilResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.IDisponibilidadPaseadorDtoMapper;
import com.paseape.apipaseape.infrastructure.mapper.dto.IPaseadorPerfilDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisponibilidadPaseadorServiceTest {

    @Mock
    private IDisponibilidadPaseadorRepository disponibilidadRepository;
    @Mock
    private IDiaSemanaRepository diaSemanaRepository;
    @Mock
    private IBloqueHorarioRepository bloqueHorarioRepository;
    @Mock
    private ISolicitudPaseoRepository solicitudPaseoRepository;
    @Mock
    private PaseadorService paseadorService;
    @Mock
    private IDisponibilidadPaseadorDtoMapper disponibilidadDtoMapper;
    @Mock
    private IPaseadorPerfilDtoMapper paseadorPerfilDtoMapper;

    @InjectMocks
    private DisponibilidadPaseadorService service;

    private Paseador mockPaseador;
    private DiaSemana mockDiaSemana;
    private BloqueHorario mockBloqueHorario;

    @BeforeEach
    void setUp() {
        mockPaseador = Paseador.builder()
                .id(10L)
                .uuid(UUID.randomUUID().toString())
                .maxMascotasPredeterminado(3)
                .build();

        mockDiaSemana = DiaSemana.builder()
                .id(1)
                .descripcion("LUNES")
                .numeroDia(1)
                .estado(true)
                .build();

        mockBloqueHorario = BloqueHorario.builder()
                .id(2)
                .codigo("B07_08")
                .horaInicio(LocalTime.of(7, 0))
                .horaFin(LocalTime.of(8, 0))
                .orden(2)
                .estado(true)
                .build();
    }

    @Test
    @DisplayName("obtenerMiDisponibilidad - Retorna DTO con slots ordenados")
    void testObtenerMiDisponibilidad() throws BadRequestException {
        when(paseadorService.obtenerPerfilPorUsuarioId(1L)).thenReturn(mockPaseador);

        var slot1 = DisponibilidadPaseador.builder().id(101L).diaSemanaId(2).bloqueHorario(mockBloqueHorario).build();
        var slot2 = DisponibilidadPaseador.builder().id(102L).diaSemanaId(1).bloqueHorario(mockBloqueHorario).build();
        List<DisponibilidadPaseador> slots = new ArrayList<>(List.of(slot1, slot2));

        when(disponibilidadRepository.findAllByPaseadorId(10L)).thenReturn(slots);
        when(disponibilidadDtoMapper.toResDto(eq(3), anyList()))
                .thenReturn(DisponibilidadPaseadorResDto.builder().maxMascotasPredeterminado(3).slots(List.of()).build());

        DisponibilidadPaseadorResDto result = service.obtenerMiDisponibilidad(1L);

        assertNotNull(result);
        assertEquals(3, result.getMaxMascotasPredeterminado());
        verify(disponibilidadRepository).findAllByPaseadorId(10L);
    }

    @Test
    @DisplayName("sincronizarDisponibilidad - Sincroniza correctamente insertando nuevos y desactivando desmarcados")
    void testSincronizarDisponibilidad() throws BadRequestException {
        when(paseadorService.obtenerPerfilPorUsuarioId(1L)).thenReturn(mockPaseador);

        // Existing slot that will be desmarcado (dia 1, bloque 2)
        var existingActiveSlot = DisponibilidadPaseador.builder()
                .id(201L)
                .paseadorId(10L)
                .diaSemanaId(1)
                .bloqueHorarioId(2)
                .estado(true)
                .build();

        // Existing inactive slot that will be reactivated (dia 2, bloque 3)
        var existingInactiveSlot = DisponibilidadPaseador.builder()
                .id(202L)
                .paseadorId(10L)
                .diaSemanaId(2)
                .bloqueHorarioId(3)
                .estado(false)
                .build();

        when(disponibilidadRepository.findAllByPaseadorIdIncludingInactive(10L))
                .thenReturn(new ArrayList<>(List.of(existingActiveSlot, existingInactiveSlot)));

        when(diaSemanaRepository.findById(3)).thenReturn(DiaSemana.builder().id(3).descripcion("MIÉRCOLES").numeroDia(3).build());
        when(bloqueHorarioRepository.findById(4)).thenReturn(BloqueHorario.builder().id(4).codigo("B09_10").orden(4).build());

        var reqDto = GuardarDisponibilidadReqDto.builder()
                .bloques(List.of(
                        // reactivate slot (dia 2, bloque 3) with custom capacity 4
                        DisponibilidadItemReqDto.builder().diaSemanaId(2).bloqueHorarioId(3).maxMascotas(4).build(),
                        // brand new slot (dia 3, bloque 4) with default capacity
                        DisponibilidadItemReqDto.builder().diaSemanaId(3).bloqueHorarioId(4).maxMascotas(null).build()
                ))
                .build();

        when(disponibilidadRepository.findAllByPaseadorId(10L)).thenReturn(List.of());
        when(disponibilidadDtoMapper.toResDto(eq(3), anyList()))
                .thenReturn(DisponibilidadPaseadorResDto.builder().maxMascotasPredeterminado(3).slots(List.of()).build());

        DisponibilidadPaseadorResDto res = service.sincronizarDisponibilidad(1L, reqDto);

        assertNotNull(res);
        verify(disponibilidadRepository).saveAll(argThat(list -> {
            // Should contain: reactivated (estado=true, max=4), new (estado=true, max=3), and unselected (estado=false)
            assertEquals(3, list.size());
            return true;
        }));
    }

    @Test
    @DisplayName("actualizarBloqueDisponibilidad - CANCELAR lanza BadRequestException si hay reservas futuras")
    void testActualizarBloque_CancelarConConflicto() throws BadRequestException {
        when(paseadorService.obtenerPerfilPorUsuarioId(1L)).thenReturn(mockPaseador);
        when(diaSemanaRepository.findById(1)).thenReturn(mockDiaSemana); // LUNES, numeroDia 1
        when(bloqueHorarioRepository.findById(2)).thenReturn(mockBloqueHorario);

        var existingSlot = DisponibilidadPaseador.builder()
                .id(301L)
                .paseadorId(10L)
                .diaSemanaId(1)
                .bloqueHorarioId(2)
                .estado(true)
                .build();

        when(disponibilidadRepository.findByPaseadorIdAndDiaSemanaIdAndBloqueHorarioIdIncludingInactive(10L, 1, 2))
                .thenReturn(existingSlot);

        // Next Monday date
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        var booking = SolicitudPaseo.builder()
                .id(501L)
                .fechaProgramada(nextMonday)
                .build();

        when(solicitudPaseoRepository.findFutureBookingsByPaseadorAndBloque(eq(10L), eq(2), any(LocalDate.class), anyList()))
                .thenReturn(List.of(booking));

        var req = ActualizarBloqueDisponibilidadReqDto.builder()
                .diaSemanaId(1)
                .bloqueHorarioId(2)
                .accion(Constant.ACCION_BLOQUE_CANCELAR)
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.actualizarBloqueDisponibilidad(1L, req)
        );

        assertEquals("No se puede cancelar el bloque porque tiene reservas programadas.", ex.getMessage());
        verify(disponibilidadRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizarBloqueDisponibilidad - CANCELAR exitoso si no hay reservas futuras")
    void testActualizarBloque_CancelarExitoso() throws BadRequestException {
        when(paseadorService.obtenerPerfilPorUsuarioId(1L)).thenReturn(mockPaseador);
        when(diaSemanaRepository.findById(1)).thenReturn(mockDiaSemana);
        when(bloqueHorarioRepository.findById(2)).thenReturn(mockBloqueHorario);

        var existingSlot = DisponibilidadPaseador.builder()
                .id(301L)
                .paseadorId(10L)
                .diaSemanaId(1)
                .bloqueHorarioId(2)
                .estado(true)
                .build();

        when(disponibilidadRepository.findByPaseadorIdAndDiaSemanaIdAndBloqueHorarioIdIncludingInactive(10L, 1, 2))
                .thenReturn(existingSlot);

        when(solicitudPaseoRepository.findFutureBookingsByPaseadorAndBloque(eq(10L), eq(2), any(LocalDate.class), anyList()))
                .thenReturn(List.of());

        when(disponibilidadRepository.save(any(DisponibilidadPaseador.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(disponibilidadDtoMapper.toSlotResDto(any(DisponibilidadPaseador.class)))
                .thenReturn(DisponibilidadSlotResDto.builder().id(301L).activo(false).build());

        var req = ActualizarBloqueDisponibilidadReqDto.builder()
                .diaSemanaId(1)
                .bloqueHorarioId(2)
                .accion(Constant.ACCION_BLOQUE_CANCELAR)
                .build();

        DisponibilidadSlotResDto res = service.actualizarBloqueDisponibilidad(1L, req);

        assertNotNull(res);
        assertFalse(res.getActivo());
        verify(disponibilidadRepository).save(argThat(slot -> Boolean.FALSE.equals(slot.getEstado())));
    }

    @Test
    @DisplayName("actualizarBloqueDisponibilidad - GUARDAR_CUPO actualiza o crea con cupo nuevo")
    void testActualizarBloque_GuardarCupo() throws BadRequestException {
        when(paseadorService.obtenerPerfilPorUsuarioId(1L)).thenReturn(mockPaseador);
        when(diaSemanaRepository.findById(1)).thenReturn(mockDiaSemana);
        when(bloqueHorarioRepository.findById(2)).thenReturn(mockBloqueHorario);

        when(disponibilidadRepository.findByPaseadorIdAndDiaSemanaIdAndBloqueHorarioIdIncludingInactive(10L, 1, 2))
                .thenReturn(null);

        when(disponibilidadRepository.save(any(DisponibilidadPaseador.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(disponibilidadDtoMapper.toSlotResDto(any(DisponibilidadPaseador.class)))
                .thenReturn(DisponibilidadSlotResDto.builder().maxMascotas(5).activo(true).build());

        var req = ActualizarBloqueDisponibilidadReqDto.builder()
                .diaSemanaId(1)
                .bloqueHorarioId(2)
                .accion(Constant.ACCION_BLOQUE_GUARDAR_CUPO)
                .maxMascotas(5)
                .build();

        DisponibilidadSlotResDto res = service.actualizarBloqueDisponibilidad(1L, req);

        assertNotNull(res);
        assertEquals(5, res.getMaxMascotas());
        verify(disponibilidadRepository).save(argThat(slot ->
                slot.getMaxMascotas() == 5 && Boolean.TRUE.equals(slot.getEstado())
        ));
    }

    @Test
    @DisplayName("actualizarBloqueDisponibilidad - REESTABLECER_PREDETERMINADO restaura a cupo base")
    void testActualizarBloque_Reestablecer() throws BadRequestException {
        when(paseadorService.obtenerPerfilPorUsuarioId(1L)).thenReturn(mockPaseador);
        when(diaSemanaRepository.findById(1)).thenReturn(mockDiaSemana);
        when(bloqueHorarioRepository.findById(2)).thenReturn(mockBloqueHorario);

        var existingSlot = DisponibilidadPaseador.builder()
                .id(301L)
                .paseadorId(10L)
                .diaSemanaId(1)
                .bloqueHorarioId(2)
                .maxMascotas(6)
                .estado(true)
                .build();

        when(disponibilidadRepository.findByPaseadorIdAndDiaSemanaIdAndBloqueHorarioIdIncludingInactive(10L, 1, 2))
                .thenReturn(existingSlot);

        when(disponibilidadRepository.save(any(DisponibilidadPaseador.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(disponibilidadDtoMapper.toSlotResDto(any(DisponibilidadPaseador.class)))
                .thenReturn(DisponibilidadSlotResDto.builder().maxMascotas(3).activo(true).build());

        var req = ActualizarBloqueDisponibilidadReqDto.builder()
                .diaSemanaId(1)
                .bloqueHorarioId(2)
                .accion(Constant.ACCION_BLOQUE_REESTABLECER)
                .build();

        DisponibilidadSlotResDto res = service.actualizarBloqueDisponibilidad(1L, req);

        assertNotNull(res);
        assertEquals(3, res.getMaxMascotas());
        verify(disponibilidadRepository).save(argThat(slot ->
                slot.getMaxMascotas() == 3 && Boolean.TRUE.equals(slot.getEstado())
        ));
    }

    @Test
    @DisplayName("actualizarCapacidadBase - Llama a paseadorService y mapea perfil actualizado")
    void testActualizarCapacidadBase() throws BadRequestException {
        when(paseadorService.actualizarCapacidadPredeterminada(1L, 5)).thenReturn(mockPaseador);
        when(paseadorPerfilDtoMapper.toResDto(mockPaseador))
                .thenReturn(PaseadorPerfilResDto.builder().uuid(mockPaseador.getUuid()).build());

        var req = ActualizarCapacidadBaseReqDto.builder().maxMascotasPredeterminado(5).build();
        PaseadorPerfilResDto res = service.actualizarCapacidadBase(1L, req);

        assertNotNull(res);
        verify(paseadorService).actualizarCapacidadPredeterminada(1L, 5);
    }
}
