package com.paseape.apipaseape.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthResponse {

    private String status;
    private String service;
    private String environment;
    private String timezone;
    private ZonedDateTime timestamp;
    private DatabaseHealthResponse database;
}
