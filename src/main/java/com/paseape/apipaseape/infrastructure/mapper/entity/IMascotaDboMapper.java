package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.Mascota;
import com.paseape.apipaseape.infrastructure.entity.MascotaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                IClienteDboMapper.class,
                ITipoMascotaDboMapper.class,
                ITipoRazaDboMapper.class,
                ITipoGeneroMascotaDboMapper.class,
                ITipoTamanoMascotaDboMapper.class,
                ITipoNivelEnergiaDboMapper.class
        }
)
public interface IMascotaDboMapper {

    Mascota toDomain(MascotaEntity entity);

    MascotaEntity toEntity(Mascota domain);

    List<Mascota> toDomainList(List<MascotaEntity> entities);
}
