package com.paseape.apipaseape.infrastructure.validator;

import java.math.BigDecimal;
import java.security.Principal;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarMascotaReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.MascotaReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.RegistrarMascotaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MascotaValidator {

    private final IUsuarioRepository usuarioRepository;
    private final IClienteRepository clienteRepository;
    private final StorageValidator storageValidator;

    public ErrorDetailDto validateSubirFotoMascota(Principal principal, String mascotaUuid, MultipartFile file) {
        if (principal == null || !StringUtils.hasText(principal.getName())) {
            return buildError(StatusCodes.Code401, MessageCodes.ResponseCodeE01, "No se encontro una sesion activa autenticada");
        }

        if (!StringUtils.hasText(mascotaUuid)) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El identificador de la mascota es obligatorio.");
        }

        Usuario usuario = usuarioRepository.findByCorreo(principal.getName());
        if (usuario == null) {
            return buildError(StatusCodes.Code404, MessageCodes.ResponseCodeBR98, "Usuario no encontrado");
        }

        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId());
        if (cliente == null) {
            return buildError(StatusCodes.Code403, MessageCodes.ResponseCodeBR04, "El usuario no posee un perfil de cliente");
        }

        ErrorDetailDto fileError = storageValidator.validateUpload(file);
        if (fileError != null) {
            return fileError;
        }

        return null;
    }

    public ErrorDetailDto validateRegistroUnitario(RegistrarMascotaReqDto request) {
        if (request == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El cuerpo de la solicitud no puede estar vacío.");
        }
        if (!StringUtils.hasText(request.getUsuarioUuid())) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo 'usuario_uuid' es estrictamente obligatorio.");
        }
        if (request.getMascota() == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El objeto 'mascota' es obligatorio.");
        }
        return validateMascota(request.getMascota());
    }

    public ErrorDetailDto validateMascota(MascotaReqDto mascota) {
        if (mascota == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "Los datos de la mascota no pueden ser nulos.");
        }
        if (!StringUtils.hasText(mascota.getNombre())) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El nombre de la mascota es obligatorio.");
        }
        if (mascota.getTipoMascotaId() == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El tipo de mascota es obligatorio.");
        }
        if (mascota.getTipoRazaId() == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "La raza de la mascota es obligatoria.");
        }
        if (mascota.getTipoGeneroMascotaId() == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El género de la mascota es obligatorio.");
        }
        if (mascota.getTipoTamanoMascotaId() == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El tamaño de la mascota es obligatorio.");
        }
        if (mascota.getTipoNivelEnergiaId() == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El nivel de energía de la mascota es obligatorio.");
        }
        if (mascota.getPesoKg() != null && mascota.getPesoKg().compareTo(BigDecimal.ZERO) <= 0) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El peso de la mascota debe ser mayor a 0 kg.");
        }
        return null;
    }

    public ErrorDetailDto validateActualizarMascota(ActualizarMascotaReqDto mascota) {
        if (mascota == null) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El cuerpo de la solicitud no puede estar vacío.");
        }

        if (StringUtils.hasText(mascota.getNombre()) && mascota.getNombre().trim().length() > 60) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El nombre de la mascota no debe superar 60 caracteres.");
        }

        if (mascota.getPesoKg() != null && mascota.getPesoKg().compareTo(BigDecimal.ZERO) <= 0) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El peso de la mascota debe ser mayor a 0 kg.");
        }

        if (mascota.getEdadAnos() != null && mascota.getEdadAnos() < 0) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "La edad en años no puede ser negativa.");
        }

        if (mascota.getEdadMeses() != null && (mascota.getEdadMeses() < 0 || mascota.getEdadMeses() > 11)) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "La edad en meses debe estar entre 0 y 11.");
        }

        if (mascota.getEsterilizado() != null && (mascota.getEsterilizado() != 0 && mascota.getEsterilizado() != 1)) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo esterilizado debe ser 0 o 1.");
        }

        if (mascota.getSociableConPerros() != null && (mascota.getSociableConPerros() != 0 && mascota.getSociableConPerros() != 1)) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo sociable_con_perros debe ser 0 o 1.");
        }

        if (mascota.getSociableConPersonas() != null && (mascota.getSociableConPersonas() != 0 && mascota.getSociableConPersonas() != 1)) {
            return buildError(StatusCodes.Code400, MessageCodes.ResponseCodeBR01, "El campo sociable_con_personas debe ser 0 o 1.");
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