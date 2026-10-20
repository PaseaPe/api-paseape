package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.DiaSemana;
import com.paseape.apipaseape.infrastructure.entity.DiaSemanaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IDiaSemanaDboMapper {

    DiaSemana toDomain(DiaSemanaEntity entity);

    DiaSemanaEntity toEntity(DiaSemana domain);

    List<DiaSemana> toDomainList(List<DiaSemanaEntity> entities);
}
