package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.application.repository.ITipoMascotaRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.TipoMascota;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.catalogo.CatalogoBaseReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.catalogo.DistritoLimaReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.catalogo.TipoRazaReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.catalogo.TipoTamanoMascotaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.regex.Pattern;

@RequiredArgsConstructor
@Component
public class CatalogoValidator {

    private final IUsuarioRepository usuarioRepository;
    private final ITipoMascotaRepository tipoMascotaRepository;

    private static final Pattern UUID_PATTERN = Pattern.compile(Constant.REGEX_UUID);

    public ResponseDto<?> validateConsultaCatalogo(String userEmail, Integer estado) {
        try {
            this.validateAdministrador(userEmail);
            if (estado != null && estado != Constant.ESTADO_LOGICO_INACTIVO && estado != Constant.ESTADO_LOGICO_ACTIVO) {
                throw new BadRequestException("El parámetro 'estado' solo permite los valores 0 (inactivo) o 1 (activo).");
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

    public ResponseDto<?> validateAccionPorUuid(String userEmail, String uuid) {
        try {
            this.validateAdministrador(userEmail);
            this.validateUuidInternal(uuid, "del registro de catálogo");
        } catch (BadRequestException brex) {
            return BuildErrorForValidation(
                    brex.statusCode != null ? brex.statusCode : StatusCodes.Code400,
                    brex.code != null ? brex.code : MessageCodes.ResponseCodeBR01,
                    brex.getMessage()
            );
        }
        return null;
    }

    public ResponseDto<?> validateDistrito(String userEmail, String uuid, DistritoLimaReqDto reqDto) {
        try {
            this.validateAdministrador(userEmail);
            if (uuid != null) {
                this.validateUuidInternal(uuid, "del distrito");
            }
            if (reqDto == null) {
                throw new BadRequestException("El cuerpo de la solicitud no puede estar vacío.");
            }
            this.validateDescripcion(reqDto.getDescripcion(), 100);

            if (!ObjectUtils.isEmpty(reqDto.getCodigoUbigeo()) && reqDto.getCodigoUbigeo().trim().length() > 10) {
                throw new BadRequestException("El campo 'codigo_ubigeo' no debe superar los 10 caracteres.");
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

    public ResponseDto<?> validateCatalogoBase(String userEmail, String uuid, CatalogoBaseReqDto reqDto, int maxLongitud) {
        try {
            this.validateAdministrador(userEmail);
            if (uuid != null) {
                this.validateUuidInternal(uuid, "del registro de catálogo");
            }
            if (reqDto == null) {
                throw new BadRequestException("El cuerpo de la solicitud no puede estar vacío.");
            }
            this.validateDescripcion(reqDto.getDescripcion(), maxLongitud);
        } catch (BadRequestException brex) {
            return BuildErrorForValidation(
                    brex.statusCode != null ? brex.statusCode : StatusCodes.Code400,
                    brex.code != null ? brex.code : MessageCodes.ResponseCodeBR01,
                    brex.getMessage()
            );
        }
        return null;
    }

    public ResponseDto<?> validateTipoRaza(String userEmail, String uuid, TipoRazaReqDto reqDto) {
        try {
            this.validateAdministrador(userEmail);
            if (uuid != null) {
                this.validateUuidInternal(uuid, "de la raza");
            }
            if (reqDto == null) {
                throw new BadRequestException("El cuerpo de la solicitud no puede estar vacío.");
            }
            this.validateUuidInternal(reqDto.getTipoMascotaUuid(), "del tipo de mascota");
            this.validateDescripcion(reqDto.getDescripcion(), 80);

            if (tipoMascotaRepository != null) {
                TipoMascota tipoMascota = tipoMascotaRepository.findByUuid(reqDto.getTipoMascotaUuid().trim());
                if (tipoMascota == null) {
                    throw new BadRequestException(MessageCodes.ResponseCodeBR98, "El tipo de mascota especificado no existe.");
                }
                if (tipoMascota.getEstado() == null || tipoMascota.getEstado() != Constant.ESTADO_LOGICO_ACTIVO) {
                    throw new BadRequestException(MessageCodes.ResponseCodeBR04, "El tipo de mascota especificado se encuentra inactivo.");
                }
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

    public ResponseDto<?> validateTipoTamanoMascota(String userEmail, String uuid, TipoTamanoMascotaReqDto reqDto) {
        try {
            this.validateAdministrador(userEmail);
            if (uuid != null) {
                this.validateUuidInternal(uuid, "del tamaño de mascota");
            }
            if (reqDto == null) {
                throw new BadRequestException("El cuerpo de la solicitud no puede estar vacío.");
            }
            this.validateDescripcion(reqDto.getDescripcion(), 50);

            if (!ObjectUtils.isEmpty(reqDto.getRangoPesoRef()) && reqDto.getRangoPesoRef().trim().length() > 50) {
                throw new BadRequestException("El campo 'rango_peso_ref' no debe superar los 50 caracteres.");
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

    private void validateAdministrador(String userEmail) throws BadRequestException {
        if (ObjectUtils.isEmpty(userEmail) || userEmail.trim().isEmpty()) {
            throw new BadRequestException(MessageCodes.ResponseCodeE01, "Usuario no autenticado.");
        }
        Usuario usuario = usuarioRepository.findByCorreo(userEmail.trim().toLowerCase());
        if (usuario == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el usuario autenticado.");
        }
        if (usuario.getUsuarioEstado() == null || usuario.getUsuarioEstado().getId() != Constant.ID_ACTIVO) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR04, "La cuenta del usuario no se encuentra activa.");
        }
        if (usuario.getTipoUsuario() == null || usuario.getTipoUsuario().getId() != Constant.ID_ADMINISTRADOR) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "No tiene permisos de administrador para gestionar catálogos.");
        }
    }

    private void validateDescripcion(String descripcion, int maxLongitud) throws BadRequestException {
        if (ObjectUtils.isEmpty(descripcion) || descripcion.trim().isEmpty()) {
            throw new BadRequestException("El campo 'descripcion' es obligatorio y no puede estar vacío.");
        }
        if (descripcion.trim().length() > maxLongitud) {
            throw new BadRequestException("El campo 'descripcion' no debe superar los " + maxLongitud + " caracteres.");
        }
    }

    private void validateUuidInternal(String uuid, String nombreEntidad) throws BadRequestException {
        if (ObjectUtils.isEmpty(uuid)) {
            throw new BadRequestException("El identificador " + nombreEntidad + " no debe estar vacío.");
        }
        if (!UUID_PATTERN.matcher(uuid.trim()).matches()) {
            throw new BadRequestException("El identificador " + nombreEntidad + " no tiene un formato UUID válido.");
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
