package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IMascotaRepository;
import com.paseape.apipaseape.application.repository.INotificacionRepository;
import com.paseape.apipaseape.application.repository.IPaseadorRepository;
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
import com.paseape.apipaseape.domain.entity.SolicitudPaseoEstado;
import com.paseape.apipaseape.domain.entity.SolicitudPaseoMascota;
import com.paseape.apipaseape.domain.entity.TipoDuracionPaseo;
import com.paseape.apipaseape.domain.entity.TipoMetodoPago;
import com.paseape.apipaseape.domain.entity.TipoModalidadPaseo;
import com.paseape.apipaseape.domain.entity.TipoNotificacion;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudInmediataReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudPaseoResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.ISolicitudPaseoDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
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

        BigDecimal tarifaSnapshot = Constant.TARIFA_BASE_INMEDIATO_HORA_PEN;
        BigDecimal minutos = BigDecimal.valueOf(tipoDuracion.getMinutos());
        BigDecimal totalMascotas = BigDecimal.valueOf(mascotas.size());
        BigDecimal montoTotal = tarifaSnapshot.multiply(minutos).multiply(totalMascotas)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        String yearMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String randomSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
        String codigoReserva = Constant.PREFIX_CODIGO_RESERVA_INMEDIATO + yearMonth + "-" + randomSuffix;

        LocalDateTime ahoraUtc = LocalDateTime.now(ZoneId.of("UTC"));

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
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .creadoEl(ahoraUtc)
                .creadoPor(userEmail.trim().toLowerCase())
                .actualizadoEl(ahoraUtc)
                .actualizadoPor(userEmail.trim().toLowerCase())
                .build();

        SolicitudPaseo solicitudGuardada = solicitudPaseoRepository.save(solicitud);

        List<SolicitudPaseoMascota> spmList = new ArrayList<>();
        for (Mascota m : mascotas) {
            SolicitudPaseoMascota spm = SolicitudPaseoMascota.builder()
                    .uuid(UUID.randomUUID().toString())
                    .solicitudPaseo(solicitudGuardada)
                    .mascota(m)
                    .estado(Constant.ESTADO_LOGICO_ACTIVO)
                    .creadoEl(ahoraUtc)
                    .creadoPor(userEmail.trim().toLowerCase())
                    .actualizadoEl(ahoraUtc)
                    .actualizadoPor(userEmail.trim().toLowerCase())
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

        LocalDateTime ahoraUtc = LocalDateTime.now(ZoneId.of("UTC"));

        int rowsUpdated = solicitudPaseoRepository.adjudicarSolicitudInmediata(
                solicitudUuid.trim(),
                paseador.getId(),
                Constant.ID_ESTADO_SOLICITUD_ACEPTADA,
                Constant.ID_ESTADO_SOLICITUD_BUSCANDO,
                ahoraUtc,
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
                    .creadoEl(ahoraUtc)
                    .creadoPor(userEmail.trim().toLowerCase())
                    .actualizadoEl(ahoraUtc)
                    .actualizadoPor(userEmail.trim().toLowerCase())
                    .build();
            notificacionRepository.save(notificacion);
        }

        List<SolicitudPaseoMascota> mascotas = solicitudPaseoMascotaRepository.findAllBySolicitudPaseoIdWithDetails(solicitudActualizada.getId());

        return solicitudPaseoDtoMapper.toResDto(solicitudActualizada, mascotas);
    }
}
