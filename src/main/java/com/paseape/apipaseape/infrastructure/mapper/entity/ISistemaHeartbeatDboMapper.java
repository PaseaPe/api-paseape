package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.SistemaHeartbeat;
import com.paseape.apipaseape.infrastructure.entity.SistemaHeartbeatEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ISistemaHeartbeatDboMapper {
    SistemaHeartbeat toDomain(SistemaHeartbeatEntity entity);

    SistemaHeartbeatEntity toEntity(SistemaHeartbeat domain);
}
