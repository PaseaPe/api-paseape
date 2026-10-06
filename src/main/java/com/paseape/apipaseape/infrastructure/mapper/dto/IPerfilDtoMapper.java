package com.paseape.apipaseape.infrastructure.mapper.dto;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.dto.response.PerfilResDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IPerfilDtoMapper {

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "nombres", source = "nombres")
    @Mapping(target = "apellidos", source = "apellidos")
    @Mapping(target = "correo", source = "correo")
    @Mapping(target = "telefono", source = "telefono")
    @Mapping(target = "fotoPerfilUrl", source = "fotoPerfilUrl")
    @Mapping(target = "tipoUsuarioId", source = "tipoUsuario.id")
    @Mapping(target = "tipoUsuarioDescripcion", source = "tipoUsuario.descripcion")
    @Mapping(target = "usuarioEstadoId", source = "usuarioEstado.id")
    @Mapping(target = "usuarioEstadoDescripcion", source = "usuarioEstado.descripcion")
    PerfilResDto toPerfilResDto(Usuario usuario);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "clienteId", source = "id")
    @Mapping(target = "clienteUuid", source = "uuid")
    @Mapping(target = "direccionReferencia", source = "direccionReferencia")
    @Mapping(target = "distritoId", source = "distrito.id")
    @Mapping(target = "distritoDescripcion", source = "distrito.descripcion")
    @Mapping(target = "contactoEmergenciaNombre", source = "contactoEmergenciaNombre")
    @Mapping(target = "contactoEmergenciaTelefono", source = "contactoEmergenciaTelefono")
    @Mapping(target = "notasAdicionales", source = "notasAdicionales")
    PerfilResDto enrichClienteFields(Cliente cliente, @MappingTarget PerfilResDto dto);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "paseadorId", source = "id")
    @Mapping(target = "paseadorUuid", source = "uuid")
    @Mapping(target = "tipoDocumentoId", source = "tipoDocumento.id")
    @Mapping(target = "numeroDocumento", source = "numeroDocumento")
    @Mapping(target = "biografia", source = "biografia")
    @Mapping(target = "tarifaHoraPen", source = "tarifaHoraPen")
    @Mapping(target = "distritoCoberturaId", source = "distritoCobertura.id")
    @Mapping(target = "distritoCoberturaDescripcion", source = "distritoCobertura.descripcion")
    @Mapping(target = "estadoVerificacionId", source = "estadoVerificacion.id")
    @Mapping(target = "estadoVerificacionDescripcion", source = "estadoVerificacion.descripcion")
    @Mapping(target = "paseosCompletados", source = "paseosCompletados")
    @Mapping(target = "calificacionPromedio", source = "calificacionPromedio")
    PerfilResDto enrichPaseadorFields(Paseador paseador, @MappingTarget PerfilResDto dto);
}
