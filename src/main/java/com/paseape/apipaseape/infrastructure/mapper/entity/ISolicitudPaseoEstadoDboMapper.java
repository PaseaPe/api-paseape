package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.SolicitudPaseoEstado;
import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoEstadoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ISolicitudPaseoEstadoDboMapper {

    SolicitudPaseoEstado toDomain(SolicitudPaseoEstadoEntity entity);

    SolicitudPaseoEstadoEntity toEntity(SolicitudPaseoEstado domain);

    List<SolicitudPaseoEstado> toDomainList(List<SolicitudPaseoEstadoEntity> entities);
}
