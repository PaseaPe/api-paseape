package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class ClienteValidator {
    private final IUsuarioRepository usuarioRepository;
    private final StorageValidator storageValidator;

    public ErrorDetailDto validateSubirFotoUsuario(Principal principal, MultipartFile file) {
        if (principal == null || !StringUtils.hasText(principal.getName())) {
            return buildError(StatusCodes.Code401, MessageCodes.ResponseCodeE01, "No se encontro una sesion activa autenticada");
        }

        Usuario usuario = usuarioRepository.findByCorreo(principal.getName());
        if (usuario == null) {
            return buildError(StatusCodes.Code404, MessageCodes.ResponseCodeBR98, "Usuario no encontrado");
        }

        ErrorDetailDto fileError = storageValidator.validateUpload(file);
        if (fileError != null) {
            return fileError;
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
