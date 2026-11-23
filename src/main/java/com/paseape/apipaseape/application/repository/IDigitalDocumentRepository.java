package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.DigitalDocument;
import java.util.List;

public interface IDigitalDocumentRepository {
    DigitalDocument findById(Long id);
    DigitalDocument findByUuid(String uuid);
    List<DigitalDocument> findAllByFolderId(Long folderId);
    DigitalDocument save(DigitalDocument document);
    DigitalDocument findByFolderIdAndDigitalDocumentTypeId(Long folderId, Integer digitalDocumentTypeId);
}