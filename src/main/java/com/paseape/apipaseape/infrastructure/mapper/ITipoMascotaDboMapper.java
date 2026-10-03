package com.paseape.apipaseape.infrastructure.mapper;

import com.paseape.apipaseape.domain.entity.TipoMascota;
import com.paseape.apipaseape.infrastructure.entity.TipoMascotaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ITipoMascotaDboMapper {

    TipoMascota toDomain(TipoMascotaEntity entity);

    TipoMascotaEntity toEntity(TipoMascota domain);

    List<TipoMascota> toDomainList(List<TipoMascotaEntity> entities);
}
