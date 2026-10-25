package com.paseape.apipaseape.infrastructure.mapper.dto;

import com.paseape.apipaseape.domain.entity.Notificacion;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.dto.response.NotificacionResDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.springframework.util.StringUtils;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface INotificacionDtoMapper {

    @Mapping(target = "tipoNotificacionId", source = "tipoNotificacion.id")
    @Mapping(target = "tipoNotificacionDescripcion", source = "tipoNotificacion.descripcion")
    @Mapping(target = "remitenteUuid", source = "remitenteUsuario.uuid")
    @Mapping(target = "remitenteNombreCompleto", source = "remitenteUsuario", qualifiedByName = "resolverNombreRemitente")
    NotificacionResDto toResDto(Notificacion domain);

    List<NotificacionResDto> toResDtoList(List<Notificacion> domains);

    @Named("resolverNombreRemitente")
    default String resolverNombreRemitente(Usuario remitente) {
        if (remitente == null) {
            return null;
        }
        String nombres = StringUtils.hasText(remitente.getNombres()) ? remitente.getNombres().trim() : "";
        String apellidos = StringUtils.hasText(remitente.getApellidos()) ? " " + remitente.getApellidos().trim() : "";
        String nombreCompleto = (nombres + apellidos).trim();
        return StringUtils.hasText(nombreCompleto) ? nombreCompleto : null;
    }
}