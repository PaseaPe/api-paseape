package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.application.repository.ISolicitudPaseoRepository;
import com.paseape.apipaseape.domain.entity.SolicitudPaseo;
import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.ISolicitudPaseoDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ISolicitudPaseoJpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class SolicitudPaseoRepository implements ISolicitudPaseoRepository {

    private final ISolicitudPaseoJpaRepository jpaRepository;
    private final ISolicitudPaseoDboMapper mapper;

    @Override
    public SolicitudPaseo save(SolicitudPaseo solicitudPaseo) {
        if (solicitudPaseo == null) {
            return null;
        }
        SolicitudPaseoEntity entity = mapper.toEntity(solicitudPaseo);
        SolicitudPaseoEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public SolicitudPaseo findById(Long id) {
        if (id == null) {
            return null;
        }
        SolicitudPaseoEntity entity = jpaRepository.findById(id.longValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public SolicitudPaseo findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        SolicitudPaseoEntity entity = jpaRepository.findByUuid(uuid.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public SolicitudPaseo findByUuidWithDetails(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        SolicitudPaseoEntity entity = jpaRepository.findByUuidWithDetails(uuid.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<SolicitudPaseo> findDisponiblesByDistrito(Integer modalidadId, Integer estadoId, Integer distritoId) {
        if (modalidadId == null || estadoId == null || distritoId == null) {
            return List.of();
        }
        List<SolicitudPaseoEntity> entities = jpaRepository.findDisponiblesByDistrito(modalidadId, estadoId, distritoId);
        return mapper.toDomainList(entities);
    }

    @Override
    public int adjudicarSolicitudInmediata(String solicitudUuid, Long paseadorId, Integer estadoAceptadaId, Integer estadoBuscandoId, LocalDateTime ahoraUtc, String emailPaseador) {
        if (solicitudUuid == null || solicitudUuid.trim().isEmpty() || paseadorId == null || estadoAceptadaId == null || estadoBuscandoId == null) {
            return 0;
        }
        return jpaRepository.adjudicarSolicitudInmediata(solicitudUuid.trim(), paseadorId, estadoAceptadaId, estadoBuscandoId, ahoraUtc, emailPaseador);
    }

    @Override
    public SolicitudPaseo findByCodigoReserva(String codigoReserva) {
        if (codigoReserva == null || codigoReserva.trim().isEmpty()) {
            return null;
        }
        SolicitudPaseoEntity entity = jpaRepository.findByCodigoReserva(codigoReserva.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<SolicitudPaseo> findAllByClienteId(Long clienteId) {
        if (clienteId == null) {
            return List.of();
        }
        List<SolicitudPaseoEntity> entities = jpaRepository.findAllByClienteId(clienteId);
        return mapper.toDomainList(entities);
    }

    @Override
    public List<SolicitudPaseo> findAllByPaseadorId(Long paseadorId) {
        if (paseadorId == null) {
            return List.of();
        }
        List<SolicitudPaseoEntity> entities = jpaRepository.findAllByPaseadorId(paseadorId);
        return mapper.toDomainList(entities);
    }

    @Override
    public List<SolicitudPaseo> findAllByDistritoIdAndEstadoId(Integer distritoId, Integer estadoId) {
        if (distritoId == null || estadoId == null) {
            return List.of();
        }
        List<SolicitudPaseoEntity> entities = jpaRepository.findAllByDistritoIdAndSolicitudPaseoEstadoId(distritoId, estadoId);
        return mapper.toDomainList(entities);
    }

    @Override
    public Integer countMascotasOcupadas(Long paseadorId, LocalDate fechaProgramada, Integer bloqueHorarioId, List<Integer> estadosIds) {
        if (paseadorId == null || fechaProgramada == null || bloqueHorarioId == null || estadosIds == null || estadosIds.isEmpty()) {
            return 0;
        }
        Long count = jpaRepository.countMascotasOcupadas(paseadorId, fechaProgramada, bloqueHorarioId, estadosIds);
        return count != null ? count.intValue() : 0;
    }

    @Override
    public Map<Integer, Integer> countMascotasOcupadasAgrupadoPorBloque(Long paseadorId, LocalDate fechaProgramada, List<Integer> estadosIds) {
        if (paseadorId == null || fechaProgramada == null || estadosIds == null || estadosIds.isEmpty()) {
            return Map.of();
        }
        List<Object[]> rows = jpaRepository.countMascotasOcupadasAgrupadoPorBloque(paseadorId, fechaProgramada, estadosIds);
        Map<Integer, Integer> result = new HashMap<>();
        if (rows != null) {
            for (Object[] row : rows) {
                if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                    Integer bloqueId = ((Number) row[0]).intValue();
                    Integer count = ((Number) row[1]).intValue();
                    result.put(bloqueId, count);
                }
            }
        }
        return result;
    }

    @Override
    public List<SolicitudPaseo> findFutureBookingsByPaseadorAndBloque(Long paseadorId, Integer bloqueHorarioId, LocalDate fechaHoy, List<Integer> estadosIds) {
        if (paseadorId == null || bloqueHorarioId == null || fechaHoy == null || estadosIds == null || estadosIds.isEmpty()) {
            return List.of();
        }
        List<SolicitudPaseoEntity> entities = jpaRepository.findFutureBookingsByPaseadorAndBloque(paseadorId, bloqueHorarioId, fechaHoy, estadosIds);
        return mapper.toDomainList(entities);
    }

    @Override
    public Page<SolicitudPaseo> findInmediatasDisponiblesPaginado(
            Integer modalidadId,
            Integer estadoId,
            Integer distritoId,
            Long paseadorId,
            Pageable pageable
    ) {
        if (modalidadId == null || estadoId == null || distritoId == null || paseadorId == null) {
            return Page.empty(pageable);
        }
        Page<SolicitudPaseoEntity> entities =
                jpaRepository.findInmediatasDisponiblesPaginado(modalidadId, estadoId, distritoId, paseadorId, pageable);
        return entities.map(mapper::toDomain);
    }

    @Override
    public Page<SolicitudPaseo> findByPaseadorIdAndEstadosPaginado(
            Long paseadorId,
            List<Integer> estadosIds,
            Pageable pageable
    ) {
        if (paseadorId == null || estadosIds == null || estadosIds.isEmpty()) {
            return Page.empty(pageable);
        }
        Page<SolicitudPaseoEntity> entities =
                jpaRepository.findByPaseadorIdAndEstadosPaginado(paseadorId, estadosIds, pageable);
        return entities.map(mapper::toDomain);
    }

    @Override
    public int expirarSolicitudesInmediatasVencidas(
            Integer modalidadInmediatoId,
            Integer estadoBuscandoId,
            Integer estadoVencidaId,
            LocalDateTime ahora
    ) {
        if (modalidadInmediatoId == null || estadoBuscandoId == null || estadoVencidaId == null || ahora == null) {
            return 0;
        }
        return jpaRepository.expirarSolicitudesInmediatasVencidas(modalidadInmediatoId, estadoBuscandoId, estadoVencidaId, ahora);
    }

    @Override
    public Page<SolicitudPaseo> findByClienteIdAndEstadosPaginado(Long clienteId, List<Integer> estados, Pageable pageable) {
        Page<SolicitudPaseoEntity> entities = jpaRepository.findByClienteIdAndEstadosPaginado(clienteId, estados, pageable);
        return entities.map(mapper::toDomain);
    }
}
