package com.paseape.apipaseape.infrastructure.mapper.dto;

import com.paseape.apipaseape.domain.entity.Administrador;
import com.paseape.apipaseape.infrastructure.dto.response.AdministradorResDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IAdministradorDtoMapper {

    @Mapping(target = "usuarioUuid", source = "usuario.uuid")
    @Mapping(target = "nombres", source = "usuario.nombres")
    @Mapping(target = "apellidos", source = "usuario.apellidos")
    @Mapping(target = "correo", source = "usuario.correo")
    @Mapping(target = "telefono", source = "usuario.telefono")
    @Mapping(target = "fotoPerfilUrl", source = "usuario.fotoPerfilUrl")
    @Mapping(target = "usuarioEstadoId", source = "usuario.usuarioEstado.id")
    @Mapping(target = "usuarioEstadoDescripcion", source = "usuario.usuarioEstado.descripcion")
    AdministradorResDto toResDto(Administrador domain);

    List<AdministradorResDto> toResDtoList(List<Administrador> domainList);
}