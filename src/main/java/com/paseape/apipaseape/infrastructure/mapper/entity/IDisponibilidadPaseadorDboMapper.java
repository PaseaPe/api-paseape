package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.DisponibilidadPaseador;
import com.paseape.apipaseape.infrastructure.entity.DisponibilidadPaseadorEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                IPaseadorDboMapper.class,
                IDiaSemanaDboMapper.class,
                IBloqueHorarioDboMapper.class
        }
)
public interface IDisponibilidadPaseadorDboMapper {

    DisponibilidadPaseador toDomain(DisponibilidadPaseadorEntity entity);

    DisponibilidadPaseadorEntity toEntity(DisponibilidadPaseador domain);

    List<DisponibilidadPaseador> toDomainList(List<DisponibilidadPaseadorEntity> entities);
}
