package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.TipoNotificacion;
import com.paseape.apipaseape.infrastructure.entity.TipoNotificacionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ITipoNotificacionDboMapper {

    TipoNotificacion toDomain(TipoNotificacionEntity entity);

    TipoNotificacionEntity toEntity(TipoNotificacion domain);

    List<TipoNotificacion> toDomainList(List<TipoNotificacionEntity> entities);
}
