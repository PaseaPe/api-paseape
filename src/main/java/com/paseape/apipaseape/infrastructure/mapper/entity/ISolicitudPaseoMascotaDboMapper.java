package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.SolicitudPaseoMascota;
import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoMascotaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                ISolicitudPaseoDboMapper.class,
                IMascotaDboMapper.class
        }
)
public interface ISolicitudPaseoMascotaDboMapper {

    SolicitudPaseoMascota toDomain(SolicitudPaseoMascotaEntity entity);

    SolicitudPaseoMascotaEntity toEntity(SolicitudPaseoMascota domain);

    List<SolicitudPaseoMascota> toDomainList(List<SolicitudPaseoMascotaEntity> entities);
}
