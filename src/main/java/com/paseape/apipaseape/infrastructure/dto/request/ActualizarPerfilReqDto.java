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
public class ActualizarPerfilReqDto {

    // Usuario
    private String nombres;
    private String apellidos;
    private String telefono;
    private String fotoPerfilUrl;

    // Cliente
    private String direccionReferencia;
    private Integer distritoId;
    private String contactoEmergenciaNombre;
    private String contactoEmergenciaTelefono;
    private String notasAdicionales;

    // Paseador
    private String biografia;
    private BigDecimal tarifaHoraPen;
    private Integer distritoCoberturaId;
}
