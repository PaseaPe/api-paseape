package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.TipoNivelEnergia;
import com.paseape.apipaseape.infrastructure.entity.TipoNivelEnergiaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ITipoNivelEnergiaDboMapper {

    TipoNivelEnergia toDomain(TipoNivelEnergiaEntity entity);

    TipoNivelEnergiaEntity toEntity(TipoNivelEnergia domain);

    List<TipoNivelEnergia> toDomainList(List<TipoNivelEnergiaEntity> entities);
}
