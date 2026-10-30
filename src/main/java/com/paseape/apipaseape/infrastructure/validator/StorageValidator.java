package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.application.repository.IPaseadorRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StorageValidator {

    private final IUsuarioRepository usuarioRepository;
    private final IPaseadorRepository paseadorRepository;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf"
    );

    public ErrorDetailDto validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "Debe seleccionar un archivo válido.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El tamaño del archivo excede el límite máximo de 10 MB.");
        }
        if (file.getContentType() == null || !ALLOWED_CONTENT_TYPES.contains(file.getContentType().toLowerCase())) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "Formato no permitido. Solo se aceptan JPEG, PNG, WEBP o PDF.");
        }
        return null;
    }

    public ErrorDetailDto validarSubidaArchivos(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01,"Debe enviar al menos un archivo.");
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                return validateUpload(file);
            }
        }
        return null;
    }

    public ErrorDetailDto validarSubirAntecedentes(List<MultipartFile> files, String usuarioUuid){
        if (usuarioUuid == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01,"El uuid del usuario no debe estar vacio.");
        }
        Usuario usuario = usuarioRepository.findByUuid(usuarioUuid);
        if (usuario == null){
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01,"El usuario no existe.");
        }

        Paseador paseador = paseadorRepository.findByUsuarioId(usuario.getId());
        if (paseador == null){
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01,"El paseador no existe.");
        }

        return validarSubidaArchivos(files);
    }

    public ErrorDetailDto validateDownload(String objectName) {
        if (!StringUtils.hasText(objectName)) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El nombre del archivo a descargar es obligatorio.");
        }
        return null;
    }

    private ErrorDetailDto buildError(int statusCode, String code, String message) {
        return ErrorDetailDto.builder()
                .statusCode(statusCode)
                .code(code)
                .message(message)
                .build();
    }
}