package com.paseape.apipaseape.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.InputStream;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDownloadResDto {
    private String fileName;
    private String contentType;
    private Long contentLength;
    private InputStream content;
}