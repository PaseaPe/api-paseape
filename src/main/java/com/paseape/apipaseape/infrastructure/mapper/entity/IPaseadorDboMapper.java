package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.infrastructure.entity.PaseadorEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                IUsuarioDboMapper.class,
                ITipoDocumentoDboMapper.class,
                IDistritoLimaDboMapper.class,
                IPaseadorEstadoVerificacionDboMapper.class
        }
)
public interface IPaseadorDboMapper {

    Paseador toDomain(PaseadorEntity entity);

    PaseadorEntity toEntity(Paseador domain);

    List<Paseador> toDomainList(List<PaseadorEntity> entities);
}
