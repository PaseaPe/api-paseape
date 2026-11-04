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
public class PerfilResDto {

    private Long id;
    private String uuid;
    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;
    private String fotoPerfilUrl;
    private Integer tipoUsuarioId;
    private String tipoUsuarioDescripcion;
    private Integer usuarioEstadoId;
    private String usuarioEstadoDescripcion;

    // Cliente (null if not CLIENTE)
    private Long clienteId;
    private String clienteUuid;
    private String direccionReferencia;
    private Integer distritoId;
    private String distritoDescripcion;
    private String contactoEmergenciaNombre;
    private String contactoEmergenciaTelefono;
    private String notasAdicionales;

    // Paseador (null if not PASEADOR)
    private Long paseadorId;
    private String paseadorUuid;
    private Integer tipoDocumentoId;
    private String numeroDocumento;
    private String biografia;
    private BigDecimal tarifaHoraPen;
    private Integer distritoCoberturaId;
    private String distritoCoberturaDescripcion;
    private Integer estadoVerificacionId;
    private String estadoVerificacionDescripcion;
    private Integer paseosCompletados;
    private BigDecimal calificacionPromedio;
}
