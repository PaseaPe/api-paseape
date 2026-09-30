package com.paseape.apipaseape.application.port.in;


import com.paseape.apipaseape.application.dto.response.HealthResponse;

public interface HealthCheckUseCase {
    HealthResponse execute();
}
