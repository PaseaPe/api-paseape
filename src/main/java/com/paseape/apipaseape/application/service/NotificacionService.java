package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.INotificacionRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Notificacion;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.dto.response.NotificacionResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.INotificacionDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final INotificacionRepository notificacionRepository;
    private final IUsuarioRepository usuarioRepository;
    private final INotificacionDtoMapper notificacionDtoMapper;

    @Transactional(readOnly = true)
    public List<NotificacionResDto> listarNotificacionesNoLeidas(String userEmail) throws BadRequestException {
        Usuario usuario = obtenerUsuarioAutenticado(userEmail);
        List<Notificacion> notificaciones = notificacionRepository.findAllNoLeidasByDestinatarioUsuarioId(usuario.getId());
        return notificacionDtoMapper.toResDtoList(notificaciones);
    }

    @Transactional(rollbackFor = Exception.class)
    public void marcarTodasComoLeidas(String userEmail) throws BadRequestException {
        Usuario usuario = obtenerUsuarioAutenticado(userEmail);
        List<Notificacion> pendientes = notificacionRepository.findAllNoLeidasByDestinatarioUsuarioId(usuario.getId());
        if (pendientes.isEmpty()) {
            return;
        }

        LocalDateTime ahoraUtc = LocalDateTime.now(ZoneOffset.UTC);
        for (Notificacion notificacion : pendientes) {
            notificacion.setLeido(true);
            notificacion.setFechaLeido(ahoraUtc);
        }

        notificacionRepository.saveAll(pendientes);
    }

    private Usuario obtenerUsuarioAutenticado(String userEmail) throws BadRequestException {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new BadRequestException(MessageCodes.ResponseCodeE01, "Usuario no autenticado.");
        }

        Usuario usuario = usuarioRepository.findByCorreo(userEmail.trim().toLowerCase());
        if (usuario == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el usuario autenticado.");
        }
        return usuario;
    }
}