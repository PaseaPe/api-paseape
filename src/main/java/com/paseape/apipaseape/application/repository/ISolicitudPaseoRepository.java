package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.SolicitudPaseo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ISolicitudPaseoRepository {

    SolicitudPaseo save(SolicitudPaseo solicitudPaseo);

    SolicitudPaseo findById(Long id);

    SolicitudPaseo findByCodigoReserva(String codigoReserva);

    List<SolicitudPaseo> findAllByClienteId(Long clienteId);

    List<SolicitudPaseo> findAllByPaseadorId(Long paseadorId);

    List<SolicitudPaseo> findAllByDistritoIdAndEstadoId(Integer distritoId, Integer estadoId);

    Integer countMascotasOcupadas(Long paseadorId, LocalDate fechaProgramada, Integer bloqueHorarioId, List<Integer> estadosIds);

    Map<Integer, Integer> countMascotasOcupadasAgrupadoPorBloque(Long paseadorId, LocalDate fechaProgramada, List<Integer> estadosIds);

    List<SolicitudPaseo> findFutureBookingsByPaseadorAndBloque(Long paseadorId, Integer bloqueHorarioId, LocalDate fechaHoy, List<Integer> estadosIds);
}
