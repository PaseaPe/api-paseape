package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.BloqueHorario;
import com.paseape.apipaseape.infrastructure.entity.BloqueHorarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IBloqueHorarioDboMapper {

    BloqueHorario toDomain(BloqueHorarioEntity entity);

    BloqueHorarioEntity toEntity(BloqueHorario domain);

    List<BloqueHorario> toDomainList(List<BloqueHorarioEntity> entities);
}
