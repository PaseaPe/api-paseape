package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.IDistritoLimaRepository;
import com.paseape.apipaseape.application.repository.ITipoGeneroMascotaRepository;
import com.paseape.apipaseape.application.repository.ITipoMascotaRepository;
import com.paseape.apipaseape.application.repository.ITipoNivelEnergiaRepository;
import com.paseape.apipaseape.application.repository.ITipoRazaRepository;
import com.paseape.apipaseape.application.repository.ITipoTamanoMascotaRepository;
import com.paseape.apipaseape.domain.entity.DistritoLima;
import com.paseape.apipaseape.domain.entity.TipoMascota;
import com.paseape.apipaseape.domain.entity.TipoRaza;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.dto.request.catalogo.DistritoLimaReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.catalogo.TipoRazaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.catalogo.DistritoLimaAdminResDto;
import com.paseape.apipaseape.infrastructure.dto.response.catalogo.TipoRazaAdminResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.ICatalogoDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogoServiceTest {

    @Mock
    private IDistritoLimaRepository distritoLimaRepository;
    @Mock
    private ITipoMascotaRepository tipoMascotaRepository;
    @Mock
    private ITipoRazaRepository tipoRazaRepository;
    @Mock
    private ITipoTamanoMascotaRepository tipoTamanoMascotaRepository;
    @Mock
    private ITipoNivelEnergiaRepository tipoNivelEnergiaRepository;
    @Mock
    private ITipoGeneroMascotaRepository tipoGeneroMascotaRepository;

    private ICatalogoDtoMapper catalogoDtoMapper;
    private CatalogoService service;

    @BeforeEach
    void setUp() {
        catalogoDtoMapper = Mappers.getMapper(ICatalogoDtoMapper.class);
        service = new CatalogoService(
                distritoLimaRepository,
                tipoMascotaRepository,
                tipoRazaRepository,
                tipoTamanoMascotaRepository,
                tipoNivelEnergiaRepository,
                tipoGeneroMascotaRepository,
                catalogoDtoMapper
        );
    }

    @Test
    @DisplayName("listarDistritos - Convierte fechas UTC a Lima y filtra por estado cuando se envía")
    void testListarDistritos() {
        DistritoLima d1 = DistritoLima.builder()
                .id(1)
                .uuid(UUID.randomUUID().toString())
                .descripcion("Miraflores")
                .codigoUbigeo("150121")
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .creadoEl(LocalDateTime.of(2026, 11, 19, 21, 0, 0))
                .build();

        when(distritoLimaRepository.findAll()).thenReturn(List.of(d1));
        List<DistritoLimaAdminResDto> todos = service.listarDistritos(null);
        assertEquals(1, todos.size());
        assertEquals(LocalDateTime.of(2026, 11, 19, 16, 0, 0), todos.get(0).getCreadoEl());

        when(distritoLimaRepository.findAllByEstado(Constant.ESTADO_LOGICO_ACTIVO)).thenReturn(List.of(d1));
        List<DistritoLimaAdminResDto> activos = service.listarDistritos(Constant.ESTADO_LOGICO_ACTIVO);
        assertEquals(1, activos.size());
    }

    @Test
    @DisplayName("crearDistrito - Éxito y error por descripción duplicada")
    void testCrearDistrito() throws BadRequestException {
        DistritoLimaReqDto req = DistritoLimaReqDto.builder()
                .descripcion("Chorrillos")
                .codigoUbigeo("150108")
                .build();

        when(distritoLimaRepository.findByDescripcion("Chorrillos")).thenReturn(null);
        when(distritoLimaRepository.findByCodigoUbigeo("150108")).thenReturn(null);
        when(distritoLimaRepository.save(any(DistritoLima.class))).thenAnswer(i -> i.getArgument(0));

        DistritoLimaAdminResDto creado = service.crearDistrito(req);
        assertNotNull(creado.getUuid());
        assertEquals("Chorrillos", creado.getDescripcion());
        assertEquals(Constant.ESTADO_LOGICO_ACTIVO, creado.getEstado());

        when(distritoLimaRepository.findByDescripcion("Chorrillos")).thenReturn(DistritoLima.builder().id(99).build());
        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.crearDistrito(req));
        assertEquals(MessageCodes.ResponseCodeBR03, ex.code);
        verify(distritoLimaRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("eliminarDistrito y activarDistrito - Cambian estado lógico y validan estado actual")
    void testEliminarYActivarDistrito() throws BadRequestException {
        String uuid = UUID.randomUUID().toString();
        DistritoLima distrito = DistritoLima.builder()
                .id(21)
                .uuid(uuid)
                .descripcion("Miraflores")
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();

        when(distritoLimaRepository.findByUuid(uuid)).thenReturn(distrito);
        when(distritoLimaRepository.save(any(DistritoLima.class))).thenAnswer(i -> i.getArgument(0));

        DistritoLimaAdminResDto eliminado = service.eliminarDistrito(uuid);
        assertEquals(Constant.ESTADO_LOGICO_INACTIVO, eliminado.getEstado());

        // Intentar eliminar nuevamente cuando ya está inactivo
        BadRequestException exYaInactivo = assertThrows(BadRequestException.class, () -> service.eliminarDistrito(uuid));
        assertEquals(MessageCodes.ResponseCodeBR03, exYaInactivo.code);

        // Reactivar
        DistritoLimaAdminResDto activado = service.activarDistrito(uuid);
        assertEquals(Constant.ESTADO_LOGICO_ACTIVO, activado.getEstado());
    }

    @Test
    @DisplayName("crearRaza - Crea raza asociada a TipoMascota y rechaza duplicado por tipo")
    void testCrearRaza() throws BadRequestException {
        String tipoUuid = UUID.randomUUID().toString();
        TipoMascota perro = TipoMascota.builder().id(1).uuid(tipoUuid).descripcion("PERRO").estado(1).build();
        TipoRazaReqDto req = TipoRazaReqDto.builder().tipoMascotaUuid(tipoUuid).descripcion("Akita").build();

        when(tipoMascotaRepository.findByUuid(tipoUuid)).thenReturn(perro);
        when(tipoRazaRepository.findByDescripcion("Akita")).thenReturn(null);
        when(tipoRazaRepository.save(any(TipoRaza.class))).thenAnswer(i -> i.getArgument(0));

        TipoRazaAdminResDto res = service.crearRaza(req);
        assertEquals("Akita", res.getDescripcion());
        assertEquals(tipoUuid, res.getTipoMascotaUuid());
        assertEquals("PERRO", res.getTipoMascotaDescripcion());

        when(tipoRazaRepository.findByDescripcion("Akita"))
                .thenReturn(TipoRaza.builder().id(50).tipoMascota(perro).descripcion("Akita").build());
        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.crearRaza(req));
        assertEquals(MessageCodes.ResponseCodeBR03, ex.code);
    }
}
