package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.infrastructure.dto.response.FileDownloadResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

public interface IStorageService {

    String uploadFile(String folder, MultipartFile file) throws BadRequestException;

    FileDownloadResDto downloadFile(String objectName) throws BadRequestException;
}