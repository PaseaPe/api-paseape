package com.paseape.apipaseape.infrastructure.service;

import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import com.paseape.apipaseape.application.service.IStorageService;
import com.paseape.apipaseape.infrastructure.dto.response.FileDownloadResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OciStorageService implements IStorageService {

    private final ObjectStorageClient objectStorageClient;

    @Value("${app.oci.namespace}")
    private String namespace;

    @Value("${app.oci.bucket-name}")
    private String bucketName;

    @Override
    public String uploadFile(String folder, MultipartFile file) throws BadRequestException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("El archivo proporcionado está vacío o es nulo.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String cleanFolder = StringUtils.hasText(folder) ? folder.trim().replaceAll("^/|/$", "") + "/" : "";
        String objectName = cleanFolder + UUID.randomUUID() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .namespaceName(namespace)
                    .bucketName(bucketName)
                    .objectName(objectName)
                    .contentLength(file.getSize())
                    .contentType(file.getContentType())
                    .putObjectBody(inputStream)
                    .build();

            objectStorageClient.putObject(putObjectRequest);
            log.info("[OCI-STORAGE] Archivo subido exitosamente: {}", objectName);
            return objectName;
        } catch (Exception ex) {
            log.error("[OCI-STORAGE] Error subiendo archivo {}: {}", objectName, ex.getMessage(), ex);
            throw new BadRequestException("No se pudo almacenar el archivo en OCI Object Storage.");
        }
    }

    @Override
    public FileDownloadResDto downloadFile(String objectName) throws BadRequestException {
        if (!StringUtils.hasText(objectName)) {
            throw new BadRequestException("El nombre del objeto en storage es obligatorio.");
        }

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .namespaceName(namespace)
                    .bucketName(bucketName)
                    .objectName(objectName.trim())
                    .build();

            GetObjectResponse response = objectStorageClient.getObject(getObjectRequest);
            String fileName = objectName.contains("/") ? objectName.substring(objectName.lastIndexOf("/") + 1) : objectName;

            return FileDownloadResDto.builder()
                    .fileName(fileName)
                    .contentType(response.getContentType())
                    .contentLength(response.getContentLength())
                    .content(response.getInputStream())
                    .build();
        } catch (Exception ex) {
            log.error("[OCI-STORAGE] Error descargando archivo {}: {}", objectName, ex.getMessage(), ex);
            throw new BadRequestException("No se encontró o no se pudo descargar el archivo solicitado.");
        }
    }
}