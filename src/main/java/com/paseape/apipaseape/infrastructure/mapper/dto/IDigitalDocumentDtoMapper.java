package com.paseape.apipaseape.infrastructure.mapper.dto;

import com.paseape.apipaseape.domain.entity.DigitalDocument;
import com.paseape.apipaseape.infrastructure.dto.response.DigitalDocumentResDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IDigitalDocumentDtoMapper {

    @Mapping(target = "tipoDocumentoId", source = "digitalDocumentType.id")
    @Mapping(target = "tipoDocumentoDescripcion", source = "digitalDocumentType.descripcion")
    DigitalDocumentResDto toResDto(DigitalDocument domain);

    List<DigitalDocumentResDto> toResDtoList(List<DigitalDocument> domainList);
}