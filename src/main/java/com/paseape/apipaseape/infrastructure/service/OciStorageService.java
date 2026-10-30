package com.paseape.apipaseape.infrastructure.service;

import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse;
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
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

    @Value("${app.oci.region}")
    private String region;

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
    public String uploadFile(String folder, byte[] content, String originalFilename, String contentType) throws BadRequestException {
        if (content == null || content.length == 0) {
            throw new BadRequestException("El contenido del archivo no puede estar vacío.");
        }

        String extension = "";
        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String objectName = folder + "/" + UUID.randomUUID() + extension;

        try (InputStream inputStream = new ByteArrayInputStream(content)) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .namespaceName(namespace)
                    .bucketName(bucketName)
                    .objectName(objectName)
                    .contentLength((long) content.length)
                    .contentType(StringUtils.hasText(contentType) ? contentType : "application/octet-stream")
                    .putObjectBody(inputStream)
                    .build();

            objectStorageClient.putObject(putObjectRequest);
            return objectName;
        } catch (Exception ex) {
            log.error("[OCI-STORAGE] Error subiendo bytes a {}: {}", objectName, ex.getMessage(), ex);
            throw new BadRequestException("Error interno al subir el archivo al almacenamiento.");
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

    @Override
    public void deleteFile(String objectName) throws BadRequestException {
        if (!StringUtils.hasText(objectName)) {
            throw new BadRequestException("El nombre del objeto a eliminar es obligatorio.");
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .namespaceName(namespace)
                    .bucketName(bucketName)
                    .objectName(objectName.trim())
                    .build();

            objectStorageClient.deleteObject(deleteObjectRequest);
        } catch (Exception ex) {
            log.error("[OCI-STORAGE] Error eliminando archivo {}: {}", objectName, ex.getMessage(), ex);
            throw new BadRequestException("No se pudo eliminar el archivo solicitado.");
        }
    }

    @Override
    public String generatePreSignedUrl(String objectName, int expirationMinutes) throws BadRequestException {
        if (!StringUtils.hasText(objectName)) {
            throw new BadRequestException("El nombre del objeto en storage es obligatorio.");
        }

        try {
            Instant expirationInstant = Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES);
            Date expirationDate = Date.from(expirationInstant);

            CreatePreauthenticatedRequestDetails details = CreatePreauthenticatedRequestDetails.builder()
                    .name("PAR_" + UUID.randomUUID())
                    .objectName(objectName.trim())
                    .accessType(CreatePreauthenticatedRequestDetails.AccessType.ObjectRead)
                    .timeExpires(expirationDate)
                    .build();

            CreatePreauthenticatedRequestRequest request = CreatePreauthenticatedRequestRequest.builder()
                    .namespaceName(namespace)
                    .bucketName(bucketName)
                    .createPreauthenticatedRequestDetails(details)
                    .build();

            CreatePreauthenticatedRequestResponse response = objectStorageClient.createPreauthenticatedRequest(request);
            String accessUri = response.getPreauthenticatedRequest().getAccessUri();

            return "https://objectstorage." + region.trim() + ".oraclecloud.com" + accessUri;
        } catch (Exception ex) {
            log.error("[OCI-STORAGE] Error generando PAR para {}: {}", objectName, ex.getMessage(), ex);
            throw new BadRequestException("No se pudo generar la URL de descarga para el archivo solicitado.");
        }
    }
}