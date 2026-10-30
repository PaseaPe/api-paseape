package com.paseape.apipaseape.infrastructure.mapper.dto;

import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.dto.response.ClienteResDto;
import com.paseape.apipaseape.infrastructure.dto.response.PaseadorResDto;
import com.paseape.apipaseape.infrastructure.dto.response.UsuarioResDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {IClienteDtoMapper.class, IPaseadorDtoMapper.class}
)
public interface IUsuarioDtoMapper {

    @Mapping(target = "tipoUsuarioId", source = "tipoUsuario.id")
    @Mapping(target = "tipoUsuarioDescripcion", source = "tipoUsuario.descripcion")
    @Mapping(target = "usuarioEstadoId", source = "usuarioEstado.id")
    @Mapping(target = "usuarioEstadoDescripcion", source = "usuarioEstado.descripcion")
    @Mapping(target = "tipoProveedorAuthId", source = "tipoProveedorAuth.id")
    @Mapping(target = "tipoProveedorAuthDescripcion", source = "tipoProveedorAuth.descripcion")
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "paseador", ignore = true)
    @Mapping(target = "fotoPerfilUrl", ignore = true)
    UsuarioResDto toResDto(Usuario domain);

    default UsuarioResDto toResDto(Usuario domain, Cliente cliente, Paseador paseador, IClienteDtoMapper clienteMapper, IPaseadorDtoMapper paseadorMapper) {
        if (domain == null) {
            return null;
        }

        UsuarioResDto res = toResDto(domain);

        if (cliente != null && clienteMapper != null) {
            res.setCliente(clienteMapper.toResDto(cliente));
        }

        if (paseador != null && paseadorMapper != null) {
            res.setPaseador(paseadorMapper.toResDto(paseador));
        }

        return res;
    }
}