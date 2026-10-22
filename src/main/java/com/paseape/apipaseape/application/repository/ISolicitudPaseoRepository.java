package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.SolicitudPaseo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ISolicitudPaseoRepository {

    SolicitudPaseo save(SolicitudPaseo solicitudPaseo);

    SolicitudPaseo findById(Long id);

    SolicitudPaseo findByUuid(String uuid);

    SolicitudPaseo findByUuidWithDetails(String uuid);

    SolicitudPaseo findByCodigoReserva(String codigoReserva);

    List<SolicitudPaseo> findAllByClienteId(Long clienteId);

    List<SolicitudPaseo> findAllByPaseadorId(Long paseadorId);

    List<SolicitudPaseo> findAllByDistritoIdAndEstadoId(Integer distritoId, Integer estadoId);

    List<SolicitudPaseo> findDisponiblesByDistrito(Integer modalidadId, Integer estadoId, Integer distritoId);

    int adjudicarSolicitudInmediata(String solicitudUuid, Long paseadorId, Integer estadoAceptadaId, Integer estadoBuscandoId, java.time.LocalDateTime ahoraUtc, String emailPaseador);

    Integer countMascotasOcupadas(Long paseadorId, LocalDate fechaProgramada, Integer bloqueHorarioId, List<Integer> estadosIds);

    Map<Integer, Integer> countMascotasOcupadasAgrupadoPorBloque(Long paseadorId, LocalDate fechaProgramada, List<Integer> estadosIds);

    List<SolicitudPaseo> findFutureBookingsByPaseadorAndBloque(Long paseadorId, Integer bloqueHorarioId, LocalDate fechaHoy, List<Integer> estadosIds);
}
