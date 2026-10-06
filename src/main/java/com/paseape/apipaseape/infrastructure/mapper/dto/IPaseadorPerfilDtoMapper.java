package com.paseape.apipaseape.infrastructure.mapper.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.infrastructure.dto.response.PaseadorPerfilResDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IPaseadorPerfilDtoMapper {

    @Mapping(target = "nombres", source = "usuario.nombres")
    @Mapping(target = "apellidos", source = "usuario.apellidos")
    @Mapping(target = "fotoPerfilUrl", source = "usuario.fotoPerfilUrl")
    @Mapping(target = "distritoCoberturaId", source = "distritoCobertura.id")
    @Mapping(target = "distritoCoberturaDescripcion", source = "distritoCobertura.descripcion")
    PaseadorPerfilResDto toResDto(Paseador domain);
}
