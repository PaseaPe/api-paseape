package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.PaseadorEstadoVerificacion;
import com.paseape.apipaseape.infrastructure.entity.PaseadorEstadoVerificacionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IPaseadorEstadoVerificacionDboMapper {

    PaseadorEstadoVerificacion toDomain(PaseadorEstadoVerificacionEntity entity);

    PaseadorEstadoVerificacionEntity toEntity(PaseadorEstadoVerificacion domain);

    List<PaseadorEstadoVerificacion> toDomainList(List<PaseadorEstadoVerificacionEntity> entities);
}
