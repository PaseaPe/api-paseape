package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.IBloqueHorarioRepository;
import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IDiaSemanaRepository;
import com.paseape.apipaseape.application.repository.IDisponibilidadPaseadorRepository;
import com.paseape.apipaseape.application.repository.IMascotaRepository;
import com.paseape.apipaseape.application.repository.INotificacionRepository;
import com.paseape.apipaseape.application.repository.IPaseadorRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoMascotaRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoRepository;
import com.paseape.apipaseape.application.repository.ITipoDuracionPaseoRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.BloqueHorario;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.DiaSemana;
import com.paseape.apipaseape.domain.entity.DisponibilidadPaseador;
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
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudProgramadaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudPaseoResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.ISolicitudPaseoDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final IBloqueHorarioRepository bloqueHorarioRepository;
    private final IDisponibilidadPaseadorRepository disponibilidadPaseadorRepository;
    private final IDiaSemanaRepository diaSemanaRepository;
    private final INotificacionRepository notificacionRepository;
    private final ISolicitudPaseoDtoMapper solicitudPaseoDtoMapper;

    @Transactional(rollbackFor = Exception.class)
    public SolicitudPaseoResDto crearSolicitudInmediata(String userEmail, CrearSolicitudInmediataReqDto reqDto) throws BadRequestException {
        Cliente cliente = validarYObtenerCliente(userEmail);
        List<Mascota> mascotas = validarYObtenerMascotas(cliente, reqDto.getMascotaUuids());

        TipoDuracionPaseo tipoDuracion = tipoDuracionPaseoRepository.findById(reqDto.getTipoDuracionPaseoId());
        if (tipoDuracion == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "El tipo de duración especificado no existe.");
        }

        BigDecimal tarifaSnapshot = reqDto.getTarifaHoraPen().setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoTotal = calcularMontoTotal(tarifaSnapshot, tipoDuracion.getMinutos(), mascotas.size());
        String codigoReserva = generarCodigoReserva(Constant.PREFIX_CODIGO_RESERVA_INMEDIATO);

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

    @Transactional(rollbackFor = Exception.class)
    public SolicitudPaseoResDto crearSolicitudProgramada(String userEmail, CrearSolicitudProgramadaReqDto reqDto) throws BadRequestException {
        Cliente cliente = validarYObtenerCliente(userEmail);
        List<Mascota> mascotas = validarYObtenerMascotas(cliente, reqDto.getMascotaUuids());

        // Paseador: buscar por UUID (paseador o usuario de paseador)
        Paseador paseador = paseadorRepository.findByUuid(reqDto.getPaseadorUuid().trim());
        if (paseador == null) {
            Usuario usuarioPaseador = usuarioRepository.findByUuid(reqDto.getPaseadorUuid().trim());
            if (usuarioPaseador != null) {
                paseador = paseadorRepository.findByUsuarioId(usuarioPaseador.getId());
            }
        }
        if (paseador == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "El paseador especificado no existe.");
        }
        if (paseador.getEstado() == null || !paseador.getEstado().equals(Constant.ESTADO_LOGICO_ACTIVO)) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR04, "El paseador no se encuentra activo.");
        }
        if (paseador.getEstadoVerificacion() == null || !Integer.valueOf(Constant.ID_APROBADO).equals(paseador.getEstadoVerificacion().getId())) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El paseador no se encuentra verificado y aprobado.");
        }
        if (paseador.getDistritoCobertura() == null || !reqDto.getDistritoId().equals(paseador.getDistritoCobertura().getId())) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El distrito seleccionado no coincide con la zona de cobertura del paseador.");
        }
        if (paseador.getTarifaHoraPen() == null || paseador.getTarifaHoraPen().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El paseador no tiene una tarifa horaria configurada.");
        }

        // Validación temporal civil contra America/Lima
        ZoneId limaZone = ZoneId.of("America/Lima");
        LocalDate hoyLima = LocalDate.now(limaZone);
        LocalTime ahoraLima = LocalTime.now(limaZone);

        if (reqDto.getFechaProgramada().isBefore(hoyLima)) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR01, "La fecha programada no puede ser anterior a la fecha actual.");
        }

        BloqueHorario bloqueInicial = bloqueHorarioRepository.findById(reqDto.getBloqueHorarioId());
        if (bloqueInicial == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "El bloque horario especificado no existe.");
        }

        if (reqDto.getFechaProgramada().isEqual(hoyLima)) {
            if (bloqueInicial.getHoraInicio() != null && !bloqueInicial.getHoraInicio().isAfter(ahoraLima)) {
                throw new BadRequestException(MessageCodes.ResponseCodeBR01, "No se puede programar en un bloque horario que ya inició o ha transcurrido.");
            }
        }

        // Duración y Bloques consecutivos (HU18)
        TipoDuracionPaseo tipoDuracion = tipoDuracionPaseoRepository.findById(reqDto.getTipoDuracionPaseoId());
        if (tipoDuracion == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "El tipo de duración especificado no existe.");
        }

        List<BloqueHorario> bloquesRequeridos = new ArrayList<>();
        bloquesRequeridos.add(bloqueInicial);

        if (tipoDuracion.getBloquesRequeridos() != null && tipoDuracion.getBloquesRequeridos() == 2) {
            int ordenSiguiente = bloqueInicial.getOrden() + 1;
            if (ordenSiguiente > 17) {
                throw new BadRequestException(MessageCodes.ResponseCodeBR03, "No hay un bloque horario consecutivo disponible para la duración solicitada.");
            }
            BloqueHorario bloqueSiguiente = bloqueHorarioRepository.findByOrden(ordenSiguiente);
            if (bloqueSiguiente == null) {
                throw new BadRequestException(MessageCodes.ResponseCodeBR03, "No hay un bloque horario consecutivo disponible para la duración solicitada.");
            }
            bloquesRequeridos.add(bloqueSiguiente);
        }

        // Disponibilidad y cupos (HU13 / HU16)
        int numeroDia = reqDto.getFechaProgramada().getDayOfWeek().getValue();
        DiaSemana diaSemana = diaSemanaRepository.findByNumeroDia(numeroDia);
        Integer diaSemanaId = diaSemana != null ? diaSemana.getId() : numeroDia;

        List<Integer> estadosOcupados = List.of(
                Constant.ID_ESTADO_SOLICITUD_PENDIENTE,
                Constant.ID_ESTADO_SOLICITUD_ACEPTADA,
                Constant.ID_ESTADO_SOLICITUD_EN_CURSO
        );

        int nuevasMascotas = mascotas.size();

        for (BloqueHorario bloque : bloquesRequeridos) {
            DisponibilidadPaseador disp = disponibilidadPaseadorRepository.findByPaseadorIdAndDiaSemanaIdAndBloqueHorarioId(
                    paseador.getId(),
                    diaSemanaId,
                    bloque.getId()
            );

            if (disp == null || !Boolean.TRUE.equals(disp.getEstado())) {
                throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El paseador no tiene disponibilidad activa para el bloque horario " + bloque.getCodigo() + " en la fecha seleccionada.");
            }

            int maxMascotasBloque = disp.getMaxMascotas() != null
                    ? disp.getMaxMascotas()
                    : (paseador.getMaxMascotasPredeterminado() != null
                            ? paseador.getMaxMascotasPredeterminado()
                            : Constant.CAPACIDAD_BASE_DEFAULT);

            Integer mascotasOcupadas = solicitudPaseoRepository.countMascotasOcupadas(
                    paseador.getId(),
                    reqDto.getFechaProgramada(),
                    bloque.getId(),
                    estadosOcupados
            );
            if (mascotasOcupadas == null) {
                mascotasOcupadas = 0;
            }

            if (mascotasOcupadas + nuevasMascotas > maxMascotasBloque) {
                throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El bloque horario " + bloque.getCodigo() + " no cuenta con cupo suficiente para la cantidad de mascotas solicitadas.");
            }
        }

        // Tarifa y snapshot
        BigDecimal tarifaSnapshot = paseador.getTarifaHoraPen().setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoTotal = calcularMontoTotal(tarifaSnapshot, tipoDuracion.getMinutos(), nuevasMascotas);

        // Código de reserva
        String codigoReserva = generarCodigoReserva(Constant.PREFIX_CODIGO_RESERVA_PROGRAMADO);

        LocalDateTime ahoraUtc = LocalDateTime.now(ZoneId.of("UTC"));

        SolicitudPaseo solicitud = SolicitudPaseo.builder()
                .uuid(UUID.randomUUID().toString())
                .codigoReserva(codigoReserva)
                .tipoModalidadPaseo(new TipoModalidadPaseo(Constant.ID_MODALIDAD_PROGRAMADO))
                .tipoMetodoPago(new TipoMetodoPago(reqDto.getTipoMetodoPagoId()))
                .cliente(cliente)
                .paseador(paseador)
                .distrito(new DistritoLima(reqDto.getDistritoId()))
                .direccionRecojo(reqDto.getDireccionRecojo().trim())
                .referenciaDireccion(StringUtils.hasText(reqDto.getReferenciaDireccion()) ? reqDto.getReferenciaDireccion().trim() : null)
                .fechaProgramada(reqDto.getFechaProgramada())
                .bloqueHorario(new BloqueHorario(bloqueInicial.getId()))
                .tipoDuracionPaseo(new TipoDuracionPaseo(tipoDuracion.getId()))
                .tarifaHoraPenSnapshot(tarifaSnapshot)
                .montoTotal(montoTotal)
                .solicitudPaseoEstado(new SolicitudPaseoEstado(Constant.ID_ESTADO_SOLICITUD_PENDIENTE))
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

        // Notificación transaccional para el paseador
        Usuario destinatario = paseador.getUsuario();

        if (destinatario != null) {
            Usuario remitente = cliente.getUsuario();
            Notificacion notificacion = Notificacion.builder()
                    .uuid(UUID.randomUUID().toString())
                    .remitenteUsuario(remitente)
                    .destinatarioUsuario(destinatario)
                    .tipoNotificacion(new TipoNotificacion(Constant.ID_TIPO_NOTIFICACION_SOLICITUD_RECIBIDA))
                    .titulo(Constant.TITULO_NOTIF_SOLICITUD_RECIBIDA)
                    .mensaje("Has recibido una nueva solicitud de paseo programado (" + solicitudGuardada.getCodigoReserva() + ") para el " + reqDto.getFechaProgramada() + ".")
                    .referenciaId(solicitudGuardada.getId())
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

        SolicitudPaseo solicitudCompleta = solicitudPaseoRepository.findByUuidWithDetails(solicitudGuardada.getUuid());
        if (solicitudCompleta == null) {
            solicitudCompleta = solicitudGuardada;
        }

        return solicitudPaseoDtoMapper.toResDtoFromMascotas(solicitudCompleta, mascotas);
    }

    private Cliente validarYObtenerCliente(String userEmail) throws BadRequestException {
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
        return cliente;
    }

    private List<Mascota> validarYObtenerMascotas(Cliente cliente, List<String> mascotaUuids) throws BadRequestException {
        List<Mascota> mascotas = new ArrayList<>();
        for (String mascotaUuid : mascotaUuids) {
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
        return mascotas;
    }

    private BigDecimal calcularMontoTotal(BigDecimal tarifaHoraPen, Integer minutosDuracion, int totalMascotas) {
        BigDecimal tarifaSnapshot = tarifaHoraPen.setScale(2, RoundingMode.HALF_UP);
        BigDecimal minutos = BigDecimal.valueOf(minutosDuracion);
        BigDecimal cantidadMascotas = BigDecimal.valueOf(totalMascotas);
        return tarifaSnapshot.multiply(minutos).multiply(cantidadMascotas)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private String generarCodigoReserva(String prefijo) {
        String yearMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String randomSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
        return prefijo + yearMonth + "-" + randomSuffix;
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
