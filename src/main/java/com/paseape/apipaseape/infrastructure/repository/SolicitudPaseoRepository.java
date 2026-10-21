package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.application.repository.ISolicitudPaseoRepository;
import com.paseape.apipaseape.domain.entity.SolicitudPaseo;
import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.ISolicitudPaseoDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ISolicitudPaseoJpaRepository;

import java.time.LocalDate;
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
}
