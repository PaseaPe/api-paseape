package com.paseape.apipaseape.infrastructure.dto.request;
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
public class CrearSolicitudProgramadaReqDto {

    private String paseadorUuid;
    private List<String> mascotaUuids;
    private Integer distritoId;
    private LocalDate fechaProgramada;
    private Integer bloqueHorarioId;
    private Integer tipoDuracionPaseoId;
    private Integer tipoMetodoPagoId;
    private String direccionRecojo;
    private String referenciaDireccion;
}
