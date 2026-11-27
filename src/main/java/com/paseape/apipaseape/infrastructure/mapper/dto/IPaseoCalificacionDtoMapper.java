package com.paseape.apipaseape.infrastructure.mapper.dto;

import com.paseape.apipaseape.domain.entity.PaseoCalificacion;
import com.paseape.apipaseape.infrastructure.dto.response.CalificacionResDto;
import com.paseape.apipaseape.infrastructure.dto.response.OpinionPaseadorResDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IPaseoCalificacionDtoMapper {

    @Mapping(target = "solicitudPaseoUuid", source = "solicitudPaseo.uuid")
    CalificacionResDto toCalificacionResDto(PaseoCalificacion domain);

    @Mapping(target = "solicitudPaseoUuid", source = "solicitudPaseo.uuid")
    @Mapping(target = "clienteUuid", source = "cliente.uuid")
    @Mapping(target = "clienteNombreCompleto", expression = "java(resolverClienteNombreCompleto(domain))")
    OpinionPaseadorResDto toOpinionPaseadorResDto(PaseoCalificacion domain);

    default String resolverClienteNombreCompleto(PaseoCalificacion domain) {
        if (domain == null || domain.getCliente() == null || domain.getCliente().getUsuario() == null) {
            return null;
        }
        var u = domain.getCliente().getUsuario();
        String nombres = u.getNombres() != null ? u.getNombres().trim() : "";
        String apellidos = u.getApellidos() != null ? u.getApellidos().trim() : "";
        String full = (nombres + " " + apellidos).trim();
        return full.isEmpty() ? null : full;
    }
}
