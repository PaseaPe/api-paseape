package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.TipoMetodoPago;
import com.paseape.apipaseape.infrastructure.entity.TipoMetodoPagoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ITipoMetodoPagoDboMapper {

    TipoMetodoPago toDomain(TipoMetodoPagoEntity entity);

    TipoMetodoPagoEntity toEntity(TipoMetodoPago domain);

    List<TipoMetodoPago> toDomainList(List<TipoMetodoPagoEntity> entities);
}
