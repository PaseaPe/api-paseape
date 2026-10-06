package com.paseape.apipaseape.infrastructure.repository.http;


import com.paseape.apipaseape.application.repository.IBrevoEmailRepository;
import com.paseape.apipaseape.domain.entity.BrevoEmail;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.dto.request.brevo.BrevoRecipientDto;
import com.paseape.apipaseape.infrastructure.dto.request.brevo.BrevoSendEmailReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.brevo.BrevoSenderDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static com.paseape.apipaseape.infrastructure.constant.Constant.HEADER_API_KEY;

@Slf4j
@Component
public class BrevoEmailHttpRepository {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final IBrevoEmailRepository brevoEmailRepository;
    private final String apiKey;
    private final String senderEmail;
    private final String senderName;
    private final String brevoUrl;

    public BrevoEmailHttpRepository(
            ObjectMapper objectMapper,
            IBrevoEmailRepository brevoEmailRepository,
            @Value("${app.brevo.api-key}") String apiKey,
            @Value("${app.brevo.sender-email}") String senderEmail,
            @Value("${app.brevo.sender-name}") String senderName,
            @Value("${app.brevo.url:https://api.brevo.com/v3/smtp/email}") String brevoUrl) {
        this.restClient = RestClient.builder().build();
        this.objectMapper = objectMapper;
        this.brevoEmailRepository = brevoEmailRepository;
        this.apiKey = apiKey;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.brevoUrl = brevoUrl;
    }

    public void sendEmail(Usuario usuario, String recipientEmail, String recipientName, String subject, String htmlContent, String tipoNotificacion) throws BadRequestException {
        BrevoSendEmailReqDto payload = BrevoSendEmailReqDto.builder()
                .sender(BrevoSenderDto.builder().email(senderEmail).name(senderName).build())
                .to(List.of(BrevoRecipientDto.builder().email(recipientEmail).name(recipientName).build()))
                .subject(subject)
                .htmlContent(htmlContent)
                .build();

        String payloadJson = null;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.warn("[BREVO] No se pudo serializar el payload de auditoría: {}", e.getMessage());
        }

        BrevoEmail audit = BrevoEmail.builder()
                .uuid(UUID.randomUUID().toString())
                .usuario(usuario)
                .destinatarioEmail(recipientEmail)
                .destinatarioNombre(recipientName)
                .remitenteEmail(senderEmail)
                .asunto(subject)
                .tipoNotificacion(tipoNotificacion)
                .payloadEnviado(payloadJson)
                .estado(1)
                .build();

        try {
            ResponseEntity<String> response = restClient.post()
                    .uri(brevoUrl)
                    .header(HEADER_API_KEY, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toEntity(String.class);

            audit.setCodigoHttp(response.getStatusCode().value());
            audit.setRespuestaServicio(response.getBody());
            audit.setExitoso(1);
            brevoEmailRepository.save(audit);

            log.info("[BREVO] Correo '{}' enviado exitosamente a {}", tipoNotificacion, recipientEmail);
        } catch (Exception ex) {
            audit.setCodigoHttp(500);
            audit.setExitoso(0);
            audit.setErrorMensaje(ex.getMessage());
            brevoEmailRepository.save(audit);

            log.error("[BREVO] Error al invocar API de Brevo: {}", ex.getMessage(), ex);
            throw new BadRequestException("No fue posible enviar el correo transaccional en este momento.");
        }
    }
}