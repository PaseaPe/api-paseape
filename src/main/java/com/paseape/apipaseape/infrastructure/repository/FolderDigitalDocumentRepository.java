package com.paseape.apipaseape.infrastructure.repository;

import com.paseape.apipaseape.application.repository.IFolderDigitalDocumentRepository;
import com.paseape.apipaseape.domain.entity.FolderDigitalDocument;
import com.paseape.apipaseape.infrastructure.entity.FolderDigitalDocumentEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.IDigitalDocumentDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.IFolderDigitalDocumentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class FolderDigitalDocumentRepository implements IFolderDigitalDocumentRepository {

    private final IFolderDigitalDocumentJpaRepository jpaRepository;
    private final IDigitalDocumentDboMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public FolderDigitalDocument findById(Long id) {
        if (id == null) return null;
        FolderDigitalDocumentEntity entity = jpaRepository.findById(id.longValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public FolderDigitalDocument findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) return null;
        FolderDigitalDocumentEntity entity = jpaRepository.findByUuid(uuid.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional
    public FolderDigitalDocument save(FolderDigitalDocument folder) {
        FolderDigitalDocumentEntity entity = mapper.toEntity(folder);
        FolderDigitalDocumentEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}