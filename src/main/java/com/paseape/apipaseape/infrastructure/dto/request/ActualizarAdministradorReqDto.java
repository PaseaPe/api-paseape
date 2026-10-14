package com.paseape.apipaseape.infrastructure.dto.request;

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
public class ActualizarAdministradorReqDto {

    private String nombres;
    private String apellidos;
    private String telefono;
    private String fotoPerfilUrl;
    private String areaDepartamento;
    private Integer superadmin;
    private Integer usuarioEstadoId;
}