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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DisponibilidadPaseadorService {

    private final IDisponibilidadPaseadorRepository disponibilidadRepository;
    private final IDiaSemanaRepository diaSemanaRepository;
    private final IBloqueHorarioRepository bloqueHorarioRepository;
    private final ISolicitudPaseoRepository solicitudPaseoRepository;
    private final PaseadorService paseadorService;
    private final IDisponibilidadPaseadorDtoMapper disponibilidadDtoMapper;
    private final IPaseadorPerfilDtoMapper paseadorPerfilDtoMapper;

    @Transactional(readOnly = true)
    public DisponibilidadPaseadorResDto obtenerMiDisponibilidad(Long usuarioId) throws BadRequestException {
        Paseador paseador = paseadorService.obtenerPerfilPorUsuarioId(usuarioId);
        List<DisponibilidadPaseador> slots = new ArrayList<>(disponibilidadRepository.findAllByPaseadorId(paseador.getId()));

        slots.sort(Comparator
                .comparing(DisponibilidadPaseador::getDiaSemanaId, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(d -> d.getBloqueHorario() != null && d.getBloqueHorario().getOrden() != null
                        ? d.getBloqueHorario().getOrden() : d.getBloqueHorarioId(), Comparator.nullsLast(Comparator.naturalOrder()))
        );

        return disponibilidadDtoMapper.toResDto(paseador.getMaxMascotasPredeterminado(), slots);
    }

    @Transactional(readOnly = true)
    public DisponibilidadPaseadorResDto obtenerDisponibilidadPorPaseador(Paseador paseador) {
        return obtenerDisponibilidadPorPaseadorYFecha(paseador, null);
    }

    @Transactional(readOnly = true)
    public DisponibilidadPaseadorResDto obtenerDisponibilidadPorPaseadorUuid(String uuid) throws BadRequestException {
        Paseador paseador = paseadorService.obtenerEntidadPaseador(uuid);
        return obtenerDisponibilidadPorPaseador(paseador);
    }

    @Transactional(readOnly = true)
    public DisponibilidadPaseadorResDto obtenerDisponibilidadPorPaseadorUuidYFecha(String uuid, LocalDate fecha) throws BadRequestException {
        Paseador paseador = paseadorService.obtenerEntidadPaseador(uuid);
        return obtenerDisponibilidadPorPaseadorYFecha(paseador, fecha);
    }

    @Transactional(readOnly = true)
    public DisponibilidadPaseadorResDto obtenerDisponibilidadPorPaseadorYFecha(Paseador paseador, LocalDate fecha) {
        if (paseador == null || paseador.getId() == null) {
            return DisponibilidadPaseadorResDto.builder()
                    .maxMascotasPredeterminado(Constant.CAPACIDAD_BASE_DEFAULT)
                    .slots(List.of())
                    .build();
        }

        List<DisponibilidadPaseador> slots = new ArrayList<>(disponibilidadRepository.findAllByPaseadorId(paseador.getId()));

        if (fecha != null) {
            int numeroDia = fecha.getDayOfWeek().getValue();
            DiaSemana dia = diaSemanaRepository.findByNumeroDia(numeroDia);
            Integer diaSemanaId = dia != null ? dia.getId() : numeroDia;
            slots = slots.stream()
                    .filter(s -> diaSemanaId.equals(s.getDiaSemanaId()))
                    .collect(Collectors.toList());
        }

        slots.sort(Comparator
                .comparing(DisponibilidadPaseador::getDiaSemanaId, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(d -> d.getBloqueHorario() != null && d.getBloqueHorario().getOrden() != null
                        ? d.getBloqueHorario().getOrden() : d.getBloqueHorarioId(), Comparator.nullsLast(Comparator.naturalOrder()))
        );

        Map<Integer, Integer> ocupadasPorBloque = (fecha != null)
                ? solicitudPaseoRepository.countMascotasOcupadasAgrupadoPorBloque(
                        paseador.getId(),
                        fecha,
                        List.of(1, 2, 5) // PENDIENTE, ACEPTADA, EN_CURSO
                  )
                : Map.of();

        ZoneId limaZone = ZoneId.of("America/Lima");
        LocalDate hoyLima = LocalDate.now(limaZone);
        LocalTime ahoraLima = LocalTime.now(limaZone);

        boolean esFechaPasada = fecha != null && fecha.isBefore(hoyLima);
        boolean esHoy = fecha != null && fecha.isEqual(hoyLima);

        int cupoDefault = paseador.getMaxMascotasPredeterminado() != null
                ? paseador.getMaxMascotasPredeterminado()
                : Constant.CAPACIDAD_BASE_DEFAULT;

        List<DisponibilidadSlotResDto> slotDtos = new ArrayList<>();
        for (DisponibilidadPaseador slot : slots) {
            DisponibilidadSlotResDto dto = disponibilidadDtoMapper.toSlotResDto(slot);

            int max = slot.getMaxMascotas() != null ? slot.getMaxMascotas() : cupoDefault;
            int ocupadas = ocupadasPorBloque.getOrDefault(slot.getBloqueHorarioId(), 0);
            int disponibles = Math.max(0, max - ocupadas);
            boolean estaLleno = disponibles <= 0;

            boolean esHoraPasada = false;
            if (esFechaPasada) {
                esHoraPasada = true;
            } else if (esHoy && slot.getBloqueHorario() != null && slot.getBloqueHorario().getHoraInicio() != null) {
                esHoraPasada = ahoraLima.isAfter(slot.getBloqueHorario().getHoraInicio());
            }

            boolean activo = Boolean.TRUE.equals(slot.getEstado());
            boolean disponible = activo && !estaLleno && !esHoraPasada;

            dto.setMascotasOcupadas(ocupadas);
            dto.setCuposDisponibles(disponibles);
            dto.setEstaLleno(estaLleno);
            dto.setDisponible(disponible);

            slotDtos.add(dto);
        }

        return DisponibilidadPaseadorResDto.builder()
                .maxMascotasPredeterminado(paseador.getMaxMascotasPredeterminado())
                .slots(slotDtos)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public DisponibilidadPaseadorResDto sincronizarDisponibilidad(Long usuarioId, GuardarDisponibilidadReqDto reqDto) throws BadRequestException {
        Paseador paseador = paseadorService.obtenerPerfilPorUsuarioId(usuarioId);

        int cupoDefault = paseador.getMaxMascotasPredeterminado() != null
                ? paseador.getMaxMascotasPredeterminado()
                : Constant.CAPACIDAD_BASE_DEFAULT;

        List<DisponibilidadPaseador> existingSlots = disponibilidadRepository.findAllByPaseadorIdIncludingInactive(paseador.getId());
        Map<String, DisponibilidadPaseador> existingMap = existingSlots.stream()
                .collect(Collectors.toMap(
                        d -> d.getDiaSemanaId() + "-" + d.getBloqueHorarioId(),
                        d -> d,
                        (existing, replacement) -> existing
                ));

        Set<String> requestedKeys = new HashSet<>();
        List<DisponibilidadPaseador> toSave = new ArrayList<>();

        for (DisponibilidadItemReqDto item : reqDto.getBloques()) {
            String key = item.getDiaSemanaId() + "-" + item.getBloqueHorarioId();
            requestedKeys.add(key);

            int cupo = item.getMaxMascotas() != null ? item.getMaxMascotas() : cupoDefault;
            DisponibilidadPaseador slot = existingMap.get(key);

            if (slot != null) {
                slot.setEstado(true);
                slot.setMaxMascotas(cupo);
                toSave.add(slot);
            } else {
                DiaSemana dia = diaSemanaRepository.findById(item.getDiaSemanaId());
                if (dia == null) {
                    throw new BadRequestException("El día de la semana con id " + item.getDiaSemanaId() + " no existe.");
                }
                BloqueHorario bloque = bloqueHorarioRepository.findById(item.getBloqueHorarioId());
                if (bloque == null) {
                    throw new BadRequestException("El bloque horario con id " + item.getBloqueHorarioId() + " no existe.");
                }

                DisponibilidadPaseador newSlot = DisponibilidadPaseador.builder()
                        .uuid(UUID.randomUUID().toString())
                        .paseadorId(paseador.getId())
                        .diaSemanaId(item.getDiaSemanaId())
                        .bloqueHorarioId(item.getBloqueHorarioId())
                        .diaSemana(dia)
                        .bloqueHorario(bloque)
                        .maxMascotas(cupo)
                        .estado(true)
                        .build();
                toSave.add(newSlot);
            }
        }

        // Borrado lógico a los slots previamente activos que fueron desmarcados
        for (DisponibilidadPaseador existing : existingSlots) {
            String key = existing.getDiaSemanaId() + "-" + existing.getBloqueHorarioId();
            if (!requestedKeys.contains(key) && Boolean.TRUE.equals(existing.getEstado())) {
                existing.setEstado(false);
                toSave.add(existing);
            }
        }

        disponibilidadRepository.saveAll(toSave);

        List<DisponibilidadPaseador> updatedActiveSlots = new ArrayList<>(disponibilidadRepository.findAllByPaseadorId(paseador.getId()));
        updatedActiveSlots.sort(Comparator
                .comparing(DisponibilidadPaseador::getDiaSemanaId, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(d -> d.getBloqueHorario() != null && d.getBloqueHorario().getOrden() != null
                        ? d.getBloqueHorario().getOrden() : d.getBloqueHorarioId(), Comparator.nullsLast(Comparator.naturalOrder()))
        );

        return disponibilidadDtoMapper.toResDto(paseador.getMaxMascotasPredeterminado(), updatedActiveSlots);
    }

    @Transactional(rollbackFor = Exception.class)
    public DisponibilidadSlotResDto actualizarBloqueDisponibilidad(Long usuarioId, ActualizarBloqueDisponibilidadReqDto reqDto) throws BadRequestException {
        Paseador paseador = paseadorService.obtenerPerfilPorUsuarioId(usuarioId);

        DiaSemana diaSemana = diaSemanaRepository.findById(reqDto.getDiaSemanaId());
        if (diaSemana == null) {
            throw new BadRequestException("El día de la semana especificado no existe.");
        }

        BloqueHorario bloqueHorario = bloqueHorarioRepository.findById(reqDto.getBloqueHorarioId());
        if (bloqueHorario == null) {
            throw new BadRequestException("El bloque horario especificado no existe.");
        }

        DisponibilidadPaseador slot = disponibilidadRepository.findByPaseadorIdAndDiaSemanaIdAndBloqueHorarioIdIncludingInactive(
                paseador.getId(),
                reqDto.getDiaSemanaId(),
                reqDto.getBloqueHorarioId()
        );

        String accion = reqDto.getAccion().trim().toUpperCase();

        if (Constant.ACCION_BLOQUE_CANCELAR.equals(accion)) {
            if (slot == null || Boolean.FALSE.equals(slot.getEstado())) {
                return DisponibilidadSlotResDto.builder()
                        .id(slot != null ? slot.getId() : null)
                        .diaSemanaId(diaSemana.getId())
                        .diaSemanaDescripcion(diaSemana.getDescripcion())
                        .bloqueHorarioId(bloqueHorario.getId())
                        .bloqueCodigo(bloqueHorario.getCodigo())
                        .horaInicio(bloqueHorario.getHoraInicio() != null ? bloqueHorario.getHoraInicio().toString() : null)
                        .horaFin(bloqueHorario.getHoraFin() != null ? bloqueHorario.getHoraFin().toString() : null)
                        .maxMascotas(slot != null ? slot.getMaxMascotas() : (paseador.getMaxMascotasPredeterminado() != null ? paseador.getMaxMascotasPredeterminado() : Constant.CAPACIDAD_BASE_DEFAULT))
                        .activo(false)
                        .build();
            }

            // Validar que no haya reservas futuras activas para ese slot
            List<SolicitudPaseo> futureBookings = solicitudPaseoRepository.findFutureBookingsByPaseadorAndBloque(
                    paseador.getId(),
                    bloqueHorario.getId(),
                    LocalDate.now(),
                    List.of(1, 2, 5) // PENDIENTE, ACEPTADA, EN_CURSO
            );

            boolean tieneConflicto = futureBookings.stream().anyMatch(b ->
                    b.getFechaProgramada() != null &&
                    b.getFechaProgramada().getDayOfWeek().getValue() == diaSemana.getNumeroDia()
            );

            if (tieneConflicto) {
                throw new BadRequestException("No se puede cancelar el bloque porque tiene reservas programadas.");
            }

            slot.setEstado(false);
            slot.setDiaSemana(diaSemana);
            slot.setBloqueHorario(bloqueHorario);
            DisponibilidadPaseador guardado = disponibilidadRepository.save(slot);
            return disponibilidadDtoMapper.toSlotResDto(guardado);
        }

        int cupoBase = paseador.getMaxMascotasPredeterminado() != null
                ? paseador.getMaxMascotasPredeterminado()
                : Constant.CAPACIDAD_BASE_DEFAULT;

        if (Constant.ACCION_BLOQUE_REESTABLECER.equals(accion)) {
            if (slot != null) {
                slot.setMaxMascotas(cupoBase);
                slot.setEstado(true);
                slot.setDiaSemana(diaSemana);
                slot.setBloqueHorario(bloqueHorario);
            } else {
                slot = DisponibilidadPaseador.builder()
                        .uuid(UUID.randomUUID().toString())
                        .paseadorId(paseador.getId())
                        .diaSemanaId(diaSemana.getId())
                        .bloqueHorarioId(bloqueHorario.getId())
                        .diaSemana(diaSemana)
                        .bloqueHorario(bloqueHorario)
                        .maxMascotas(cupoBase)
                        .estado(true)
                        .build();
            }
            DisponibilidadPaseador guardado = disponibilidadRepository.save(slot);
            return disponibilidadDtoMapper.toSlotResDto(guardado);
        }

        if (Constant.ACCION_BLOQUE_GUARDAR_CUPO.equals(accion)) {
            int nuevoCupo = reqDto.getMaxMascotas();
            if (slot != null) {
                slot.setMaxMascotas(nuevoCupo);
                slot.setEstado(true);
                slot.setDiaSemana(diaSemana);
                slot.setBloqueHorario(bloqueHorario);
            } else {
                slot = DisponibilidadPaseador.builder()
                        .uuid(UUID.randomUUID().toString())
                        .paseadorId(paseador.getId())
                        .diaSemanaId(diaSemana.getId())
                        .bloqueHorarioId(bloqueHorario.getId())
                        .diaSemana(diaSemana)
                        .bloqueHorario(bloqueHorario)
                        .maxMascotas(nuevoCupo)
                        .estado(true)
                        .build();
            }
            DisponibilidadPaseador guardado = disponibilidadRepository.save(slot);
            return disponibilidadDtoMapper.toSlotResDto(guardado);
        }

        throw new BadRequestException("Acción no reconocida para el bloque de disponibilidad.");
    }

    @Transactional(rollbackFor = Exception.class)
    public PaseadorPerfilResDto actualizarCapacidadBase(Long usuarioId, ActualizarCapacidadBaseReqDto reqDto) throws BadRequestException {
        Paseador paseadorActualizado = paseadorService.actualizarCapacidadPredeterminada(
                usuarioId,
                reqDto.getMaxMascotasPredeterminado()
        );
        return paseadorPerfilDtoMapper.toResDto(paseadorActualizado);
    }
}
