package com.paseape.apipaseape.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaDownloadResDto {

    private byte[] data;
    private String contentType;
    private String fileName;
}