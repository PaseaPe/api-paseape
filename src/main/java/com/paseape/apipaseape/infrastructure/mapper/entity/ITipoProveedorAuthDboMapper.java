package com.paseape.apipaseape.infrastructure.mapper.entity;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.paseape.apipaseape.domain.entity.TipoProveedorAuth;
import com.paseape.apipaseape.infrastructure.entity.TipoProveedorAuthEntity;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ITipoProveedorAuthDboMapper {

    TipoProveedorAuth toDomain(TipoProveedorAuthEntity entity);

    TipoProveedorAuthEntity toEntity(TipoProveedorAuth domain);

    List<TipoProveedorAuth> toDomainList(List<TipoProveedorAuthEntity> entities);
}