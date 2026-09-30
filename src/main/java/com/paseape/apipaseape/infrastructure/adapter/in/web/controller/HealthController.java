package com.paseape.apipaseape.infrastructure.adapter.in.web.controller;

import com.paseape.apipaseape.application.dto.response.HealthResponse;
import com.paseape.apipaseape.application.port.in.HealthCheckUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final HealthCheckUseCase healthCheckUseCase;

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> getHealth() {
        HealthResponse response = healthCheckUseCase.execute();
        if ("UP".equalsIgnoreCase(response.getStatus())) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}