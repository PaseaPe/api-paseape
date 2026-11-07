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
public class SolicitudPaseoUbicacionEnVivo {

    private Long id;
    private String uuid;
    private SolicitudPaseo solicitudPaseo;
    private BigDecimal latitudActual;
    private BigDecimal longitudActual;
    private BigDecimal precisionM;
    private BigDecimal velocidadMs;
    private BigDecimal rumboGrados;
    private LocalDateTime fechaHoraGps;
    private String coordenadasCodificadas;
    private BigDecimal distanciaMetrosAcumulada;
    private Integer totalPuntos;
    private Integer estado;
    private LocalDateTime creadoEl;
    private LocalDateTime actualizadoEl;
    private String creadoPor;
    private String actualizadoPor;
}
