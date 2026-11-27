package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.PaseoCalificacion;
import com.paseape.apipaseape.infrastructure.entity.PaseoCalificacionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                ISolicitudPaseoDboMapper.class,
                IClienteDboMapper.class,
                IPaseadorDboMapper.class
        }
)
public interface IPaseoCalificacionDboMapper {

    PaseoCalificacion toDomain(PaseoCalificacionEntity entity);

    PaseoCalificacionEntity toDbo(PaseoCalificacion domain);

    List<PaseoCalificacion> toDomainList(List<PaseoCalificacionEntity> entities);
}
