package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.SolicitudPaseoRecorrido;
import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoRecorridoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                ISolicitudPaseoDboMapper.class,
                ITipoOrigenTrazaDboMapper.class
        }
)
public interface ISolicitudPaseoRecorridoDboMapper {

    SolicitudPaseoRecorrido toDomain(SolicitudPaseoRecorridoEntity entity);

    SolicitudPaseoRecorridoEntity toEntity(SolicitudPaseoRecorrido domain);
}
