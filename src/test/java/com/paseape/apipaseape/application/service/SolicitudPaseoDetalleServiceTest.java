package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.IDigitalDocumentRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoMascotaRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoRepository;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.DigitalDocument;
import com.paseape.apipaseape.domain.entity.FolderDigitalDocument;
import com.paseape.apipaseape.domain.entity.Mascota;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.domain.entity.SolicitudPaseo;
import com.paseape.apipaseape.domain.entity.SolicitudPaseoEstado;
import com.paseape.apipaseape.domain.entity.SolicitudPaseoMascota;
import com.paseape.apipaseape.domain.entity.TipoRaza;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.dto.response.DocumentUrlResDto;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudMascotaDetalleResDto;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudPaseadorDetalleResDto;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudPaseoDetalleResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.ISolicitudPaseoDetalleDtoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudPaseoDetalleServiceTest {

    @Mock
    private ISolicitudPaseoRepository solicitudPaseoRepository;
    @Mock
    private ISolicitudPaseoMascotaRepository solicitudPaseoMascotaRepository;
    @Mock
    private IDigitalDocumentRepository digitalDocumentRepository;
    @Mock
    private DigitalDocumentService digitalDocumentService;
    @Spy
    private ISolicitudPaseoDetalleDtoMapper solicitudPaseoDetalleDtoMapper = Mappers.getMapper(ISolicitudPaseoDetalleDtoMapper.class);

    @InjectMocks
    private SolicitudPaseoDetalleService service;

    @Test
    @DisplayName("obtenerDetalle - Retorna detalle completo con mascotas (y foto) y paseador incluyendo su telefono")
    void testObtenerDetalle_ConPaseadorYMascotas() throws BadRequestException {
        String solicitudUuid = UUID.randomUUID().toString();
        String paseadorUuid = UUID.randomUUID().toString();
        String usuarioPaseadorUuid = UUID.randomUUID().toString();
        String mascotaUuid = UUID.randomUUID().toString();
        String docFotoUuid = UUID.randomUUID().toString();

        Usuario usuarioPaseador = Usuario.builder()
                .uuid(usuarioPaseadorUuid)
                .nombres("Carlos")
                .apellidos("Mendoza")
                .correo("carlos@paseape.com")
                .telefono("987654321")
                .fotoPerfilUrl("perfil-carlos.jpg")
                .build();

        Paseador paseador = Paseador.builder()
                .id(5L)
                .uuid(paseadorUuid)
                .usuario(usuarioPaseador)
                .experienciaAnos(3)
                .tarifaHoraPen(new BigDecimal("30.00"))
                .calificacionPromedio(new BigDecimal("4.9"))
                .paseosCompletados(42)
                .build();

        Usuario usuarioCliente = Usuario.builder().nombres("Ana").apellidos("Torres").build();
        Cliente cliente = Cliente.builder().uuid(UUID.randomUUID().toString()).usuario(usuarioCliente).build();

        SolicitudPaseo solicitud = SolicitudPaseo.builder()
                .id(100L)
                .uuid(solicitudUuid)
                .codigoReserva("PAS-PROG-202611-ABCD")
                .solicitudPaseoEstado(new SolicitudPaseoEstado(Constant.ID_ESTADO_SOLICITUD_PENDIENTE, Constant.ESTADO_SOLICITUD_PENDIENTE, null, 1, null, null, null, null))
                .cliente(cliente)
                .paseador(paseador)
                .build();

        Mascota mascota = Mascota.builder()
                .uuid(mascotaUuid)
                .nombre("Rocky")
                .tipoRaza(TipoRaza.builder().id(8).descripcion("Beagle").build())
                .edadAnos(2)
                .pesoKg(new BigDecimal("11.50"))
                .precaucionesMedicas("Ninguna")
                .folderDigitalDocument(FolderDigitalDocument.builder().id(55L).build())
                .build();

        SolicitudPaseoMascota spm = SolicitudPaseoMascota.builder().mascota(mascota).build();
        DigitalDocument docFoto = DigitalDocument.builder().id(77L).uuid(docFotoUuid).build();
        DocumentUrlResDto fotoResDto = DocumentUrlResDto.builder().url("par-url-rocky").nombreOriginal("rocky.jpg").build();

        when(solicitudPaseoRepository.findByUuidWithDetails(solicitudUuid)).thenReturn(solicitud);
        when(solicitudPaseoMascotaRepository.findAllBySolicitudPaseoIdWithDetails(100L)).thenReturn(List.of(spm));
        when(digitalDocumentRepository.findByFolderIdAndDigitalDocumentTypeId(55L, Constant.ID_DOC_FOTO_MASCOTA)).thenReturn(docFoto);
        when(digitalDocumentService.descargarDocumento(docFotoUuid)).thenReturn(fotoResDto);

        SolicitudPaseoDetalleResDto result = service.obtenerDetalle(solicitudUuid);

        assertNotNull(result);
        assertEquals(solicitudUuid, result.getUuid());
        assertEquals("Ana Torres", result.getClienteNombreCompleto());

        SolicitudPaseadorDetalleResDto paseadorDto = result.getPaseador();
        assertNotNull(paseadorDto);
        assertEquals(paseadorUuid, paseadorDto.getUuid());
        assertEquals("Carlos Mendoza", paseadorDto.getNombreCompleto());
        assertEquals("987654321", paseadorDto.getTelefono());
        assertEquals(42, paseadorDto.getPaseosCompletados());

        assertNotNull(result.getMascotas());
        assertEquals(1, result.getMascotas().size());
        SolicitudMascotaDetalleResDto mascotaDto = result.getMascotas().get(0);
        assertEquals(mascotaUuid, mascotaDto.getUuid());
        assertEquals("Rocky", mascotaDto.getNombre());
        assertEquals("Beagle", mascotaDto.getTipoRazaDescripcion());
        assertEquals("Ninguna", mascotaDto.getPrecaucionesMedicas());
        assertNotNull(mascotaDto.getFotoMascota());
        assertEquals("par-url-rocky", mascotaDto.getFotoMascota().getUrl());
    }

    @Test
    @DisplayName("obtenerDetalle - Retorna paseador null cuando la solicitud inmediata esta en busqueda")
    void testObtenerDetalle_SinPaseadorAsignado() throws BadRequestException {
        String solicitudUuid = UUID.randomUUID().toString();
        SolicitudPaseo solicitud = SolicitudPaseo.builder()
                .id(101L)
                .uuid(solicitudUuid)
                .paseador(null)
                .build();

        when(solicitudPaseoRepository.findByUuidWithDetails(solicitudUuid)).thenReturn(solicitud);
        when(solicitudPaseoMascotaRepository.findAllBySolicitudPaseoIdWithDetails(101L)).thenReturn(List.of());

        SolicitudPaseoDetalleResDto result = service.obtenerDetalle(solicitudUuid);

        assertNotNull(result);
        assertNull(result.getPaseador());
        assertTrue(result.getMascotas().isEmpty());
    }

    @Test
    @DisplayName("obtenerDetalle - Lanza BadRequestException cuando la solicitud no existe")
    void testObtenerDetalle_NoExiste() {
        String solicitudUuid = UUID.randomUUID().toString();
        when(solicitudPaseoRepository.findByUuidWithDetails(solicitudUuid)).thenReturn(null);

        assertThrows(BadRequestException.class, () -> service.obtenerDetalle(solicitudUuid));
        verify(solicitudPaseoMascotaRepository, never()).findAllBySolicitudPaseoIdWithDetails(any());
    }
}
