package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.INotificacionRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Notificacion;
import com.paseape.apipaseape.domain.entity.TipoNotificacion;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.dto.response.NotificacionResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.INotificacionDtoMapper;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private INotificacionRepository notificacionRepository;
    @Mock
    private IUsuarioRepository usuarioRepository;

    private INotificacionDtoMapper notificacionDtoMapper;
    private NotificacionService service;

    @BeforeEach
    void setUp() {
        notificacionDtoMapper = Mappers.getMapper(INotificacionDtoMapper.class);
        service = new NotificacionService(notificacionRepository, usuarioRepository, notificacionDtoMapper);
    }

    @Test
    @DisplayName("listarNotificacionesNoLeidas - Convierte creadoEl y fechaLeido de UTC a hora Lima")
    void testListarNotificacionesNoLeidas_ConvierteFechasALima() throws BadRequestException {
        String correo = "cliente@paseape.com";
        Usuario destinatario = Usuario.builder().id(7L).uuid(UUID.randomUUID().toString()).correo(correo).build();
        Usuario remitente = Usuario.builder().id(4L).uuid(UUID.randomUUID().toString()).nombres("Marcos").apellidos("Perez").build();

        Notificacion notificacion = Notificacion.builder()
                .id(66L)
                .uuid(UUID.randomUUID().toString())
                .remitenteUsuario(remitente)
                .destinatarioUsuario(destinatario)
                .tipoNotificacion(TipoNotificacion.builder().id(5).descripcion("PASEO_FINALIZADO").build())
                .titulo("¡Tu paseo ha finalizado!")
                .mensaje("Paseo finalizado.")
                .leido(false)
                .fechaLeido(null)
                .creadoEl(LocalDateTime.of(2026, 11, 19, 21, 27, 5))
                .build();

        when(usuarioRepository.findByCorreo(correo)).thenReturn(destinatario);
        when(notificacionRepository.findAllNoLeidasByDestinatarioUsuarioId(7L)).thenReturn(List.of(notificacion));

        List<NotificacionResDto> resultado = service.listarNotificacionesNoLeidas(correo);

        assertEquals(1, resultado.size());
        assertNull(resultado.get(0).getFechaLeido());
        assertEquals(LocalDateTime.of(2026, 11, 19, 16, 27, 5), resultado.get(0).getCreadoEl());
    }

    @Test
    @DisplayName("listarNotificacionesNoLeidas - Falla si el usuario autenticado no existe")
    void testListarNotificacionesNoLeidas_UsuarioNoExiste() {
        when(usuarioRepository.findByCorreo("noexiste@paseape.com")).thenReturn(null);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.listarNotificacionesNoLeidas("noexiste@paseape.com"));

        assertEquals(MessageCodes.ResponseCodeBR98, ex.code);
    }
}
