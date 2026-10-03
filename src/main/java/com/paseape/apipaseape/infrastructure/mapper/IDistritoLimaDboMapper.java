package com.paseape.apipaseape.infrastructure.mapper;

import com.paseape.apipaseape.domain.entity.DistritoLima;
import com.paseape.apipaseape.infrastructure.entity.DistritoLimaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IDistritoLimaDboMapper {

    DistritoLima toDomain(DistritoLimaEntity entity);

    DistritoLimaEntity toEntity(DistritoLima domain);

    List<DistritoLima> toDomainList(List<DistritoLimaEntity> entities);
}
