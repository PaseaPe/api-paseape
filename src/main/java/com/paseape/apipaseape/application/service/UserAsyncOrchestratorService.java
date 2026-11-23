package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.domain.event.GoogleUserRegisteredEvent;
import com.paseape.apipaseape.infrastructure.dto.response.MediaDownloadResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAsyncOrchestratorService {

    private final GoogleMediaDownloadService googleMediaDownloadService;
    private final DigitalDocumentService digitalDocumentService;

    @Async("paseapeAsyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void procesarFotoGoogleAsync(GoogleUserRegisteredEvent event) {
        if (event == null || !StringUtils.hasText(event.getUsuarioUuid()) || !StringUtils.hasText(event.getGooglePictureUrl())) {
            return;
        }

        try {
            MediaDownloadResDto mediaDto = googleMediaDownloadService.descargarFoto(event.getGooglePictureUrl());
            digitalDocumentService.subirFotoUsuarioBytes(
                    event.getUsuarioUuid(),
                    mediaDto.getData(),
                    mediaDto.getFileName(),
                    mediaDto.getContentType()
            );
        } catch (Exception ex) {
            log.warn("[ASYNC-ORCHESTRATOR] No se pudo procesar la foto de Google para el usuario {}: {}", event.getUsuarioUuid(), ex.getMessage());
        }
    }
}