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
import com.paseape.apipaseape.domain.entity.TipoNotificacion;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.dto.request.RegistrarCalificacionReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.CalificacionResDto;
import com.paseape.apipaseape.infrastructure.dto.response.OpinionPaseadorResDto;
import com.paseape.apipaseape.infrastructure.dto.response.PageResponseDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResumenCalificacionesPaseadorResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.IPaseoCalificacionDtoMapper;
import com.paseape.apipaseape.infrastructure.util.UtilMethods;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalificacionService {

    private final IPaseoCalificacionRepository paseoCalificacionRepository;
    private final ISolicitudPaseoRepository solicitudPaseoRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IClienteRepository clienteRepository;
    private final IPaseadorRepository paseadorRepository;
    private final INotificacionRepository notificacionRepository;
    private final IPaseoCalificacionDtoMapper dtoMapper;

    private final Logger logger = LoggerFactory.getLogger(CalificacionService.class);

    @Transactional(rollbackFor = Exception.class)
    public CalificacionResDto registrarCalificacion(
            String userEmail,
            String solicitudUuid,
            RegistrarCalificacionReqDto reqDto
    ) throws BadRequestException {
        Cliente cliente = resolverClienteAutenticado(userEmail);
        SolicitudPaseo solicitud = resolverSolicitudFinalizadaDelCliente(solicitudUuid, cliente);

        if (solicitud.getPaseador() == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "La solicitud no tiene un paseador asignado.");
        }

        PaseoCalificacion existente = paseoCalificacionRepository.findBySolicitudPaseoId(solicitud.getId());
        if (existente != null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "Este paseo ya fue calificado anteriormente.");
        }

        Paseador paseador = paseadorRepository.findById(solicitud.getPaseador().getId());
        if (paseador == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el paseador del servicio.");
        }

        String comentarioLimpio = (reqDto.getComentario() != null && !reqDto.getComentario().trim().isEmpty())
                ? reqDto.getComentario().trim()
                : null;
        LocalDateTime fechaComentario = comentarioLimpio != null ? LocalDateTime.now(ZoneOffset.UTC) : null;

        PaseoCalificacion nueva = PaseoCalificacion.builder()
                .uuid(UUID.randomUUID().toString())
                .solicitudPaseo(solicitud)
                .cliente(cliente)
                .paseador(paseador)
                .puntuacion(reqDto.getPuntuacion())
                .comentario(comentarioLimpio)
                .fechaComentario(fechaComentario)
                .visible(Boolean.TRUE)
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();

        PaseoCalificacion guardada = paseoCalificacionRepository.save(nueva);

        paseador.registrarCalificacion(reqDto.getPuntuacion());
        paseadorRepository.save(paseador);

        notificarCalificacionRecibida(solicitud, cliente, paseador, reqDto.getPuntuacion());
        logger.info("Calificación {} ({} estrellas) registrada para solicitud {}", guardada.getUuid(), reqDto.getPuntuacion(), solicitud.getCodigoReserva());

        return toCalificacionResDtoLima(guardada);
    }

    @Transactional(readOnly = true)
    public ResumenCalificacionesPaseadorResDto obtenerCalificacionesPaseadorAutenticado(
            String userEmail,
            int page,
            int size
    ) throws BadRequestException {
        Usuario usuario = usuarioRepository.findByCorreo(userEmail.trim().toLowerCase());
        if (usuario == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el usuario autenticado.");
        }
        Paseador paseador = paseadorRepository.findByUsuarioId(usuario.getId());
        if (paseador == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El usuario no cuenta con perfil de paseador.");
        }
        return construirResumenCalificaciones(paseador, page, size);
    }

    @Transactional(readOnly = true)
    public ResumenCalificacionesPaseadorResDto obtenerCalificacionesPorPaseadorUuid(
            String paseadorUuid,
            int page,
            int size
    ) throws BadRequestException {
        String uuidLimpio = paseadorUuid.trim();
        Paseador paseador = paseadorRepository.findByUuid(uuidLimpio);
        if (paseador == null) {
            Usuario usuario = usuarioRepository.findByUuid(uuidLimpio);
            if (usuario != null) {
                paseador = paseadorRepository.findByUsuarioId(usuario.getId());
            }
        }
        if (paseador == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el paseador especificado.");
        }
        return construirResumenCalificaciones(paseador, page, size);
    }

    private ResumenCalificacionesPaseadorResDto construirResumenCalificaciones(Paseador paseador, int page, int size) {
        int total = paseador.getTotalCalificaciones() != null ? paseador.getTotalCalificaciones() : 0;
        BigDecimal promedio = paseador.getCalificacionPromedio() != null
                ? paseador.getCalificacionPromedio().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        if (total == 0) {
            PageResponseDto<OpinionPaseadorResDto> vacio = PageResponseDto.<OpinionPaseadorResDto>builder()
                    .items(List.of())
                    .page(page)
                    .size(size)
                    .totalElements(0L)
                    .totalPages(0)
                    .isFirst(true)
                    .isLast(true)
                    .hasNext(false)
                    .hasPrevious(false)
                    .build();
            return ResumenCalificacionesPaseadorResDto.builder()
                    .paseadorUuid(paseador.getUuid())
                    .calificacionPromedio(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .totalCalificaciones(0)
                    .tieneCalificaciones(Boolean.FALSE)
                    .mensajeEstado(Constant.MSJ_PASEADOR_SIN_CALIFICACIONES)
                    .opiniones(vacio)
                    .build();
        }

        Page<PaseoCalificacion> pageResult = paseoCalificacionRepository.findByPaseadorIdVisiblesPaginado(
                paseador.getId(),
                PageRequest.of(page, size)
        );
        List<OpinionPaseadorResDto> items = pageResult.getContent().stream()
                .map(this::toOpinionResDtoLima)
                .toList();

        PageResponseDto<OpinionPaseadorResDto> paginado = PageResponseDto.<OpinionPaseadorResDto>builder()
                .items(items)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .isFirst(pageResult.isFirst())
                .isLast(pageResult.isLast())
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .build();

        return ResumenCalificacionesPaseadorResDto.builder()
                .paseadorUuid(paseador.getUuid())
                .calificacionPromedio(promedio)
                .totalCalificaciones(total)
                .tieneCalificaciones(Boolean.TRUE)
                .mensajeEstado(Constant.MSJ_PASEADOR_CON_CALIFICACIONES)
                .opiniones(paginado)
                .build();
    }

    private Cliente resolverClienteAutenticado(String userEmail) throws BadRequestException {
        Usuario usuario = usuarioRepository.findByCorreo(userEmail.trim().toLowerCase());
        if (usuario == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el usuario autenticado.");
        }
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId());
        if (cliente == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El usuario no cuenta con perfil de cliente.");
        }
        return cliente;
    }

    private SolicitudPaseo resolverSolicitudFinalizadaDelCliente(String solicitudUuid, Cliente cliente) throws BadRequestException {
        SolicitudPaseo solicitud = solicitudPaseoRepository.findByUuidWithDetails(solicitudUuid.trim());
        if (solicitud == null) {
            solicitud = solicitudPaseoRepository.findByUuid(solicitudUuid.trim());
        }
        if (solicitud == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró la solicitud de paseo especificada.");
        }
        if (solicitud.getCliente() == null || !cliente.getId().equals(solicitud.getCliente().getId())) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "No tiene permisos para calificar esta solicitud de paseo.");
        }
        if (solicitud.getSolicitudPaseoEstado() == null
                || !Integer.valueOf(Constant.ID_ESTADO_SOLICITUD_FINALIZADA).equals(solicitud.getSolicitudPaseoEstado().getId())) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "Solo puedes calificar paseos que hayan finalizado.");
        }
        return solicitud;
    }

    private void notificarCalificacionRecibida(SolicitudPaseo solicitud, Cliente cliente, Paseador paseador, int puntuacion) {
        if (paseador.getUsuario() == null) {
            return;
        }
        String nombreCliente = resolverNombreCliente(cliente);
        Notificacion notificacion = Notificacion.builder()
                .uuid(UUID.randomUUID().toString())
                .remitenteUsuario(cliente.getUsuario())
                .destinatarioUsuario(paseador.getUsuario())
                .tipoNotificacion(TipoNotificacion.builder().id(Constant.ID_TIPO_NOTIFICACION_CALIFICACION_RECIBIDA).build())
                .titulo(Constant.TITULO_NOTIF_CALIFICACION_RECIBIDA)
                .mensaje(nombreCliente + " calificó tu paseo " + solicitud.getCodigoReserva() + " con " + puntuacion + " estrellas.")
                .referenciaId(solicitud.getId())
                .referenciaTipo(Constant.REFERENCIA_TIPO_SOLICITUD_PASEO)
                .leido(Boolean.FALSE)
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();
        notificacionRepository.save(notificacion);
    }

    private String resolverNombreCliente(Cliente cliente) {
        if (cliente == null || cliente.getUsuario() == null) {
            return "Un cliente";
        }
        String nombres = cliente.getUsuario().getNombres() != null ? cliente.getUsuario().getNombres().trim() : "";
        String apellidos = cliente.getUsuario().getApellidos() != null ? cliente.getUsuario().getApellidos().trim() : "";
        String full = (nombres + " " + apellidos).trim();
        return full.isEmpty() ? "Un cliente" : full;
    }

    private CalificacionResDto toCalificacionResDtoLima(PaseoCalificacion calificacion) {
        CalificacionResDto dto = dtoMapper.toCalificacionResDto(calificacion);
        if (dto != null) {
            dto.setCreadoEl(UtilMethods.convertirUtcALima(dto.getCreadoEl()));
            dto.setFechaComentario(UtilMethods.convertirUtcALima(dto.getFechaComentario()));
        }
        return dto;
    }

    private OpinionPaseadorResDto toOpinionResDtoLima(PaseoCalificacion calificacion) {
        OpinionPaseadorResDto dto = dtoMapper.toOpinionPaseadorResDto(calificacion);
        if (dto != null) {
            dto.setCreadoEl(UtilMethods.convertirUtcALima(dto.getCreadoEl()));
            dto.setFechaComentario(UtilMethods.convertirUtcALima(dto.getFechaComentario()));
        }
        return dto;
    }
}
