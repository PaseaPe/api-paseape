package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.FolderDigitalDocument;

public interface IFolderDigitalDocumentRepository {
    FolderDigitalDocument findById(Long id);
    FolderDigitalDocument findByUuid(String uuid);
    FolderDigitalDocument save(FolderDigitalDocument folder);
}