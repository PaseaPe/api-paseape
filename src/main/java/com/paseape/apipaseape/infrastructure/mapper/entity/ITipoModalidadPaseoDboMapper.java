package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.TipoModalidadPaseo;
import com.paseape.apipaseape.infrastructure.entity.TipoModalidadPaseoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ITipoModalidadPaseoDboMapper {

    TipoModalidadPaseo toDomain(TipoModalidadPaseoEntity entity);

    TipoModalidadPaseoEntity toEntity(TipoModalidadPaseo domain);

    List<TipoModalidadPaseo> toDomainList(List<TipoModalidadPaseoEntity> entities);
}
