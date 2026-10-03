package com.paseape.apipaseape.infrastructure.mapper;

import com.paseape.apipaseape.domain.entity.TipoDocumento;
import com.paseape.apipaseape.infrastructure.entity.TipoDocumentoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ITipoDocumentoDboMapper {

    TipoDocumento toDomain(TipoDocumentoEntity entity);

    TipoDocumentoEntity toEntity(TipoDocumento domain);

    List<TipoDocumento> toDomainList(List<TipoDocumentoEntity> entities);
}
