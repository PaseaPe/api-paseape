package com.paseape.apipaseape.infrastructure.mapper.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.dto.response.UsuarioAdminResDto;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IUsuarioAdminDtoMapper {

    @Mapping(target = "tipoUsuarioId", source = "tipoUsuario.id")
    @Mapping(target = "tipoUsuarioDescripcion", source = "tipoUsuario.descripcion")
    @Mapping(target = "usuarioEstadoId", source = "usuarioEstado.id")
    @Mapping(target = "usuarioEstadoDescripcion", source = "usuarioEstado.descripcion")
    UsuarioAdminResDto toResDto(Usuario domain);

    List<UsuarioAdminResDto> toResDtoList(List<Usuario> domainList);
}
