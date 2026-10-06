package com.paseape.apipaseape.infrastructure.mapper.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.infrastructure.dto.response.PaseadorResDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IPaseadorDtoMapper {

    @Mapping(target = "usuarioUuid", source = "usuario.uuid")
    @Mapping(target = "nombres", source = "usuario.nombres")
    @Mapping(target = "apellidos", source = "usuario.apellidos")
    @Mapping(target = "correo", source = "usuario.correo")
    @Mapping(target = "telefono", source = "usuario.telefono")
    @Mapping(target = "tipoDocumentoId", source = "tipoDocumento.id")
    @Mapping(target = "distritoCoberturaId", source = "distritoCobertura.id")
    @Mapping(target = "distritoCoberturaDescripcion", source = "distritoCobertura.descripcion")
    @Mapping(target = "estadoVerificacionId", source = "estadoVerificacion.id")
    @Mapping(target = "estadoVerificacionDescripcion", source = "estadoVerificacion.descripcion")
    PaseadorResDto toResDto(Paseador domain);
}
