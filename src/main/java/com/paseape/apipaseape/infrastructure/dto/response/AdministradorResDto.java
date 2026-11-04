package com.paseape.apipaseape.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AdministradorResDto {

    private Long id;
    private String uuid;
    private String usuarioUuid;
    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;
    private String fotoPerfilUrl;
    private String codigoEmpleado;
    private String areaDepartamento;
    private Integer superadmin;
    private Integer usuarioEstadoId;
    private String usuarioEstadoDescripcion;
    private Integer estado;
    private LocalDateTime creadoEl;
}