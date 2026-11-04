package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.TipoDuracionPaseo;
import com.paseape.apipaseape.infrastructure.entity.TipoDuracionPaseoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ITipoDuracionPaseoDboMapper {

    TipoDuracionPaseo toDomain(TipoDuracionPaseoEntity entity);

    TipoDuracionPaseoEntity toEntity(TipoDuracionPaseo domain);

    List<TipoDuracionPaseo> toDomainList(List<TipoDuracionPaseoEntity> entities);
}
