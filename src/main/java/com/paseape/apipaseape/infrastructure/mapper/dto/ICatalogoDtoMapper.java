package com.paseape.apipaseape.infrastructure.mapper.dto;

import com.paseape.apipaseape.domain.entity.DistritoLima;
import com.paseape.apipaseape.domain.entity.TipoGeneroMascota;
import com.paseape.apipaseape.domain.entity.TipoMascota;
import com.paseape.apipaseape.domain.entity.TipoNivelEnergia;
import com.paseape.apipaseape.domain.entity.TipoRaza;
import com.paseape.apipaseape.domain.entity.TipoTamanoMascota;
import com.paseape.apipaseape.infrastructure.dto.response.catalogo.CatalogoAdminResDto;
import com.paseape.apipaseape.infrastructure.dto.response.catalogo.DistritoLimaAdminResDto;
import com.paseape.apipaseape.infrastructure.dto.response.catalogo.TipoRazaAdminResDto;
import com.paseape.apipaseape.infrastructure.dto.response.catalogo.TipoTamanoMascotaAdminResDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ICatalogoDtoMapper {

    DistritoLimaAdminResDto toDistritoResDto(DistritoLima domain);

    List<DistritoLimaAdminResDto> toDistritoResDtoList(List<DistritoLima> domains);

    CatalogoAdminResDto toTipoMascotaResDto(TipoMascota domain);

    List<CatalogoAdminResDto> toTipoMascotaResDtoList(List<TipoMascota> domains);

    @Mapping(target = "tipoMascotaUuid", source = "tipoMascota.uuid")
    @Mapping(target = "tipoMascotaDescripcion", source = "tipoMascota.descripcion")
    TipoRazaAdminResDto toTipoRazaResDto(TipoRaza domain);

    List<TipoRazaAdminResDto> toTipoRazaResDtoList(List<TipoRaza> domains);

    TipoTamanoMascotaAdminResDto toTipoTamanoResDto(TipoTamanoMascota domain);

    List<TipoTamanoMascotaAdminResDto> toTipoTamanoResDtoList(List<TipoTamanoMascota> domains);

    CatalogoAdminResDto toTipoNivelEnergiaResDto(TipoNivelEnergia domain);

    List<CatalogoAdminResDto> toTipoNivelEnergiaResDtoList(List<TipoNivelEnergia> domains);

    CatalogoAdminResDto toTipoGeneroResDto(TipoGeneroMascota domain);

    List<CatalogoAdminResDto> toTipoGeneroResDtoList(List<TipoGeneroMascota> domains);
}
