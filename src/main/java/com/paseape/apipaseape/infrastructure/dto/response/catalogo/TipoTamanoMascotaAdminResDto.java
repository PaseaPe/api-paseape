package com.paseape.apipaseape.infrastructure.dto.response.catalogo;

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
public class TipoTamanoMascotaAdminResDto {

    private String uuid;
    private String descripcion;
    private String rangoPesoRef;
    private Integer estado;
    private LocalDateTime creadoEl;
    private LocalDateTime actualizadoEl;
}
