package com.paseape.apipaseape.infrastructure.dto.request;

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
public class ActualizarMascotaReqDto {

    private String nombre;
    private Integer tipoMascotaId;
    private Integer tipoRazaId;
    private Integer tipoGeneroMascotaId;
    private Integer tipoTamanoMascotaId;
    private Integer tipoNivelEnergiaId;
    private Integer edadAnos;
    private Integer edadMeses;
    private BigDecimal pesoKg;
    private Integer esterilizado;
    private Integer sociableConPerros;
    private Integer sociableConPersonas;
    private String precaucionesMedicas;
    private String fotoUrl;
}
