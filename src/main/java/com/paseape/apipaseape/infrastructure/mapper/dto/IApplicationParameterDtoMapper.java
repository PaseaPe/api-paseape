package com.paseape.apipaseape.infrastructure.mapper.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.paseape.apipaseape.domain.entity.DistritoLima;
import com.paseape.apipaseape.domain.entity.TipoDocumento;
import com.paseape.apipaseape.domain.entity.TipoGeneroMascota;
import com.paseape.apipaseape.domain.entity.TipoMascota;
import com.paseape.apipaseape.domain.entity.TipoNivelEnergia;
import com.paseape.apipaseape.domain.entity.TipoRaza;
import com.paseape.apipaseape.domain.entity.TipoTamanoMascota;
import com.paseape.apipaseape.domain.entity.TipoUsuario;
import com.paseape.apipaseape.infrastructure.dto.response.parameter.CatalogoItemResDto;
import com.paseape.apipaseape.infrastructure.dto.response.parameter.DistritoItemResDto;
import com.paseape.apipaseape.infrastructure.dto.response.parameter.TipoDocumentoItemResDto;
import com.paseape.apipaseape.infrastructure.dto.response.parameter.TipoRazaItemResDto;
import com.paseape.apipaseape.infrastructure.dto.response.parameter.TipoTamanoMascotaItemResDto;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IApplicationParameterDtoMapper {

    CatalogoItemResDto toCatalogoItemResDto(TipoUsuario entity);
    List<CatalogoItemResDto> toTipoUsuarioDtoList(List<TipoUsuario> entities);

    TipoDocumentoItemResDto toTipoDocumentoItemResDto(TipoDocumento entity);
    List<TipoDocumentoItemResDto> toTipoDocumentoDtoList(List<TipoDocumento> entities);

    DistritoItemResDto toDistritoItemResDto(DistritoLima entity);
    List<DistritoItemResDto> toDistritoDtoList(List<DistritoLima> entities);

    CatalogoItemResDto toCatalogoItemResDto(TipoMascota entity);
    List<CatalogoItemResDto> toTipoMascotaDtoList(List<TipoMascota> entities);

    @Mapping(target = "tipoMascotaId", source = "tipoMascota.id")
    TipoRazaItemResDto toTipoRazaItemResDto(TipoRaza entity);
    List<TipoRazaItemResDto> toTipoRazaDtoList(List<TipoRaza> entities);

    CatalogoItemResDto toCatalogoItemResDto(TipoGeneroMascota entity);
    List<CatalogoItemResDto> toTipoGeneroMascotaDtoList(List<TipoGeneroMascota> entities);

    TipoTamanoMascotaItemResDto toTipoTamanoMascotaItemResDto(TipoTamanoMascota entity);
    List<TipoTamanoMascotaItemResDto> toTipoTamanoMascotaDtoList(List<TipoTamanoMascota> entities);

    CatalogoItemResDto toCatalogoItemResDto(TipoNivelEnergia entity);
    List<CatalogoItemResDto> toTipoNivelEnergiaDtoList(List<TipoNivelEnergia> entities);
}
