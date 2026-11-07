package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.SolicitudPaseoUbicacionEnVivo;
import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoUbicacionEnVivoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                ISolicitudPaseoDboMapper.class
        }
)
public interface ISolicitudPaseoUbicacionEnVivoDboMapper {

    SolicitudPaseoUbicacionEnVivo toDomain(SolicitudPaseoUbicacionEnVivoEntity entity);

    SolicitudPaseoUbicacionEnVivoEntity toEntity(SolicitudPaseoUbicacionEnVivo domain);
}
