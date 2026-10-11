package com.paseape.apipaseape.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PaseadorPerfilResDto {

    private String uuid;
    private String nombres;
    private String apellidos;
    private String fotoPerfilUrl;
    private String biografia;
    private Integer experienciaAnos;
    private BigDecimal tarifaHoraPen;
    private Integer distritoCoberturaId;
    private String distritoCoberturaDescripcion;
    private Integer paseosCompletados;
    private BigDecimal calificacionPromedio;
}
