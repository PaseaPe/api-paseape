package com.paseape.apipaseape.infrastructure.mapper;

import com.paseape.apipaseape.domain.entity.TipoRaza;
import com.paseape.apipaseape.infrastructure.entity.TipoRazaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ITipoMascotaDboMapper.class}
)
public interface ITipoRazaDboMapper {

    TipoRaza toDomain(TipoRazaEntity entity);

    TipoRazaEntity toEntity(TipoRaza domain);

    List<TipoRaza> toDomainList(List<TipoRazaEntity> entities);
}
