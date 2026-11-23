package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IPaseadorRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.domain.entity.SolicitudPaseo;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class SolicitudPaseoDetalleValidator {

    private final IUsuarioRepository usuarioRepository;
    private final IClienteRepository clienteRepository;
    private final IPaseadorRepository paseadorRepository;
    private final ISolicitudPaseoRepository solicitudPaseoRepository;

    private static final Pattern UUID_PATTERN = Pattern.compile(Constant.REGEX_UUID);

    public ResponseDto<?> validateObtenerDetalle(String userEmail, String uuid) {
        try {
            if (ObjectUtils.isEmpty(userEmail)) {
                throw new BadRequestException(MessageCodes.ResponseCodeE01, "No se encontró una sesión activa autenticada.");
            }

            Usuario usuario = usuarioRepository.findByCorreo(userEmail.trim().toLowerCase());
            if (usuario == null) {
                throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el usuario autenticado.");
            }

            this.validateUuid(uuid, "solicitud de paseo");

            SolicitudPaseo solicitud = solicitudPaseoRepository.findByUuidWithDetails(uuid.trim());
            if (solicitud == null) {
                throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró la solicitud de paseo especificada.");
            }

            boolean esAdmin = usuario.getTipoUsuario() != null
                    && Integer.valueOf(Constant.ID_ADMINISTRADOR).equals(usuario.getTipoUsuario().getId());

            Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId());
            boolean esClienteDueno = cliente != null
                    && solicitud.getCliente() != null
                    && cliente.getId().equals(solicitud.getCliente().getId());

            Paseador paseador = paseadorRepository.findByUsuarioId(usuario.getId());
            boolean esPaseadorAsignado = paseador != null
                    && solicitud.getPaseador() != null
                    && paseador.getId().equals(solicitud.getPaseador().getId());

            if (!esAdmin && !esClienteDueno && !esPaseadorAsignado) {
                throw new BadRequestException(MessageCodes.ResponseCodeBR03, "No tiene permisos para consultar el detalle de esta solicitud de paseo.");
            }

        } catch (BadRequestException brex) {
            return BuildErrorForValidation(
                    brex.statusCode != null ? brex.statusCode : StatusCodes.Code400,
                    brex.code != null ? brex.code : MessageCodes.ResponseCodeBR01,
                    brex.getMessage()
            );
        }

        return null;
    }

    private void validateUuid(String uuid, String nombreEntidad) throws BadRequestException {
        if (ObjectUtils.isEmpty(uuid)) {
            throw new BadRequestException("El identificador de " + nombreEntidad + " no debe estar vacío");
        }
        if (!UUID_PATTERN.matcher(uuid.trim()).matches()) {
            throw new BadRequestException("El identificador de " + nombreEntidad + " no tiene un formato UUID válido");
        }
    }

    private ResponseDto<?> BuildErrorForValidation(int statusCode, String code, String message) {
        var response = new ResponseDto<>();
        response.setStatusCode(statusCode);
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
}
