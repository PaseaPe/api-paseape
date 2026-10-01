package com.paseape.apipaseape.infrastructure.repository.http;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GoogleTokenVerifierHttpRepository {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifierHttpRepository(@Value("${app.security.google-client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    public GoogleIdToken.Payload verify(String idTokenString) throws BadRequestException {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new BadRequestException("El idToken proporcionado es invalido o expiro ante los servidores de Google.");
            }
            return idToken.getPayload();
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("Error en la validacion criptografica del token de Google: " + ex.getMessage(), ex);
        }
    }
}
