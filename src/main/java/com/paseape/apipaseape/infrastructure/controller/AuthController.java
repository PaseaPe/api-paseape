package com.paseape.apipaseape.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paseape.apipaseape.application.service.AuthService;
import com.paseape.apipaseape.infrastructure.dto.request.GoogleAuthReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.AuthResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.shared.ApiResponse;

import java.security.Principal;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResDto>> loginWithGoogle(@RequestBody GoogleAuthReqDto request) throws BadRequestException {
        AuthResDto response = authService.authenticateWithGoogle(request);
        return ResponseEntity.ok(ApiResponse.<AuthResDto>builder()
                .code("AUTH-00")
                .message("Autenticacion con Google exitosa.")
                .data(response)
                .build());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> validateSession(Principal principal) {
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code("AUTH-OK")
                .message("Sesion valida")
                .data("Usuario autenticado con ID interno: " + principal.getName())
                .build());
    }
}
