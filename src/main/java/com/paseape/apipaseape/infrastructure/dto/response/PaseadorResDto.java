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
public class PaseadorResDto {

    private Long id;
    private String uuid;
    private String usuarioUuid;
    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;
    private Integer tipoDocumentoId;
    private String numeroDocumento;
    private Integer experienciaAnos;
    private String biografia;
    private BigDecimal tarifaHoraPen;
    private Integer distritoCoberturaId;
    private String distritoCoberturaDescripcion;
    private Integer estadoVerificacionId;
    private String estadoVerificacionDescripcion;
    private Integer paseosCompletados;
    private BigDecimal calificacionPromedio;
    private Integer estado;
}
