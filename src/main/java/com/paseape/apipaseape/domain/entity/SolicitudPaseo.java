package com.paseape.apipaseape.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudPaseo {

    private Long id;
    private String uuid;
    private String codigoReserva;
    private TipoModalidadPaseo tipoModalidadPaseo;
    private TipoMetodoPago tipoMetodoPago;
    private Cliente cliente;
    private Paseador paseador;
    private DistritoLima distrito;
    private String direccionRecojo;
    private String referenciaDireccion;
    private LocalDate fechaProgramada;
    private BloqueHorario bloqueHorario;
    private TipoDuracionPaseo tipoDuracionPaseo;
    private BigDecimal tarifaHoraPenSnapshot;
    private BigDecimal montoTotal;
    private SolicitudPaseoEstado solicitudPaseoEstado;
    private String motivoRechazoCancelacion;
    private LocalDateTime fechaHoraAceptada;
    private LocalDateTime fechaHoraInicioReal;
    private LocalDateTime fechaHoraFinReal;
    private Integer estado;
    private LocalDateTime creadoEl;
    private LocalDateTime actualizadoEl;
    private String creadoPor;
    private String actualizadoPor;
}
