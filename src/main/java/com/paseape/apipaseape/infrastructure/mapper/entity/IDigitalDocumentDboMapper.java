package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.DigitalDocument;
import com.paseape.apipaseape.domain.entity.DigitalDocumentType;
import com.paseape.apipaseape.domain.entity.FolderDigitalDocument;
import com.paseape.apipaseape.infrastructure.entity.DigitalDocumentEntity;
import com.paseape.apipaseape.infrastructure.entity.DigitalDocumentTypeEntity;
import com.paseape.apipaseape.infrastructure.entity.FolderDigitalDocumentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IDigitalDocumentDboMapper {

    DigitalDocumentType toDomain(DigitalDocumentTypeEntity entity);
    DigitalDocumentTypeEntity toEntity(DigitalDocumentType domain);

    FolderDigitalDocument toDomain(FolderDigitalDocumentEntity entity);
    FolderDigitalDocumentEntity toEntity(FolderDigitalDocument domain);

    DigitalDocument toDomain(DigitalDocumentEntity entity);
    DigitalDocumentEntity toEntity(DigitalDocument domain);
    List<DigitalDocument> toDomainList(List<DigitalDocumentEntity> entities);
}