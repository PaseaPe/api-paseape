package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.TipoUsuario;
import com.paseape.apipaseape.infrastructure.entity.TipoUsuarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ITipoUsuarioDboMapper {

    TipoUsuario toDomain(TipoUsuarioEntity entity);

    TipoUsuarioEntity toEntity(TipoUsuario domain);

    List<TipoUsuario> toDomainList(List<TipoUsuarioEntity> entities);
}