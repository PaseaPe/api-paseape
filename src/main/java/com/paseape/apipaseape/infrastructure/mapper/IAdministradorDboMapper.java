package com.paseape.apipaseape.infrastructure.mapper;

import com.paseape.apipaseape.domain.entity.Administrador;
import com.paseape.apipaseape.infrastructure.entity.AdministradorEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {IUsuarioDboMapper.class}
)
public interface IAdministradorDboMapper {

    Administrador toDomain(AdministradorEntity entity);

    AdministradorEntity toEntity(Administrador domain);

    List<Administrador> toDomainList(List<AdministradorEntity> entities);
}
