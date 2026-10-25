package com.paseape.apipaseape.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DisponibilidadBloqueFechaResDto {

    private Integer bloqueHorarioId;
    private String bloqueCodigo;
    private String horaInicio;
    private String horaFin;
    private Integer maxMascotas;
    private Integer mascotasOcupadas;
    private Integer cuposDisponibles;
    private Boolean estaLleno;
    private Boolean disponible;
    private String motivoNoDisponible;
}
