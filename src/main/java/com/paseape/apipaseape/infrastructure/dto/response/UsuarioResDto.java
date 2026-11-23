package com.paseape.apipaseape.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UsuarioResDto {

    private Long id;
    private String uuid;
    private String nombres;
    private String apellidos;
    private String correo;
    private Integer correoVerificado;
    private String telefono;
    private String fotoPerfilUrl;
    private Integer tipoUsuarioId;
    private String tipoUsuarioDescripcion;
    private Integer usuarioEstadoId;
    private String usuarioEstadoDescripcion;
    private Integer tipoProveedorAuthId;
    private String tipoProveedorAuthDescripcion;
    private Integer estado;

    private List<DigitalDocumentResDto> digitalDocuments;

    private ClienteResDto cliente;
    private PaseadorResDto paseador;
}