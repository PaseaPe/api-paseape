package com.paseape.apipaseape.infrastructure.mapper.dto;

import com.paseape.apipaseape.domain.entity.SolicitudPaseoUbicacionEnVivo;
import com.paseape.apipaseape.infrastructure.dto.response.UbicacionEnVivoResDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ISolicitudPaseoUbicacionEnVivoDtoMapper {

    @Mapping(target = "solicitudPaseoUuid", source = "domain.solicitudPaseo.uuid")
    @Mapping(target = "latitud", source = "domain.latitudActual")
    @Mapping(target = "longitud", source = "domain.longitudActual")
    @Mapping(target = "senalDegradada", source = "senalDegradada")
    UbicacionEnVivoResDto toResDto(SolicitudPaseoUbicacionEnVivo domain, Boolean senalDegradada);

    @Mapping(target = "solicitudPaseoUuid", source = "domain.solicitudPaseo.uuid")
    @Mapping(target = "latitud", source = "domain.latitudActual")
    @Mapping(target = "longitud", source = "domain.longitudActual")
    @Mapping(target = "senalDegradada", constant = "false")
    UbicacionEnVivoResDto toResDto(SolicitudPaseoUbicacionEnVivo domain);
}
