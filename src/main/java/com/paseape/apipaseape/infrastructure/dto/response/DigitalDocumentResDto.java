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
public class DigitalDocumentResDto {
    private Long id;
    private String uuid;
    private Integer tipoDocumentoId;
    private String tipoDocumentoDescripcion;
    private String objectName;
    private String nombreOriginal;
    private String contentType;
    private Long tamanoBytes;
}