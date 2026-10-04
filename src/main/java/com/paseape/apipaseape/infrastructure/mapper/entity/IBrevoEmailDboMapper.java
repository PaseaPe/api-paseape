package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.BrevoEmail;
import com.paseape.apipaseape.infrastructure.entity.BrevoEmailEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {IUsuarioDboMapper.class}
)
public interface IBrevoEmailDboMapper {

    BrevoEmail toDomain(BrevoEmailEntity entity);

    BrevoEmailEntity toEntity(BrevoEmail domain);

    List<BrevoEmail> toDomainList(List<BrevoEmailEntity> entities);
}
