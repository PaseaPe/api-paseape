package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.infrastructure.dto.response.FileDownloadResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

public interface IStorageService {

    String uploadFile(String folder, MultipartFile file) throws BadRequestException;

    String uploadFile(String folder, byte[] content, String originalFilename, String contentType) throws BadRequestException;

    FileDownloadResDto downloadFile(String objectName) throws BadRequestException;

    void deleteFile(String objectName) throws BadRequestException;

    String generatePreSignedUrl(String objectName, int expirationMinutes) throws BadRequestException;
}