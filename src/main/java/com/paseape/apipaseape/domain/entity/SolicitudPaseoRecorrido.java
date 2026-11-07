package com.paseape.apipaseape.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudPaseoRecorrido {

    private Long id;
    private String uuid;
    private SolicitudPaseo solicitudPaseo;
    private TipoOrigenTraza tipoOrigenTraza;
    private BigDecimal distanciaMetros;
    private Integer duracionSegundos;
    private BigDecimal velocidadPromedioKmh;
    private String coordenadasCodificadas;
    private Integer coordenadasTotalPuntos;
    private Integer estado;
    private LocalDateTime creadoEl;
    private LocalDateTime actualizadoEl;
    private String creadoPor;
    private String actualizadoPor;
}
