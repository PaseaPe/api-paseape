package com.paseape.apipaseape.infrastructure.mapper.dto;

import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.infrastructure.dto.response.ClienteResDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IClienteDtoMapper {

    @Mapping(target = "usuarioUuid", source = "usuario.uuid")
    @Mapping(target = "nombres", source = "usuario.nombres")
    @Mapping(target = "apellidos", source = "usuario.apellidos")
    @Mapping(target = "correo", source = "usuario.correo")
    @Mapping(target = "telefono", source = "usuario.telefono")
    @Mapping(target = "fotoPerfilUrl", source = "usuario.fotoPerfilUrl")
    @Mapping(target = "distritoId", source = "distrito.id")
    @Mapping(target = "distritoDescripcion", source = "distrito.descripcion")
    ClienteResDto toResDto(Cliente domain);

    List<ClienteResDto> toResDtoList(List<Cliente> domainList);
}