package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.SolicitudPaseo;
import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                ITipoModalidadPaseoDboMapper.class,
                ITipoMetodoPagoDboMapper.class,
                IClienteDboMapper.class,
                IPaseadorDboMapper.class,
                IDistritoLimaDboMapper.class,
                IBloqueHorarioDboMapper.class,
                ITipoDuracionPaseoDboMapper.class,
                ISolicitudPaseoEstadoDboMapper.class
        }
)
public interface ISolicitudPaseoDboMapper {

    SolicitudPaseo toDomain(SolicitudPaseoEntity entity);

    SolicitudPaseoEntity toEntity(SolicitudPaseo domain);

    List<SolicitudPaseo> toDomainList(List<SolicitudPaseoEntity> entities);
}
