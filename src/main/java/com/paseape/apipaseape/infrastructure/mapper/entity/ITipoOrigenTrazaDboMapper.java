package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.TipoOrigenTraza;
import com.paseape.apipaseape.infrastructure.entity.TipoOrigenTrazaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ITipoOrigenTrazaDboMapper {

    TipoOrigenTraza toDomain(TipoOrigenTrazaEntity entity);

    TipoOrigenTrazaEntity toEntity(TipoOrigenTraza domain);

    List<TipoOrigenTraza> toDomainList(List<TipoOrigenTrazaEntity> entities);
}
