package com.paseape.apipaseape.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DisponibilidadPaseadorFechaResDto {

    private String paseadorUuid;
    private LocalDate fecha;
    private Integer diaSemanaId;
    private String diaSemanaDescripcion;
    private List<DisponibilidadBloqueFechaResDto> bloques;
}
