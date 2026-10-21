package com.paseape.apipaseape.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class ApisPeruService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String dniUrl;
    private final String token;
    private final String appEnv;

    public ApisPeruService(
            ObjectMapper objectMapper,
            @Value("${app.apisperu.dni.url:https://dniruc.apisperu.com/api/v1/dni}") String dniUrl,
            @Value("${app.apisperu.token:}") String token,
            @Value("${app.env:local}") String appEnv
    ) {
        this.restClient = RestClient.builder().build();
        this.objectMapper = objectMapper;
        this.dniUrl = dniUrl;
        this.token = token;
        this.appEnv = appEnv;
    }

    public boolean existeDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            return false;
        }

        String dniLimpio = dni.trim();

        // En entornos no productivos (local, dev), se omite la llamada HTTP a RENIEC
        if (!"prod".equalsIgnoreCase(appEnv != null ? appEnv.trim() : "")) {
            log.info("[APISPERU] Entorno '{}': Se omite verificación remota en RENIEC para DNI {}", appEnv, dniLimpio);
            return true;
        }

        String url = dniUrl.endsWith("/") ? dniUrl + dniLimpio : dniUrl + "/" + dniLimpio;

        try {
            String responseBody = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        log.warn("[APISPERU] Código HTTP {} al consultar DNI {}", response.getStatusCode(), dniLimpio);
                    })
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                return false;
            }

            JsonNode rootNode = objectMapper.readTree(responseBody);
            if (rootNode.has("success") && !rootNode.get("success").asBoolean()) {
                return false;
            }

            // Si la respuesta incluye el DNI o el nombre del titular, el DNI existe y es válido
            return rootNode.has("dni") || rootNode.has("nombres");
        } catch (Exception ex) {
            log.error("[APISPERU] Error al invocar la API de ApisPerú para DNI {}: {}", dniLimpio, ex.getMessage());
            return false;
        }
    }
}
