package com.paseape.apipaseape.infrastructure.repository;

import com.paseape.apipaseape.application.repository.IDigitalDocumentRepository;
import com.paseape.apipaseape.domain.entity.DigitalDocument;
import com.paseape.apipaseape.infrastructure.entity.DigitalDocumentEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.IDigitalDocumentDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.IDigitalDocumentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DigitalDocumentRepository implements IDigitalDocumentRepository {

    private final IDigitalDocumentJpaRepository jpaRepository;
    private final IDigitalDocumentDboMapper mapper;

    @Override
    public DigitalDocument findById(Long id) {
        if (id == null) return null;
        DigitalDocumentEntity entity = jpaRepository.findById(id.longValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public DigitalDocument findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) return null;
        DigitalDocumentEntity entity = jpaRepository.findByUuid(uuid.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<DigitalDocument> findAllByFolderId(Long folderId) {
        if (folderId == null) return List.of();
        List<DigitalDocumentEntity> entities = jpaRepository.findByFolderDigitalDocumentId(folderId);
        return mapper.toDomainList(entities);
    }

    @Override
    public DigitalDocument save(DigitalDocument document) {
        DigitalDocumentEntity entity = mapper.toEntity(document);
        DigitalDocumentEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public DigitalDocument findByFolderIdAndDigitalDocumentTypeId(Long folderId, Integer digitalDocumentTypeId) {
        DigitalDocumentEntity entity = jpaRepository.findByFolderDigitalDocumentIdAndDigitalDocumentTypeId(folderId, digitalDocumentTypeId);
        return mapper.toDomain(entity);
    }
}