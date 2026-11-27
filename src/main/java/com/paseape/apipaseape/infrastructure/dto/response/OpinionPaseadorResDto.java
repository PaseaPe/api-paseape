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
public class OpinionPaseadorResDto {

    private String uuid;
    private String solicitudPaseoUuid;
    private String clienteUuid;
    private String clienteNombreCompleto;
    private Integer puntuacion;
    private String comentario;
    private LocalDateTime fechaComentario;
    private LocalDateTime creadoEl;
}
