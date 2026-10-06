package com.paseape.apipaseape.infrastructure.mapper.entity;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.paseape.apipaseape.domain.entity.UsuarioEstado;
import com.paseape.apipaseape.infrastructure.entity.UsuarioEstadoEntity;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IUsuarioEstadoDboMapper {

    UsuarioEstado toDomain(UsuarioEstadoEntity entity);

    UsuarioEstadoEntity toEntity(UsuarioEstado domain);

    List<UsuarioEstado> toDomainList(List<UsuarioEstadoEntity> entities);
}