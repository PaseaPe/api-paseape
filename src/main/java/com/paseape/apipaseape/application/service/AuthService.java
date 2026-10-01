package com.paseape.apipaseape.application.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.paseape.apipaseape.infrastructure.dto.request.GoogleAuthReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.AuthResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.repository.http.GoogleTokenVerifierHttpRepository;
import com.paseape.apipaseape.infrastructure.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleTokenVerifierHttpRepository googleTokenVerifier;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResDto authenticateWithGoogle(GoogleAuthReqDto reqDto) throws BadRequestException {
        if (reqDto == null || reqDto.getIdToken() == null || reqDto.getIdToken().trim().isEmpty()) {
            throw new BadRequestException("El campo 'idToken' es estrictamente obligatorio.");
        }

        // 1. Verificación criptográfica con Google Identity Services
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(reqDto.getIdToken());

        String email = payload.getEmail();
        String fullName = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

        // 2. Normalización de rol solicitado (por defecto OWNER)
        String role = (reqDto.getSelectedRole() != null && reqDto.getSelectedRole().equalsIgnoreCase("WALKER"))
                ? "WALKER"
                : "OWNER";

        // 3. ID temporal (Stub/Mock hasta crear la entidad User y persistir en Aiven MySQL)
        Long provisionalUserId = 1001L;

        // 4. Generación de Token JWT de sesión interna
        String token = jwtTokenProvider.generateToken(provisionalUserId, email, role);

        return AuthResDto.builder()
                .id(provisionalUserId)
                .email(email)
                .fullName(fullName != null ? fullName : "Usuario PaseaPe")
                .pictureUrl(pictureUrl)
                .role(role)
                .token(token)
                .build();
    }
}
