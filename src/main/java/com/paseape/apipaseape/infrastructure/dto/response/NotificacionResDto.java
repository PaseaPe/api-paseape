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
public class NotificacionResDto {

    private String uuid;
    private Integer tipoNotificacionId;
    private String tipoNotificacionDescripcion;
    private String titulo;
    private String mensaje;
    private Long referenciaId;
    private String referenciaTipo;
    private String remitenteUuid;
    private String remitenteNombreCompleto;
    private Boolean leido;
    private LocalDateTime fechaLeido;
    private LocalDateTime creadoEl;
}