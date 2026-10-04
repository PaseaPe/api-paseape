package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.infrastructure.entity.ClienteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                IUsuarioDboMapper.class,
                IDistritoLimaDboMapper.class
        }
)
public interface IClienteDboMapper {

    Cliente toDomain(ClienteEntity entity);

    ClienteEntity toEntity(Cliente domain);

    List<Cliente> toDomainList(List<ClienteEntity> entities);
}
