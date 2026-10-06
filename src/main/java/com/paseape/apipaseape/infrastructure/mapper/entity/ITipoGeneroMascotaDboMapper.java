package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.TipoGeneroMascota;
import com.paseape.apipaseape.infrastructure.entity.TipoGeneroMascotaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ITipoGeneroMascotaDboMapper {

    TipoGeneroMascota toDomain(TipoGeneroMascotaEntity entity);

    TipoGeneroMascotaEntity toEntity(TipoGeneroMascota domain);

    List<TipoGeneroMascota> toDomainList(List<TipoGeneroMascotaEntity> entities);
}
