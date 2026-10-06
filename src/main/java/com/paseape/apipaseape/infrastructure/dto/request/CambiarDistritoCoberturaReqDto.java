package com.paseape.apipaseape.infrastructure.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CambiarDistritoCoberturaReqDto {

    @NotBlank(message = "El UUID del usuario es obligatorio")
    @Size(max = 36, message = "El UUID no debe superar los 36 caracteres")
    private String usuarioUuid;

    @NotNull(message = "El ID del distrito es obligatorio")
    @Min(value = 1, message = "El ID del distrito debe ser mayor a 0")
    private Integer distritoId;
}
