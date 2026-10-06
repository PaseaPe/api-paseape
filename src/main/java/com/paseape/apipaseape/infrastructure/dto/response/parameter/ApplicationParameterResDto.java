package com.paseape.apipaseape.infrastructure.dto.response.parameter;

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
public class ApplicationParameterResDto {

    private List<CatalogoItemResDto> tiposUsuario;
    private List<TipoDocumentoItemResDto> tiposDocumento;
    private List<DistritoItemResDto> distritos;
    private List<CatalogoItemResDto> tiposMascota;
    private List<TipoRazaItemResDto> razas;
    private List<CatalogoItemResDto> generosMascota;
    private List<TipoTamanoMascotaItemResDto> tamanosMascota;
    private List<CatalogoItemResDto> nivelesEnergia;
}
