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
public class MascotaResDto {

    private Long id;
    private String uuid;
    private Long clienteId;
    private String nombre;
    private Integer tipoMascotaId;
    private String tipoMascotaDescripcion;
    private Integer tipoRazaId;
    private String tipoRazaDescripcion;
    private Integer tipoGeneroMascotaId;
    private String tipoGeneroMascotaDescripcion;
    private Integer tipoTamanoMascotaId;
    private String tipoTamanoMascotaDescripcion;
    private Integer tipoNivelEnergiaId;
    private String tipoNivelEnergiaDescripcion;
    private Integer edadAnos;
    private Integer edadMeses;
    private BigDecimal pesoKg;
    private Integer esterilizado;
    private Integer sociableConPerros;
    private Integer sociableConPersonas;
    private String precaucionesMedicas;
    private String fotoUrl;
    private Integer estado;
}
