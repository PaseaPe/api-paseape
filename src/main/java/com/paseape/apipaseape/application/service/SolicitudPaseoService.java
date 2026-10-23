package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IMascotaRepository;
import com.paseape.apipaseape.application.repository.INotificacionRepository;
import com.paseape.apipaseape.application.repository.IPaseadorRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoDescarteRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoMascotaRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoRepository;
import com.paseape.apipaseape.application.repository.ITipoDuracionPaseoRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.DistritoLima;
import com.paseape.apipaseape.domain.entity.Mascota;
import com.paseape.apipaseape.domain.entity.Notificacion;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.domain.entity.SolicitudPaseo;
import com.paseape.apipaseape.domain.entity.SolicitudPaseoDescarte;
import com.paseape.apipaseape.domain.entity.SolicitudPaseoEstado;
import com.paseape.apipaseape.domain.entity.SolicitudPaseoMascota;
import com.paseape.apipaseape.domain.entity.TipoDuracionPaseo;
import com.paseape.apipaseape.domain.entity.TipoMetodoPago;
import com.paseape.apipaseape.domain.entity.TipoModalidadPaseo;
import com.paseape.apipaseape.domain.entity.TipoNotificacion;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CancelarSolicitudReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudInmediataReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.RechazarSolicitudReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.PageResponseDto;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudPaseoResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.ISolicitudPaseoDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolicitudPaseoService {

    private final ISolicitudPaseoRepository solicitudPaseoRepository;
    private final ISolicitudPaseoMascotaRepository solicitudPaseoMascotaRepository;
    private final IClienteRepository clienteRepository;
    private final IPaseadorRepository paseadorRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IMascotaRepository mascotaRepository;
    private final ITipoDuracionPaseoRepository tipoDuracionPaseoRepository;
    private final INotificacionRepository notificacionRepository;
    private final ISolicitudPaseoDescarteRepository solicitudPaseoDescarteRepository;
    private final ISolicitudPaseoDtoMapper solicitudPaseoDtoMapper;

    @Transactional(rollbackFor = Exception.class)
    public SolicitudPaseoResDto crearSolicitudInmediata(String userEmail, CrearSolicitudInmediataReqDto reqDto) throws BadRequestException {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new BadRequestException(MessageCodes.ResponseCodeE01, "Usuario no autenticado.");
        }

        Usuario usuario = usuarioRepository.findByCorreo(userEmail.trim().toLowerCase());
        if (usuario == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el usuario autenticado.");
        }

        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId());
        if (cliente == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El usuario no cuenta con un perfil de cliente activo.");
        }

        List<Mascota> mascotas = new ArrayList<>();
        for (String mascotaUuid : reqDto.getMascotaUuids()) {
            Mascota mascota = mascotaRepository.findByUuid(mascotaUuid.trim());
            if (mascota == null) {
                throw new BadRequestException(MessageCodes.ResponseCodeBR98, "La mascota con identificador " + mascotaUuid + " no fue encontrada.");
            }
            if (mascota.getCliente() == null || !cliente.getId().equals(mascota.getCliente().getId())) {
                throw new BadRequestException(MessageCodes.ResponseCodeBR03, "La mascota '" + mascota.getNombre() + "' no pertenece al cliente autenticado.");
            }
            if (mascota.getEstado() == null || mascota.getEstado() != Constant.ESTADO_LOGICO_ACTIVO) {
                throw new BadRequestException(MessageCodes.ResponseCodeBR04, "La mascota '" + mascota.getNombre() + "' no se encuentra activa.");
            }
            mascotas.add(mascota);
        }

        TipoDuracionPaseo tipoDuracion = tipoDuracionPaseoRepository.findById(reqDto.getTipoDuracionPaseoId());
        if (tipoDuracion == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "El tipo de duración especificado no existe.");
        }

        BigDecimal tarifaSnapshot = reqDto.getTarifaHoraPen().setScale(2, RoundingMode.HALF_UP);
        BigDecimal minutos = BigDecimal.valueOf(tipoDuracion.getMinutos());
        BigDecimal totalMascotas = BigDecimal.valueOf(mascotas.size());
        BigDecimal montoTotal = tarifaSnapshot.multiply(minutos).multiply(totalMascotas)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        String yearMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String randomSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
        String codigoReserva = Constant.PREFIX_CODIGO_RESERVA_INMEDIATO + yearMonth + "-" + randomSuffix;

        LocalDateTime horaVencimiento = LocalDateTime.now().plusMinutes(5);

        SolicitudPaseo solicitud = SolicitudPaseo.builder()
                .uuid(UUID.randomUUID().toString())
                .codigoReserva(codigoReserva)
                .tipoModalidadPaseo(new TipoModalidadPaseo(Constant.ID_MODALIDAD_INMEDIATO))
                .tipoMetodoPago(new TipoMetodoPago(reqDto.getTipoMetodoPagoId()))
                .cliente(cliente)
                .paseador(null)
                .distrito(new DistritoLima(reqDto.getDistritoId()))
                .direccionRecojo(reqDto.getDireccionRecojo().trim())
                .referenciaDireccion(reqDto.getReferenciaDireccion() != null ? reqDto.getReferenciaDireccion().trim() : null)
                .fechaProgramada(LocalDate.now())
                .bloqueHorario(null)
                .tipoDuracionPaseo(new TipoDuracionPaseo(reqDto.getTipoDuracionPaseoId()))
                .tarifaHoraPenSnapshot(tarifaSnapshot)
                .montoTotal(montoTotal)
                .solicitudPaseoEstado(new SolicitudPaseoEstado(Constant.ID_ESTADO_SOLICITUD_BUSCANDO))
                .horaVencimiento(horaVencimiento)
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();

        SolicitudPaseo solicitudGuardada = solicitudPaseoRepository.save(solicitud);

        List<SolicitudPaseoMascota> spmList = new ArrayList<>();
        for (Mascota m : mascotas) {
            SolicitudPaseoMascota spm = SolicitudPaseoMascota.builder()
                    .uuid(UUID.randomUUID().toString())
                    .solicitudPaseo(solicitudGuardada)
                    .mascota(m)
                    .estado(Constant.ESTADO_LOGICO_ACTIVO)
                    .build();
            spmList.add(spm);
        }
        solicitudPaseoMascotaRepository.saveAll(spmList);

        SolicitudPaseo solicitudCompleta = solicitudPaseoRepository.findByUuidWithDetails(solicitudGuardada.getUuid());
        if (solicitudCompleta == null) {
            solicitudCompleta = solicitudGuardada;
        }

        return solicitudPaseoDtoMapper.toResDtoFromMascotas(solicitudCompleta, mascotas);
    }

    @Transactional(readOnly = true)
    public List<SolicitudPaseoResDto> obtenerSolicitudesInmediatasDisponibles(String userEmail) throws BadRequestException {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new BadRequestException(MessageCodes.ResponseCodeE01, "Usuario no autenticado.");
        }

        Usuario usuario = usuarioRepository.findByCorreo(userEmail.trim().toLowerCase());
        if (usuario == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el usuario autenticado.");
        }

        Paseador paseador = paseadorRepository.findByUsuarioId(usuario.getId());
        if (paseador == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El usuario no cuenta con un perfil de paseador registrado.");
        }

        if (paseador.getDistritoCobertura() == null || paseador.getDistritoCobertura().getId() == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El paseador no cuenta con un distrito de cobertura asignado.");
        }

        Integer distritoId = paseador.getDistritoCobertura().getId();
        List<SolicitudPaseo> solicitudes = solicitudPaseoRepository.findDisponiblesByDistrito(
                Constant.ID_MODALIDAD_INMEDIATO,
                Constant.ID_ESTADO_SOLICITUD_BUSCANDO,
                distritoId
        );

        if (solicitudes == null || solicitudes.isEmpty()) {
            return List.of();
        }

        List<Long> solicitudIds = solicitudes.stream()
                .map(SolicitudPaseo::getId)
                .toList();

        List<SolicitudPaseoMascota> todasMascotas = solicitudPaseoMascotaRepository.findAllBySolicitudPaseoIdInWithDetails(solicitudIds);
        Map<Long, List<SolicitudPaseoMascota>> mascotasPorSolicitud = todasMascotas.stream()
                .filter(spm -> spm.getSolicitudPaseo() != null && spm.getSolicitudPaseo().getId() != null)
                .collect(Collectors.groupingBy(spm -> spm.getSolicitudPaseo().getId()));

        return solicitudes.stream()
                .map(s -> {
                    List<SolicitudPaseoMascota> mascotasSolicitud = mascotasPorSolicitud.getOrDefault(s.getId(), List.of());
                    return solicitudPaseoDtoMapper.toResDto(s, mascotasSolicitud);
                })
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public SolicitudPaseoResDto tomarSolicitudInmediata(String userEmail, String solicitudUuid) throws BadRequestException {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new BadRequestException(MessageCodes.ResponseCodeE01, "Usuario no autenticado.");
        }

        Usuario usuario = usuarioRepository.findByCorreo(userEmail.trim().toLowerCase());
        if (usuario == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el usuario autenticado.");
        }

        Paseador paseador = paseadorRepository.findByUsuarioId(usuario.getId());
        if (paseador == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El usuario no cuenta con un perfil de paseador registrado.");
        }

        if (paseador.getEstado() != null && paseador.getEstado() != Constant.ESTADO_LOGICO_ACTIVO) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR04, "El paseador no se encuentra activo.");
        }

        LocalDateTime ahora = LocalDateTime.now();

        int rowsUpdated = solicitudPaseoRepository.adjudicarSolicitudInmediata(
                solicitudUuid.trim(),
                paseador.getId(),
                Constant.ID_ESTADO_SOLICITUD_ACEPTADA,
                Constant.ID_ESTADO_SOLICITUD_BUSCANDO,
                ahora,
                userEmail.trim().toLowerCase()
        );

        if (rowsUpdated == 0) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, Constant.MSJ_ERROR_SOLICITUD_NO_DISPONIBLE);
        }

        SolicitudPaseo solicitudActualizada = solicitudPaseoRepository.findByUuidWithDetails(solicitudUuid.trim());
        if (solicitudActualizada == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró la solicitud de paseo.");
        }

        String paseadorNombre = "";
        if (usuario.getNombres() != null) {
            paseadorNombre = (usuario.getNombres() + " " + (usuario.getApellidos() != null ? usuario.getApellidos() : "")).trim();
        } else if (paseador.getUsuario() != null) {
            paseadorNombre = (paseador.getUsuario().getNombres() + " " + (paseador.getUsuario().getApellidos() != null ? paseador.getUsuario().getApellidos() : "")).trim();
        }
        if (paseadorNombre.isEmpty()) {
            paseadorNombre = "Un paseador";
        }

        Usuario destinatario = solicitudActualizada.getCliente() != null ? solicitudActualizada.getCliente().getUsuario() : null;
        if (destinatario != null) {
            Notificacion notificacion = Notificacion.builder()
                    .uuid(UUID.randomUUID().toString())
                    .remitenteUsuario(usuario)
                    .destinatarioUsuario(destinatario)
                    .tipoNotificacion(new TipoNotificacion(Constant.ID_TIPO_NOTIFICACION_SOLICITUD_ACEPTADA))
                    .titulo(Constant.TITULO_NOTIF_SOLICITUD_ACEPTADA)
                    .mensaje("¡Buenas noticias! El paseador " + paseadorNombre + " ha aceptado tu solicitud de paseo inmediato " + solicitudActualizada.getCodigoReserva() + ".")
                    .referenciaId(solicitudActualizada.getId())
                    .referenciaTipo(Constant.REFERENCIA_TIPO_SOLICITUD_PASEO)
                    .leido(false)
                    .estado(Constant.ESTADO_LOGICO_ACTIVO)
                    .build();
            notificacionRepository.save(notificacion);
        }

        List<SolicitudPaseoMascota> mascotas = solicitudPaseoMascotaRepository.findAllBySolicitudPaseoIdWithDetails(solicitudActualizada.getId());

        return solicitudPaseoDtoMapper.toResDto(solicitudActualizada, mascotas);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<SolicitudPaseoResDto> obtenerBandejaPaseador(String userEmail, String tab, int page, int size) throws BadRequestException {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new BadRequestException(MessageCodes.ResponseCodeE01, "Usuario no autenticado.");
        }

        Usuario usuario = usuarioRepository.findByCorreo(userEmail.trim().toLowerCase());
        if (usuario == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el usuario autenticado.");
        }

        Paseador paseador = paseadorRepository.findByUsuarioId(usuario.getId());
        if (paseador == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El usuario no cuenta con un perfil de paseador registrado.");
        }

        if (paseador.getEstado() != null && paseador.getEstado() != Constant.ESTADO_LOGICO_ACTIVO) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR04, "El paseador no se encuentra activo.");
        }

        String tabUpper = tab != null ? tab.trim().toUpperCase() : Constant.TAB_INMEDIATAS;
        Page<SolicitudPaseo> pageResult;

        if (Constant.TAB_INMEDIATAS.equals(tabUpper)) {
            if (paseador.getDistritoCobertura() == null || paseador.getDistritoCobertura().getId() == null) {
                return PageResponseDto.<SolicitudPaseoResDto>builder()
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
            }

            Integer distritoId = paseador.getDistritoCobertura().getId();
            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.DESC, "creadoEl")
            );

            pageResult = solicitudPaseoRepository.findInmediatasDisponiblesPaginado(
                    Constant.ID_MODALIDAD_INMEDIATO,
                    Constant.ID_ESTADO_SOLICITUD_BUSCANDO,
                    distritoId,
                    paseador.getId(),
                    pageable
            );
        } else if (Constant.TAB_MIS_PASEOS.equals(tabUpper)) {
            List<Integer> estados = List.of(
                    Constant.ID_ESTADO_SOLICITUD_PENDIENTE,
                    Constant.ID_ESTADO_SOLICITUD_ACEPTADA
            );
            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.ASC, "fechaProgramada")
                            .and(Sort.by(Sort.Direction.DESC, "creadoEl"))
            );
            pageResult = solicitudPaseoRepository.findByPaseadorIdAndEstadosPaginado(paseador.getId(), estados, pageable);
        } else if (Constant.TAB_EN_CURSO.equals(tabUpper)) {
            List<Integer> estados = List.of(Constant.ID_ESTADO_SOLICITUD_EN_CURSO);
            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.DESC, "fechaHoraInicioReal")
                            .and(Sort.by(Sort.Direction.DESC, "creadoEl"))
            );
            pageResult = solicitudPaseoRepository.findByPaseadorIdAndEstadosPaginado(paseador.getId(), estados, pageable);
        } else if (Constant.TAB_HISTORIAL.equals(tabUpper)) {
            List<Integer> estados = List.of(
                    Constant.ID_ESTADO_SOLICITUD_RECHAZADA,
                    Constant.ID_ESTADO_SOLICITUD_CANCELADA,
                    Constant.ID_ESTADO_SOLICITUD_FINALIZADA,
                    Constant.ID_ESTADO_SOLICITUD_VENCIDA
            );
            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.DESC, "actualizadoEl")
                            .and(Sort.by(Sort.Direction.DESC, "creadoEl"))
            );
            pageResult = solicitudPaseoRepository.findByPaseadorIdAndEstadosPaginado(paseador.getId(), estados, pageable);
        } else {
            throw new BadRequestException(MessageCodes.ResponseCodeBR01, Constant.MSJ_ERROR_TAB_INVALIDO);
        }

        List<SolicitudPaseo> content = pageResult.getContent();
        if (content.isEmpty()) {
            return PageResponseDto.<SolicitudPaseoResDto>builder()
                    .items(List.of())
                    .page(pageResult.getNumber())
                    .size(pageResult.getSize())
                    .totalElements(pageResult.getTotalElements())
                    .totalPages(pageResult.getTotalPages())
                    .isFirst(pageResult.isFirst())
                    .isLast(pageResult.isLast())
                    .hasNext(pageResult.hasNext())
                    .hasPrevious(pageResult.hasPrevious())
                    .build();
        }

        List<Long> solicitudIds = content.stream().map(SolicitudPaseo::getId).toList();
        List<SolicitudPaseoMascota> todasMascotas = solicitudPaseoMascotaRepository.findAllBySolicitudPaseoIdInWithDetails(solicitudIds);
        Map<Long, List<SolicitudPaseoMascota>> mascotasPorSolicitud = todasMascotas.stream()
                .filter(spm -> spm.getSolicitudPaseo() != null && spm.getSolicitudPaseo().getId() != null)
                .collect(Collectors.groupingBy(spm -> spm.getSolicitudPaseo().getId()));

        List<SolicitudPaseoResDto> dtoList = content.stream()
                .map(s -> {
                    List<SolicitudPaseoMascota> mascotas = mascotasPorSolicitud.getOrDefault(s.getId(), List.of());
                    return solicitudPaseoDtoMapper.toResDto(s, mascotas);
                })
                .toList();

        return PageResponseDto.<SolicitudPaseoResDto>builder()
                .items(dtoList)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .isFirst(pageResult.isFirst())
                .isLast(pageResult.isLast())
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public void descartarSolicitudInmediata(String userEmail, String solicitudUuid) throws BadRequestException {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new BadRequestException(MessageCodes.ResponseCodeE01, "Usuario no autenticado.");
        }

        Usuario usuario = usuarioRepository.findByCorreo(userEmail.trim().toLowerCase());
        if (usuario == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el usuario autenticado.");
        }

        Paseador paseador = paseadorRepository.findByUsuarioId(usuario.getId());
        if (paseador == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El usuario no cuenta con un perfil de paseador registrado.");
        }

        SolicitudPaseo solicitud = solicitudPaseoRepository.findByUuid(solicitudUuid.trim());
        if (solicitud == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró la solicitud de paseo especificada.");
        }

        if (solicitud.getTipoModalidadPaseo() == null
                || !Integer.valueOf(Constant.ID_MODALIDAD_INMEDIATO).equals(solicitud.getTipoModalidadPaseo().getId())
                || solicitud.getSolicitudPaseoEstado() == null
                || !Integer.valueOf(Constant.ID_ESTADO_SOLICITUD_BUSCANDO).equals(solicitud.getSolicitudPaseoEstado().getId())) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, Constant.MSJ_ERROR_SOLICITUD_NO_DESCARTABLE);
        }

        if (solicitudPaseoDescarteRepository.existsBySolicitudPaseoIdAndPaseadorId(solicitud.getId(), paseador.getId())) {
            return;
        }

        SolicitudPaseoDescarte descarte = SolicitudPaseoDescarte.builder()
                .uuid(UUID.randomUUID().toString())
                .solicitudPaseo(solicitud)
                .paseador(paseador)
                .motivoDescarte(null)
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();

        solicitudPaseoDescarteRepository.save(descarte);
    }

    @Transactional(rollbackFor = Exception.class)
    public int expirarSolicitudesInmediatasVencidas() {
        LocalDateTime ahora = LocalDateTime.now();
        return solicitudPaseoRepository.expirarSolicitudesInmediatasVencidas(
                Constant.ID_MODALIDAD_INMEDIATO,
                Constant.ID_ESTADO_SOLICITUD_BUSCANDO,
                Constant.ID_ESTADO_SOLICITUD_VENCIDA,
                ahora
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public SolicitudPaseoResDto aceptarSolicitudProgramada(String userEmail, String solicitudUuid) throws BadRequestException {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new BadRequestException(MessageCodes.ResponseCodeE01, "Usuario no autenticado.");
        }

        Usuario usuario = usuarioRepository.findByCorreo(userEmail.trim().toLowerCase());
        if (usuario == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el usuario autenticado.");
        }

        Paseador paseador = paseadorRepository.findByUsuarioId(usuario.getId());
        if (paseador == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El usuario no cuenta con un perfil de paseador registrado.");
        }

        SolicitudPaseo solicitud = solicitudPaseoRepository.findByUuidWithDetails(solicitudUuid.trim());
        if (solicitud == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró la solicitud de paseo.");
        }

        if (solicitud.getPaseador() == null || !paseador.getId().equals(solicitud.getPaseador().getId())) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "La solicitud no pertenece al paseador autenticado.");
        }

        if (solicitud.getSolicitudPaseoEstado() == null
                || !Integer.valueOf(Constant.ID_ESTADO_SOLICITUD_PENDIENTE).equals(solicitud.getSolicitudPaseoEstado().getId())) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, Constant.MSJ_ERROR_SOLICITUD_NO_PENDIENTE);
        }

        solicitud.setSolicitudPaseoEstado(new SolicitudPaseoEstado(Constant.ID_ESTADO_SOLICITUD_ACEPTADA));
        solicitud.setFechaHoraAceptada(LocalDateTime.now());
        SolicitudPaseo guardada = solicitudPaseoRepository.save(solicitud);

        String paseadorNombre = obtenerNombreCompletoPaseador(usuario, paseador);
        Usuario destinatario = guardada.getCliente() != null ? guardada.getCliente().getUsuario() : null;
        if (destinatario != null) {
            Notificacion notificacion = Notificacion.builder()
                    .uuid(UUID.randomUUID().toString())
                    .remitenteUsuario(usuario)
                    .destinatarioUsuario(destinatario)
                    .tipoNotificacion(new TipoNotificacion(Constant.ID_TIPO_NOTIFICACION_SOLICITUD_ACEPTADA))
                    .titulo(Constant.TITULO_NOTIF_SOLICITUD_PROGRAMADA_ACEPTADA)
                    .mensaje("¡Buenas noticias! El paseador " + paseadorNombre + " ha confirmado tu solicitud de paseo programado " + guardada.getCodigoReserva() + ".")
                    .referenciaId(guardada.getId())
                    .referenciaTipo(Constant.REFERENCIA_TIPO_SOLICITUD_PASEO)
                    .leido(false)
                    .estado(Constant.ESTADO_LOGICO_ACTIVO)
                    .build();
            notificacionRepository.save(notificacion);
        }

        List<SolicitudPaseoMascota> mascotas = solicitudPaseoMascotaRepository.findAllBySolicitudPaseoIdWithDetails(guardada.getId());
        return solicitudPaseoDtoMapper.toResDto(guardada, mascotas);
    }

    @Transactional(rollbackFor = Exception.class)
    public SolicitudPaseoResDto rechazarSolicitudProgramada(String userEmail, String solicitudUuid, RechazarSolicitudReqDto reqDto) throws BadRequestException {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new BadRequestException(MessageCodes.ResponseCodeE01, "Usuario no autenticado.");
        }

        Usuario usuario = usuarioRepository.findByCorreo(userEmail.trim().toLowerCase());
        if (usuario == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el usuario autenticado.");
        }

        Paseador paseador = paseadorRepository.findByUsuarioId(usuario.getId());
        if (paseador == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El usuario no cuenta con un perfil de paseador registrado.");
        }

        SolicitudPaseo solicitud = solicitudPaseoRepository.findByUuidWithDetails(solicitudUuid.trim());
        if (solicitud == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró la solicitud de paseo.");
        }

        if (solicitud.getPaseador() == null || !paseador.getId().equals(solicitud.getPaseador().getId())) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "La solicitud no pertenece al paseador autenticado.");
        }

        if (solicitud.getSolicitudPaseoEstado() == null
                || !Integer.valueOf(Constant.ID_ESTADO_SOLICITUD_PENDIENTE).equals(solicitud.getSolicitudPaseoEstado().getId())) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, Constant.MSJ_ERROR_SOLICITUD_NO_PENDIENTE);
        }

        String motivo = (reqDto != null && StringUtils.hasText(reqDto.getMotivoRechazo()))
                ? reqDto.getMotivoRechazo().trim()
                : Constant.MOTIVO_RECHAZO_DEFAULT;

        solicitud.setSolicitudPaseoEstado(new SolicitudPaseoEstado(Constant.ID_ESTADO_SOLICITUD_RECHAZADA));
        solicitud.setMotivoRechazoCancelacion(motivo);
        SolicitudPaseo guardada = solicitudPaseoRepository.save(solicitud);

        String paseadorNombre = obtenerNombreCompletoPaseador(usuario, paseador);
        Usuario destinatario = guardada.getCliente() != null ? guardada.getCliente().getUsuario() : null;
        if (destinatario != null) {
            Notificacion notificacion = Notificacion.builder()
                    .uuid(UUID.randomUUID().toString())
                    .remitenteUsuario(usuario)
                    .destinatarioUsuario(destinatario)
                    .tipoNotificacion(new TipoNotificacion(Constant.ID_TIPO_NOTIFICACION_SOLICITUD_RECHAZADA))
                    .titulo(Constant.TITULO_NOTIF_SOLICITUD_PROGRAMADA_RECHAZADA)
                    .mensaje("El paseador " + paseadorNombre + " no pudo aceptar tu solicitud de paseo programado " + guardada.getCodigoReserva() + ". Motivo: " + motivo)
                    .referenciaId(guardada.getId())
                    .referenciaTipo(Constant.REFERENCIA_TIPO_SOLICITUD_PASEO)
                    .leido(false)
                    .estado(Constant.ESTADO_LOGICO_ACTIVO)
                    .build();
            notificacionRepository.save(notificacion);
        }

        List<SolicitudPaseoMascota> mascotas = solicitudPaseoMascotaRepository.findAllBySolicitudPaseoIdWithDetails(guardada.getId());
        return solicitudPaseoDtoMapper.toResDto(guardada, mascotas);
    }

    @Transactional(rollbackFor = Exception.class)
    public SolicitudPaseoResDto cancelarPaseoAceptado(String userEmail, String solicitudUuid, CancelarSolicitudReqDto reqDto) throws BadRequestException {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new BadRequestException(MessageCodes.ResponseCodeE01, "Usuario no autenticado.");
        }

        Usuario usuario = usuarioRepository.findByCorreo(userEmail.trim().toLowerCase());
        if (usuario == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el usuario autenticado.");
        }

        Paseador paseador = paseadorRepository.findByUsuarioId(usuario.getId());
        if (paseador == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El usuario no cuenta con un perfil de paseador registrado.");
        }

        SolicitudPaseo solicitud = solicitudPaseoRepository.findByUuidWithDetails(solicitudUuid.trim());
        if (solicitud == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró la solicitud de paseo.");
        }

        if (solicitud.getPaseador() == null || !paseador.getId().equals(solicitud.getPaseador().getId())) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "La solicitud no pertenece al paseador autenticado.");
        }

        if (solicitud.getSolicitudPaseoEstado() == null
                || !Integer.valueOf(Constant.ID_ESTADO_SOLICITUD_ACEPTADA).equals(solicitud.getSolicitudPaseoEstado().getId())
                || solicitud.getFechaHoraInicioReal() != null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, Constant.MSJ_ERROR_SOLICITUD_NO_CANCELABLE);
        }

        String motivo = (reqDto != null && StringUtils.hasText(reqDto.getMotivo()))
                ? reqDto.getMotivo().trim()
                : "";

        if (motivo.length() < 10) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR01, Constant.MSJ_ERROR_MOTIVO_CANCELACION_OBLIGATORIO);
        }

        solicitud.setSolicitudPaseoEstado(new SolicitudPaseoEstado(Constant.ID_ESTADO_SOLICITUD_CANCELADA));
        solicitud.setMotivoRechazoCancelacion(motivo);
        SolicitudPaseo guardada = solicitudPaseoRepository.save(solicitud);

        String paseadorNombre = obtenerNombreCompletoPaseador(usuario, paseador);
        Usuario destinatario = guardada.getCliente() != null ? guardada.getCliente().getUsuario() : null;
        if (destinatario != null) {
            Notificacion notificacion = Notificacion.builder()
                    .uuid(UUID.randomUUID().toString())
                    .remitenteUsuario(usuario)
                    .destinatarioUsuario(destinatario)
                    .tipoNotificacion(new TipoNotificacion(Constant.ID_TIPO_NOTIFICACION_SOLICITUD_CANCELADA))
                    .titulo(Constant.TITULO_NOTIF_SOLICITUD_CANCELADA_PASEADOR)
                    .mensaje("Lamentamos informarte que el paseador " + paseadorNombre + " ha cancelado el paseo " + guardada.getCodigoReserva() + ". Motivo: " + motivo)
                    .referenciaId(guardada.getId())
                    .referenciaTipo(Constant.REFERENCIA_TIPO_SOLICITUD_PASEO)
                    .leido(false)
                    .estado(Constant.ESTADO_LOGICO_ACTIVO)
                    .build();
            notificacionRepository.save(notificacion);
        }

        List<SolicitudPaseoMascota> mascotas = solicitudPaseoMascotaRepository.findAllBySolicitudPaseoIdWithDetails(guardada.getId());
        return solicitudPaseoDtoMapper.toResDto(guardada, mascotas);
    }

    private String obtenerNombreCompletoPaseador(Usuario usuario, Paseador paseador) {
        String paseadorNombre = "";
        if (usuario != null && usuario.getNombres() != null) {
            paseadorNombre = (usuario.getNombres() + " " + (usuario.getApellidos() != null ? usuario.getApellidos() : "")).trim();
        } else if (paseador != null && paseador.getUsuario() != null) {
            paseadorNombre = (paseador.getUsuario().getNombres() + " " + (paseador.getUsuario().getApellidos() != null ? paseador.getUsuario().getApellidos() : "")).trim();
        }
        return !paseadorNombre.isEmpty() ? paseadorNombre : "Un paseador";
    }
}
