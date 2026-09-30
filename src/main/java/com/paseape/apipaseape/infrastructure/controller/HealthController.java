package com.paseape.apipaseape.infrastructure.controller;

import com.paseape.apipaseape.application.service.HealthService;
import com.paseape.apipaseape.infrastructure.dto.response.HealthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final HealthService healthService;

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> checkHealth() {
        HealthResponse response = healthService.checkHealth();
        if ("UP".equalsIgnoreCase(response.getStatus())) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}