package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.TipoTamanoMascota;
import com.paseape.apipaseape.infrastructure.entity.TipoTamanoMascotaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ITipoTamanoMascotaDboMapper {

    TipoTamanoMascota toDomain(TipoTamanoMascotaEntity entity);

    TipoTamanoMascotaEntity toEntity(TipoTamanoMascota domain);

    List<TipoTamanoMascota> toDomainList(List<TipoTamanoMascotaEntity> entities);
}
