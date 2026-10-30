package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.DigitalDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDigitalDocumentJpaRepository extends JpaRepository<DigitalDocumentEntity, Long> {
    DigitalDocumentEntity findById(long id);
    DigitalDocumentEntity findByUuid(String uuid);
    List<DigitalDocumentEntity> findByFolderDigitalDocumentId(Long folderId);
    DigitalDocumentEntity findByFolderDigitalDocumentIdAndDigitalDocumentTypeId(Long folderId, Integer digitalDocumentTypeId);
}