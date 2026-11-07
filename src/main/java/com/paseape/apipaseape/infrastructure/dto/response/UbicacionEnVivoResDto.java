package com.paseape.apipaseape.infrastructure.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UbicacionEnVivoResDto {
    private String uuid;
    private String solicitudPaseoUuid;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private BigDecimal precisionM;
    private BigDecimal velocidadMs;
    private BigDecimal rumboGrados;
    private LocalDateTime fechaHoraGps;
    private Boolean senalDegradada;
}
