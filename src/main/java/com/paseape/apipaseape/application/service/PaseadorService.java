package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.IDiaSemanaRepository;
import com.paseape.apipaseape.application.repository.IDisponibilidadPaseadorRepository;
import com.paseape.apipaseape.application.repository.IPaseadorRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.BloqueHorario;
import com.paseape.apipaseape.domain.entity.DiaSemana;
import com.paseape.apipaseape.domain.entity.DisponibilidadPaseador;
import com.paseape.apipaseape.domain.entity.DistritoLima;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.dto.request.CambiarDistritoCoberturaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.DisponibilidadBloqueFechaResDto;
import com.paseape.apipaseape.infrastructure.dto.response.DisponibilidadPaseadorFechaResDto;
import com.paseape.apipaseape.infrastructure.dto.response.PaseadorResDto;
import com.paseape.apipaseape.infrastructure.dto.response.UsuarioResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.IDigitalDocumentDtoMapper;
import com.paseape.apipaseape.infrastructure.mapper.dto.IPaseadorDtoMapper;
import com.paseape.apipaseape.infrastructure.mapper.dto.IUsuarioDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaseadorService {

    private final DigitalDocumentService digitalDocumentService;

    private final IPaseadorRepository paseadorRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IPaseadorDtoMapper paseadorDtoMapper;
    private final IUsuarioDtoMapper usuarioDtoMapper;
    private final IDigitalDocumentDtoMapper digitalDocumentDtoMapper;
    private final IDisponibilidadPaseadorRepository disponibilidadRepository;
    private final ISolicitudPaseoRepository solicitudPaseoRepository;
    private final IDiaSemanaRepository diaSemanaRepository;

    @Transactional(rollbackFor = Exception.class)
    public PaseadorResDto cambiarDistritoCobertura(CambiarDistritoCoberturaReqDto reqDto) throws BadRequestException {
        Usuario usuario = usuarioRepository.findByUuid(reqDto.getUsuarioUuid().trim());
        if (usuario == null) {
            throw new BadRequestException("El usuario especificado no existe.");
        }

        Paseador paseador = paseadorRepository.findByUsuarioId(usuario.getId());
        if (paseador == null) {
            throw new BadRequestException("El usuario no cuenta con un perfil de paseador registrado.");
        }

        paseador.setDistritoCobertura(new DistritoLima(reqDto.getDistritoId()));
        Paseador paseadorActualizado = paseadorRepository.save(paseador);

        return paseadorDtoMapper.toResDto(paseadorActualizado);
    }

    @Transactional(readOnly = true)
    public Paseador obtenerEntidadPaseador(String uuid) throws BadRequestException {
        if (uuid == null || uuid.trim().isEmpty()) {
            throw new BadRequestException("El identificador del paseador es obligatorio.");
        }
        Usuario usuario = usuarioRepository.findByUuid(uuid.trim());
        Paseador paseador = null;
        if (usuario != null) {
            paseador = paseadorRepository.findByUsuarioId(usuario.getId());
        } else {
            paseador = paseadorRepository.findByUuid(uuid.trim());
        }
        if (paseador == null) {
            throw new BadRequestException("No se encontró el paseador especificado.");
        }
        return paseador;
    }

    @Transactional(readOnly = true)
    public UsuarioResDto obtenerPaseador(String uuid) throws BadRequestException {
        Paseador paseador = obtenerEntidadPaseador(uuid);
        Usuario usuario = paseador.getUsuario() != null && paseador.getUsuario().getId() != null
                ? usuarioRepository.findById(paseador.getUsuario().getId())
                : null;
        if (usuario == null) {
            throw new BadRequestException("No se encontró el usuario del paseador.");
        }
        UsuarioResDto usuarioResDto = usuarioDtoMapper.toResDto(usuario, null, paseador, null, paseadorDtoMapper);
        usuarioResDto.setFotoPerfilUrl(digitalDocumentService.urlFotoPerfil(usuario));
        usuarioResDto.setDigitalDocuments(digitalDocumentDtoMapper.toResDtoList(digitalDocumentService.documentList(usuario.getFolderDigitalDocument().getId())));
        return usuarioResDto;
    }

    @Transactional(readOnly = true)
    public List<PaseadorResDto> obtenerPaseadores(List<Integer> distritos) {
        List<Paseador> paseadores = paseadorRepository.findAllByDistritoCoberturaIds(distritos);
        return paseadorDtoMapper.toResDtoList(paseadores);
    }

    @Transactional(readOnly = true)
    public List<PaseadorResDto> obtenerPaseadores(Integer distrito) {
        List<Integer> distritos = distrito != null ? List.of(distrito) : List.of();
        return obtenerPaseadores(distritos);
    }

    @Transactional(readOnly = true)
    public Paseador obtenerPerfilPorUsuarioId(Long usuarioId) throws BadRequestException {
        if (usuarioId == null) {
            throw new BadRequestException("El identificador de usuario es obligatorio.");
        }
        Paseador paseador = paseadorRepository.findByUsuarioId(usuarioId);
        if (paseador == null) {
            throw new BadRequestException("El usuario no cuenta con un perfil de paseador registrado.");
        }
        return paseador;
    }

    @Transactional(rollbackFor = Exception.class)
    public Paseador actualizarCapacidadPredeterminada(Long usuarioId, Integer nuevaCapacidadBase) throws BadRequestException {
        Paseador paseador = obtenerPerfilPorUsuarioId(usuarioId);
        paseador.setMaxMascotasPredeterminado(nuevaCapacidadBase);
        return paseadorRepository.save(paseador);
    }

    @Transactional(readOnly = true)
    public DisponibilidadPaseadorFechaResDto obtenerDisponibilidadPorFecha(String paseadorUuid, LocalDate fecha) throws BadRequestException {
        if (!StringUtils.hasText(paseadorUuid)) {
            throw new BadRequestException("El identificador del paseador es obligatorio.");
        }

        Paseador paseador = paseadorRepository.findByUuid(paseadorUuid.trim());
        if (paseador == null) {
            Usuario usuario = usuarioRepository.findByUuid(paseadorUuid.trim());
            if (usuario != null && usuario.getId() != null) {
                paseador = paseadorRepository.findByUsuarioId(usuario.getId());
            }
        }

        if (paseador == null || paseador.getEstado() == null || !paseador.getEstado().equals(Constant.ESTADO_LOGICO_ACTIVO)
                || paseador.getEstadoVerificacion() == null
                || !Integer.valueOf(Constant.ID_APROBADO).equals(paseador.getEstadoVerificacion().getId())) {
            throw new BadRequestException(Constant.MSJ_ERROR_PASEADOR_NO_DISPONIBLE);
        }

        ZoneId limaZone = ZoneId.of(Constant.ZONE_ID_LIMA);
        LocalDate fechaConsulta = (fecha != null) ? fecha : LocalDate.now(limaZone);

        int numeroDia = fechaConsulta.getDayOfWeek().getValue();
        DiaSemana diaSemana = diaSemanaRepository.findByNumeroDia(numeroDia);
        Integer diaSemanaId = (diaSemana != null && diaSemana.getId() != null) ? diaSemana.getId() : numeroDia;
        String diaSemanaDescripcion = (diaSemana != null && StringUtils.hasText(diaSemana.getDescripcion()))
                ? diaSemana.getDescripcion()
                : fechaConsulta.getDayOfWeek().name();

        List<DisponibilidadPaseador> slots = disponibilidadRepository.findAllByPaseadorIdAndDiaSemanaId(paseador.getId(), diaSemanaId);
        if (slots == null) {
            slots = List.of();
        }

        List<DisponibilidadPaseador> slotsActivos = slots.stream()
                .filter(s -> Boolean.TRUE.equals(s.getEstado()))
                .sorted(Comparator.comparing(
                        d -> d.getBloqueHorario() != null && d.getBloqueHorario().getOrden() != null
                                ? d.getBloqueHorario().getOrden()
                                : (d.getBloqueHorarioId() != null ? d.getBloqueHorarioId() : 0)
                ))
                .toList();

        Map<Integer, Integer> ocupadasPorBloque = solicitudPaseoRepository.countMascotasOcupadasAgrupadoPorBloque(
                paseador.getId(),
                fechaConsulta,
                List.of(
                        Constant.ID_ESTADO_SOLICITUD_PENDIENTE,
                        Constant.ID_ESTADO_SOLICITUD_ACEPTADA,
                        Constant.ID_ESTADO_SOLICITUD_EN_CURSO
                )
        );
        if (ocupadasPorBloque == null) {
            ocupadasPorBloque = Map.of();
        }

        LocalDate hoyLima = LocalDate.now(limaZone);
        LocalTime ahoraLima = LocalTime.now(limaZone);
        boolean esHoy = fechaConsulta.isEqual(hoyLima);

        int capacidadPredeterminada = (paseador.getMaxMascotasPredeterminado() != null)
                ? paseador.getMaxMascotasPredeterminado()
                : Constant.CAPACIDAD_BASE_DEFAULT;

        List<DisponibilidadBloqueFechaResDto> bloquesRes = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (DisponibilidadPaseador slot : slotsActivos) {
            BloqueHorario bloque = slot.getBloqueHorario();
            Integer bloqueId = (bloque != null && bloque.getId() != null) ? bloque.getId() : slot.getBloqueHorarioId();
            String bloqueCodigo = (bloque != null) ? bloque.getCodigo() : null;

            String horaInicioStr = null;
            String horaFinStr = null;
            if (bloque != null) {
                if (bloque.getHoraInicio() != null) {
                    horaInicioStr = bloque.getHoraInicio().format(timeFormatter);
                }
                if (bloque.getHoraFin() != null) {
                    horaFinStr = bloque.getHoraFin().format(timeFormatter);
                }
            }

            int capacidadMaxima = (slot.getMaxMascotas() != null) ? slot.getMaxMascotas() : capacidadPredeterminada;
            int ocupadas = ocupadasPorBloque.getOrDefault(bloqueId, 0);
            int cuposDisponibles = Math.max(0, capacidadMaxima - ocupadas);
            boolean estaLleno = cuposDisponibles <= 0;

            boolean expirado = false;
            if (esHoy && bloque != null && bloque.getHoraInicio() != null) {
                expirado = !bloque.getHoraInicio().isAfter(ahoraLima);
            }

            boolean disponible = !estaLleno && !expirado;

            String motivoNoDisponible = null;
            if (expirado) {
                motivoNoDisponible = Constant.MOTIVO_HORARIO_EXPIRADO;
            } else if (estaLleno) {
                motivoNoDisponible = Constant.MOTIVO_SIN_CUPOS;
            }

            bloquesRes.add(DisponibilidadBloqueFechaResDto.builder()
                    .bloqueHorarioId(bloqueId)
                    .bloqueCodigo(bloqueCodigo)
                    .horaInicio(horaInicioStr)
                    .horaFin(horaFinStr)
                    .maxMascotas(capacidadMaxima)
                    .mascotasOcupadas(ocupadas)
                    .cuposDisponibles(cuposDisponibles)
                    .estaLleno(estaLleno)
                    .disponible(disponible)
                    .motivoNoDisponible(motivoNoDisponible)
                    .build());
        }

        return DisponibilidadPaseadorFechaResDto.builder()
                .paseadorUuid(paseador.getUuid())
                .fecha(fechaConsulta)
                .diaSemanaId(diaSemanaId)
                .diaSemanaDescripcion(diaSemanaDescripcion)
                .bloques(bloquesRes)
                .build();
    }
}
