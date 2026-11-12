package com.paseape.apipaseape.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SolicitudPaseoDetalleResDto {

    private String uuid;
    private String codigoReserva;
    private Integer tipoModalidadPaseoId;
    private String tipoModalidadPaseoDescripcion;
    private Integer estadoId;
    private String estadoDescripcion;
    private Integer tipoMetodoPagoId;
    private String tipoMetodoPagoDescripcion;
    private Integer tipoDuracionPaseoId;
    private String tipoDuracionPaseoDescripcion;
    private Integer duracionMinutos;
    private Integer distritoId;
    private String distritoDescripcion;
    private String direccionRecojo;
    private String referenciaDireccion;
    private LocalDate fechaProgramada;
    private Integer bloqueHorarioId;
    private String bloqueHorarioCodigo;
    private LocalTime bloqueHorarioHoraInicio;
    private LocalTime bloqueHorarioHoraFin;
    private BigDecimal tarifaHoraPenSnapshot;
    private BigDecimal montoTotal;
    private String motivoRechazoCancelacion;
    private LocalDateTime horaVencimiento;
    private LocalDateTime fechaHoraAceptada;
    private LocalDateTime fechaHoraInicioReal;
    private LocalDateTime fechaHoraFinReal;
    private LocalDateTime creadoEl;
    private String clienteUuid;
    private String clienteNombreCompleto;
    private SolicitudPaseadorDetalleResDto paseador;
    private List<SolicitudMascotaDetalleResDto> mascotas;
}
