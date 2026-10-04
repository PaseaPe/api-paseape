package com.paseape.apipaseape.infrastructure.mapper.dto;

import com.paseape.apipaseape.infrastructure.dto.response.MascotaResDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.paseape.apipaseape.domain.entity.Mascota;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IMascotaDtoMapper {

    @Mapping(target = "clienteId", source = "cliente.id")
    @Mapping(target = "tipoMascotaId", source = "tipoMascota.id")
    @Mapping(target = "tipoMascotaDescripcion", source = "tipoMascota.descripcion")
    @Mapping(target = "tipoRazaId", source = "tipoRaza.id")
    @Mapping(target = "tipoRazaDescripcion", source = "tipoRaza.descripcion")
    @Mapping(target = "tipoGeneroMascotaId", source = "tipoGeneroMascota.id")
    @Mapping(target = "tipoGeneroMascotaDescripcion", source = "tipoGeneroMascota.descripcion")
    @Mapping(target = "tipoTamanoMascotaId", source = "tipoTamanoMascota.id")
    @Mapping(target = "tipoTamanoMascotaDescripcion", source = "tipoTamanoMascota.descripcion")
    @Mapping(target = "tipoNivelEnergiaId", source = "tipoNivelEnergia.id")
    @Mapping(target = "tipoNivelEnergiaDescripcion", source = "tipoNivelEnergia.descripcion")
    MascotaResDto toResDto(Mascota domain);

    List<MascotaResDto> toResDtoList(List<Mascota> domainList);
}
