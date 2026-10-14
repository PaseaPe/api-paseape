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
public class ClienteResDto {

    private Long id;
    private String uuid;
    private String usuarioUuid;
    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;
    private String fotoPerfilUrl;
    private String direccionReferencia;
    private Integer distritoId;
    private String distritoDescripcion;
    private String contactoEmergenciaNombre;
    private String contactoEmergenciaTelefono;
    private String notasAdicionales;
    private Integer estado;
}