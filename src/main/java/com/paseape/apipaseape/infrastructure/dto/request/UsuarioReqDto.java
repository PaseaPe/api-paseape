package com.paseape.apipaseape.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UsuarioReqDto {

    private Long id;
    private String uuid;

    private String idToken;
    private String contrasena;
    private Integer tipoUsuarioId;

    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;
    private String fotoPerfilUrl;

    private String direccionReferencia;
    private Integer distritoId;
    private String contactoEmergenciaNombre;
    private String contactoEmergenciaTelefono;
    private String notasAdicionales;

    private List<MascotaReqDto> mascotas;

    private Integer tipoDocumentoId;
    private String numeroDocumento;
    private String antecedentesPolicialesUrl;
    private Integer experienciaAnos;
    private String biografia;
    private BigDecimal tarifaHoraPen;
    private Integer distritoCoberturaId;
}
