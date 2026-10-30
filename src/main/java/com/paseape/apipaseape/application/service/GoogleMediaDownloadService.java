package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.infrastructure.dto.response.MediaDownloadResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

@Slf4j
@Service
public class GoogleMediaDownloadService {

    private static final int CONNECT_TIMEOUT_MS = 3000;
    private static final int READ_TIMEOUT_MS = 5000;
    private static final String DEFAULT_FILE_NAME = "google_avatar.jpg";
    private static final String DEFAULT_CONTENT_TYPE = "image/jpeg";

    public MediaDownloadResDto descargarFoto(String url) throws BadRequestException {
        if (!StringUtils.hasText(url)) {
            throw new BadRequestException("La URL de la imagen a descargar es obligatoria.");
        }

        try {
            URL targetUrl = URI.create(url.trim()).toURL();
            HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new BadRequestException("Respuesta fallida al descargar foto de Google: HTTP " + responseCode);
            }

            String contentType = connection.getContentType();
            if (!StringUtils.hasText(contentType)) {
                contentType = DEFAULT_CONTENT_TYPE;
            }

            byte[] data;
            try (InputStream in = connection.getInputStream()) {
                data = in.readAllBytes();
            }

            return MediaDownloadResDto.builder()
                    .data(data)
                    .contentType(contentType)
                    .fileName(DEFAULT_FILE_NAME)
                    .build();
        } catch (Exception ex) {
            log.error("[GOOGLE-MEDIA] Error descargando imagen desde {}: {}", url, ex.getMessage(), ex);
            throw new BadRequestException("No se pudo descargar la imagen de Google.");
        }
    }
}