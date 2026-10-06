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
public class Paseador {

    private Long id;
    private String uuid;
    private Usuario usuario;
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;
    private String antecedentesPolicialesUrl;
    private Integer experienciaAnos;
    private String biografia;
    private BigDecimal tarifaHoraPen;
    private DistritoLima distritoCobertura;
    private PaseadorEstadoVerificacion estadoVerificacion;
    private Integer paseosCompletados;
    private BigDecimal calificacionPromedio;
    private Integer estado;
    private LocalDateTime creadoEl;
    private LocalDateTime actualizadoEl;
    private String creadoPor;
    private String actualizadoPor;
}
