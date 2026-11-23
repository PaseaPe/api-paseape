package com.paseape.apipaseape.domain.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DigitalDocument {
    private Long id;
    private String uuid;
    private FolderDigitalDocument folderDigitalDocument;
    private DigitalDocumentType digitalDocumentType;
    private String objectName;
    private String nombreOriginal;
    private String contentType;
    private Long tamanoBytes;
    private Integer estado;
    private LocalDateTime creadoEl;
    private LocalDateTime actualizadoEl;
    private String creadoPor;
    private String actualizadoPor;
}