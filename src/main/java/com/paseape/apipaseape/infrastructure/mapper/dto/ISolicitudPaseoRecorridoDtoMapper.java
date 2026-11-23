package com.paseape.apipaseape.infrastructure.mapper.dto;

import com.paseape.apipaseape.domain.entity.SolicitudPaseoRecorrido;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudPaseoRecorridoResDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ISolicitudPaseoRecorridoDtoMapper {

    @Mapping(target = "solicitudPaseoUuid", source = "domain.solicitudPaseo.uuid")
    @Mapping(target = "tipoOrigenTrazaDescripcion", source = "domain.tipoOrigenTraza.descripcion")
    @Mapping(target = "fechaHoraInicio", source = "domain.solicitudPaseo.fechaHoraInicioReal")
    @Mapping(target = "fechaHoraFin", source = "domain.solicitudPaseo.fechaHoraFinReal")
    SolicitudPaseoRecorridoResDto toResDto(SolicitudPaseoRecorrido domain);
}
