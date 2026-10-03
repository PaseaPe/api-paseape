package com.paseape.apipaseape.infrastructure.mapper;

import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.entity.UsuarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                ITipoUsuarioDboMapper.class,
                IUsuarioEstadoDboMapper.class,
                ITipoProveedorAuthDboMapper.class
        }
)
public interface IUsuarioDboMapper {

    Usuario toDomain(UsuarioEntity entity);

    UsuarioEntity toEntity(Usuario domain);

    List<Usuario> toDomainList(List<UsuarioEntity> entities);
}
