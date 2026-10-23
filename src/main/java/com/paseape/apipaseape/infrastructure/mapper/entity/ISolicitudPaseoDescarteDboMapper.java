package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.SolicitudPaseoDescarte;
import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoDescarteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                ISolicitudPaseoDboMapper.class,
                IPaseadorDboMapper.class
        }
)
public interface ISolicitudPaseoDescarteDboMapper {

    SolicitudPaseoDescarte toDomain(SolicitudPaseoDescarteEntity entity);

    SolicitudPaseoDescarteEntity toEntity(SolicitudPaseoDescarte domain);
}
