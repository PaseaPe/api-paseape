package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.Notificacion;
import com.paseape.apipaseape.infrastructure.entity.NotificacionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                IUsuarioDboMapper.class,
                ITipoNotificacionDboMapper.class
        }
)
public interface INotificacionDboMapper {

    Notificacion toDomain(NotificacionEntity entity);

    NotificacionEntity toEntity(Notificacion domain);

    List<Notificacion> toDomainList(List<NotificacionEntity> entities);
}
