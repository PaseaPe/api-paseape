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
public class ResumenCalificacionesPaseadorResDto {

    private String paseadorUuid;
    private BigDecimal calificacionPromedio;
    private Integer totalCalificaciones;
    private Boolean tieneCalificaciones;
    private String mensajeEstado;
    private PageResponseDto<OpinionPaseadorResDto> opiniones;
}
