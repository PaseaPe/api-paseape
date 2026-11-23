package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.FolderDigitalDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IFolderDigitalDocumentJpaRepository extends JpaRepository<FolderDigitalDocumentEntity, Long> {
    FolderDigitalDocumentEntity findById(long id);
    FolderDigitalDocumentEntity findByUuid(String uuid);
}