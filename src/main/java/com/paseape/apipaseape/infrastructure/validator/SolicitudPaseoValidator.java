package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IDistritoLimaRepository;
import com.paseape.apipaseape.application.repository.IPaseadorRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.DistritoLima;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.domain.entity.SolicitudPaseo;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CancelarSolicitudReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudInmediataReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudProgramadaReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.RechazarSolicitudReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.RegistrarUbicacionesBatchReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.UbicacionPuntoReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Component
public class SolicitudPaseoValidator {

    private final IDistritoLimaRepository distritoLimaRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IPaseadorRepository paseadorRepository;
    private final IClienteRepository clienteRepository;
    private final ISolicitudPaseoRepository solicitudPaseoRepository;

    private static final Pattern UUID_PATTERN = Pattern.compile(Constant.REGEX_UUID);

    public SolicitudPaseoValidator(IDistritoLimaRepository distritoLimaRepository) {
        this(distritoLimaRepository, null, null, null, null);
    }

    public SolicitudPaseoValidator() {
        this(null, null, null, null, null);
    }

    public ResponseDto<?> validateCrearInmediata(CrearSolicitudInmediataReqDto reqDto) {
        try {
            // 1. Nulidad del request
            if (reqDto == null) {
                throw new BadRequestException("El cuerpo de la solicitud no puede estar vacío.");
            }

            // 2. Obligatoriedad y formato de listas
            List<String> mascotaUuids = reqDto.getMascotaUuids();
            if (ObjectUtils.isEmpty(mascotaUuids)) {
                throw new BadRequestException("La lista de mascotas es obligatoria y debe contener al menos un elemento.");
            }

            Set<String> uniqueUuids = new HashSet<>();
            for (String uuid : mascotaUuids) {
                this.validateUuidInternal(uuid, "mascota");
                if (!uniqueUuids.add(uuid.trim().toLowerCase())) {
                    throw new BadRequestException("La lista de mascotas contiene identificadores duplicados.");
                }
            }

            // 3. Montos y valores numéricos
            if (reqDto.getTarifaHoraPen() == null || reqDto.getTarifaHoraPen().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("El campo 'tarifa_hora_pen' es obligatorio y debe ser mayor a 0.");
            }

            if (reqDto.getTipoDuracionPaseoId() == null || reqDto.getTipoDuracionPaseoId() <= 0) {
                throw new BadRequestException("El campo 'tipo_duracion_paseo_id' es obligatorio y debe ser un valor positivo.");
            }

            if (reqDto.getTipoMetodoPagoId() == null || reqDto.getTipoMetodoPagoId() <= 0) {
                throw new BadRequestException("El campo 'tipo_metodo_pago_id' es obligatorio y debe ser un valor positivo.");
            }

            // 4. Existencia previa barata mediante puertos IXRepository
            if (reqDto.getDistritoId() == null || reqDto.getDistritoId() <= 0) {
                throw new BadRequestException("El campo 'distrito_id' es obligatorio y debe ser un valor positivo.");
            }

            if (distritoLimaRepository != null) {
                DistritoLima distrito = distritoLimaRepository.findById(reqDto.getDistritoId());
                if (distrito == null) {
                    throw new BadRequestException("El distrito seleccionado no existe.");
                }
            }

            // 5. Cadenas de texto y longitud
            if (ObjectUtils.isEmpty(reqDto.getDireccionRecojo()) || reqDto.getDireccionRecojo().trim().isEmpty()) {
                throw new BadRequestException("El campo 'direccion_recojo' es obligatorio y no puede estar vacío.");
            }

            if (reqDto.getDireccionRecojo().trim().length() > 255) {
                throw new BadRequestException("La dirección de recojo no debe superar los 255 caracteres.");
            }

            if (reqDto.getReferenciaDireccion() != null && reqDto.getReferenciaDireccion().trim().length() > 255) {
                throw new BadRequestException("El campo 'referencia_direccion' no debe superar los 255 caracteres.");
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

    public ResponseDto<?> validateCrearProgramada(CrearSolicitudProgramadaReqDto reqDto) {
        try {
            // 1. Nulidad del request
            if (reqDto == null) {
                throw new BadRequestException("El cuerpo de la solicitud no puede estar vacío.");
            }

            // 2. Paseador UUID
            if (ObjectUtils.isEmpty(reqDto.getPaseadorUuid())) {
                throw new BadRequestException("El campo 'paseador_uuid' es obligatorio.");
            }
            this.validateUuidInternal(reqDto.getPaseadorUuid(), "del paseador");

            // 3. Obligatoriedad y formato de listas
            List<String> mascotaUuids = reqDto.getMascotaUuids();
            if (ObjectUtils.isEmpty(mascotaUuids)) {
                throw new BadRequestException("La lista de mascotas es obligatoria y debe contener al menos un elemento.");
            }

            Set<String> uniqueUuids = new HashSet<>();
            for (String uuid : mascotaUuids) {
                this.validateUuidInternal(uuid, "mascota");
                if (!uniqueUuids.add(uuid.trim().toLowerCase())) {
                    throw new BadRequestException("La lista de mascotas contiene identificadores duplicados.");
                }
            }

            // 4. Distrito
            if (reqDto.getDistritoId() == null || reqDto.getDistritoId() <= 0) {
                throw new BadRequestException("El campo 'distrito_id' es obligatorio y debe ser un valor positivo.");
            }

            if (distritoLimaRepository != null) {
                DistritoLima distrito = distritoLimaRepository.findById(reqDto.getDistritoId());
                if (distrito == null) {
                    throw new BadRequestException("El distrito seleccionado no existe.");
                }
            }

            // 5. Fecha programada y bloque horario
            if (reqDto.getFechaProgramada() == null) {
                throw new BadRequestException("El campo 'fecha_programada' es obligatorio.");
            }

            if (reqDto.getBloqueHorarioId() == null || reqDto.getBloqueHorarioId() <= 0) {
                throw new BadRequestException("El campo 'bloque_horario_id' es obligatorio y debe ser un valor positivo.");
            }

            if (reqDto.getTipoDuracionPaseoId() == null || reqDto.getTipoDuracionPaseoId() <= 0) {
                throw new BadRequestException("El campo 'tipo_duracion_paseo_id' es obligatorio y debe ser un valor positivo.");
            }

            if (reqDto.getTipoMetodoPagoId() == null || reqDto.getTipoMetodoPagoId() <= 0) {
                throw new BadRequestException("El campo 'tipo_metodo_pago_id' es obligatorio y debe ser un valor positivo.");
            }

            // 6. Cadenas de texto y longitud
            if (ObjectUtils.isEmpty(reqDto.getDireccionRecojo()) || reqDto.getDireccionRecojo().trim().isEmpty()) {
                throw new BadRequestException("El campo 'direccion_recojo' es obligatorio y no puede estar vacío.");
            }

            if (reqDto.getDireccionRecojo().trim().length() > 255) {
                throw new BadRequestException("La dirección de recojo no debe superar los 255 caracteres.");
            }

            if (reqDto.getReferenciaDireccion() != null && reqDto.getReferenciaDireccion().trim().length() > 255) {
                throw new BadRequestException("El campo 'referencia_direccion' no debe superar los 255 caracteres.");
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

    public ResponseDto<?> validateConsultaBandejaCliente(String tab, Integer page, Integer size) {
        try {
            if (ObjectUtils.isEmpty(tab)) {
                throw new BadRequestException("El parámetro 'tab' es estrictamente obligatorio.");
            }
            String tabUpper = tab.trim().toUpperCase();
            if (!Constant.TAB_CLIENTE_ACTIVOS.equals(tabUpper) &&
                    !Constant.TAB_CLIENTE_PENDIENTES.equals(tabUpper) &&
                    !Constant.TAB_CLIENTE_HISTORIAL.equals(tabUpper)) {
                throw new BadRequestException(Constant.MSJ_ERROR_TAB_CLIENTE_INVALIDO);
            }
            if (page == null || page < 0) {
                throw new BadRequestException("El número de página no puede ser negativo.");
            }
            if (size == null || size < 1 || size > Constant.SIZE_MAXIMO) {
                throw new BadRequestException("El tamaño de página debe estar entre 1 y " + Constant.SIZE_MAXIMO + ".");
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

    public ResponseDto<?> validateCancelarSolicitudCliente(String uuid, CancelarSolicitudReqDto reqDto) {
        try {
            this.validateUuidInternal(uuid, "de la solicitud");
            if (reqDto != null && !ObjectUtils.isEmpty(reqDto.getMotivo()) && reqDto.getMotivo().trim().length() > 500) {
                throw new BadRequestException("El motivo de cancelación no debe superar los 500 caracteres.");
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

    public ResponseDto<?> validateConsultaBandejaPaseador(String tab, Integer page, Integer size) {
        try {
            if (ObjectUtils.isEmpty(tab)) {
                throw new BadRequestException("El parámetro 'tab' es estrictamente obligatorio.");
            }
            String tabUpper = tab.trim().toUpperCase();
            if (!Constant.TAB_INMEDIATAS.equals(tabUpper) &&
                    !Constant.TAB_MIS_PASEOS.equals(tabUpper) &&
                    !Constant.TAB_EN_CURSO.equals(tabUpper) &&
                    !Constant.TAB_HISTORIAL.equals(tabUpper)) {
                throw new BadRequestException(Constant.MSJ_ERROR_TAB_INVALIDO);
            }
            if (page == null || page < 0) {
                throw new BadRequestException("El número de página no puede ser negativo.");
            }
            if (size == null || size < 1 || size > Constant.SIZE_MAXIMO) {
                throw new BadRequestException("El tamaño de página debe estar entre 1 y " + Constant.SIZE_MAXIMO + ".");
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

    public ResponseDto<?> validateTomarSolicitudInmediata(String uuid) {
        try {
            this.validateUuidInternal(uuid, "de la solicitud");
        } catch (BadRequestException brex) {
            return BuildErrorForValidation(
                    brex.statusCode != null ? brex.statusCode : StatusCodes.Code400,
                    brex.code != null ? brex.code : MessageCodes.ResponseCodeBR01,
                    brex.getMessage()
            );
        }
        return null;
    }

    public ResponseDto<?> validateDescartarSolicitudInmediata(String uuid) {
        try {
            this.validateUuidInternal(uuid, "de la solicitud");
        } catch (BadRequestException brex) {
            return BuildErrorForValidation(
                    brex.statusCode != null ? brex.statusCode : StatusCodes.Code400,
                    brex.code != null ? brex.code : MessageCodes.ResponseCodeBR01,
                    brex.getMessage()
            );
        }
        return null;
    }

    public ResponseDto<?> validateAceptarSolicitudProgramada(String uuid) {
        try {
            this.validateUuidInternal(uuid, "de la solicitud");
        } catch (BadRequestException brex) {
            return BuildErrorForValidation(
                    brex.statusCode != null ? brex.statusCode : StatusCodes.Code400,
                    brex.code != null ? brex.code : MessageCodes.ResponseCodeBR01,
                    brex.getMessage()
            );
        }
        return null;
    }

    public ResponseDto<?> validateRechazarSolicitud(String uuid, RechazarSolicitudReqDto reqDto) {
        try {
            this.validateUuidInternal(uuid, "de la solicitud");
            if (reqDto != null && !ObjectUtils.isEmpty(reqDto.getMotivoRechazo()) && reqDto.getMotivoRechazo().trim().length() > 500) {
                throw new BadRequestException("El motivo de rechazo no debe superar los 500 caracteres.");
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

    public ResponseDto<?> validateCancelarPaseoAceptado(String uuid, CancelarSolicitudReqDto reqDto) {
        try {
            this.validateUuidInternal(uuid, "de la solicitud");
            if (reqDto == null) {
                throw new BadRequestException("El cuerpo de la solicitud no puede estar vacío.");
            }
            if (ObjectUtils.isEmpty(reqDto.getMotivo()) || reqDto.getMotivo().trim().length() < 10) {
                throw new BadRequestException(Constant.MSJ_ERROR_MOTIVO_CANCELACION_OBLIGATORIO);
            }
            if (reqDto.getMotivo().trim().length() > 500) {
                throw new BadRequestException("El motivo de cancelación no debe superar los 500 caracteres.");
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

    public ResponseDto<?> validateIniciarPaseo(String uuid) {
        try {
            this.validateUuidInternal(uuid, "de la solicitud");
        } catch (BadRequestException brex) {
            return BuildErrorForValidation(
                    brex.statusCode != null ? brex.statusCode : StatusCodes.Code400,
                    brex.code != null ? brex.code : MessageCodes.ResponseCodeBR01,
                    brex.getMessage()
            );
        }
        return null;
    }

    public ResponseDto<?> validateRegistrarUbicaciones(String userEmail, String uuid, RegistrarUbicacionesBatchReqDto reqDto) {
        try {
            // 1. Nulidad de parámetros y sesión
            if (ObjectUtils.isEmpty(userEmail) || userEmail.trim().isEmpty()) {
                throw new BadRequestException(MessageCodes.ResponseCodeE01, "Usuario no autenticado.");
            }

            this.validateUuidInternal(uuid, "de la solicitud");

            if (reqDto == null) {
                throw new BadRequestException("El cuerpo de la solicitud no puede estar vacío.");
            }

            // 2. Existencia previa y estado del paseador mediante puertos IXRepository
            if (usuarioRepository != null && paseadorRepository != null) {
                Usuario usuario = usuarioRepository.findByCorreo(userEmail.trim().toLowerCase());
                if (usuario == null) {
                    throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el usuario autenticado.");
                }

                Paseador paseador = paseadorRepository.findByUsuarioId(usuario.getId());
                if (paseador == null) {
                    throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El usuario no cuenta con un perfil de paseador registrado.");
                }

                if (paseador.getEstado() != null && paseador.getEstado() != Constant.ESTADO_LOGICO_ACTIVO) {
                    throw new BadRequestException(MessageCodes.ResponseCodeBR04, "El paseador no se encuentra activo.");
                }

                // 3. Existencia de la solicitud, titularidad y estado activo EN_CURSO
                if (solicitudPaseoRepository != null) {
                    SolicitudPaseo solicitud = solicitudPaseoRepository.findByUuidWithDetails(uuid.trim());
                    if (solicitud == null) {
                        solicitud = solicitudPaseoRepository.findByUuid(uuid.trim());
                    }
                    if (solicitud == null) {
                        throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró la solicitud de paseo.");
                    }

                    if (solicitud.getPaseador() == null || !paseador.getId().equals(solicitud.getPaseador().getId())) {
                        throw new BadRequestException(MessageCodes.ResponseCodeBR03, "La solicitud no pertenece al paseador autenticado.");
                    }

                    if (solicitud.getSolicitudPaseoEstado() == null
                            || !Integer.valueOf(Constant.ID_ESTADO_SOLICITUD_EN_CURSO).equals(solicitud.getSolicitudPaseoEstado().getId())) {
                        throw new BadRequestException(MessageCodes.ResponseCodeBR03, Constant.MSJ_ERROR_PASEO_NO_ACTIVO_GPS);
                    }
                }
            }

            // 4. Validación de lista y restricciones numéricas
            List<UbicacionPuntoReqDto> puntos = reqDto.getUbicaciones();
            if (ObjectUtils.isEmpty(puntos)) {
                throw new BadRequestException("La lista de ubicaciones es obligatoria y debe contener al menos un punto.");
            }

            if (puntos.size() > 50) {
                throw new BadRequestException("El lote de ubicaciones no puede superar un máximo de 50 puntos por envío.");
            }

            LocalDateTime maxFechaGpsPermitida = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(60);

            for (int i = 0; i < puntos.size(); i++) {
                UbicacionPuntoReqDto punto = puntos.get(i);
                if (punto == null) {
                    throw new BadRequestException("El punto de ubicación en la posición " + i + " no puede ser nulo.");
                }

                if (punto.getLatitud() == null) {
                    throw new BadRequestException("La latitud es obligatoria en la posición " + i + ".");
                }
                if (punto.getLatitud().compareTo(BigDecimal.valueOf(-90)) < 0 || punto.getLatitud().compareTo(BigDecimal.valueOf(90)) > 0) {
                    throw new BadRequestException("La latitud en la posición " + i + " debe encontrarse en el rango [-90.0, 90.0].");
                }

                if (punto.getLongitud() == null) {
                    throw new BadRequestException("La longitud es obligatoria en la posición " + i + ".");
                }
                if (punto.getLongitud().compareTo(BigDecimal.valueOf(-180)) < 0 || punto.getLongitud().compareTo(BigDecimal.valueOf(180)) > 0) {
                    throw new BadRequestException("La longitud en la posición " + i + " debe encontrarse en el rango [-180.0, 180.0].");
                }

                if (punto.getFechaHoraGps() == null) {
                    throw new BadRequestException("El campo 'fecha_hora_gps' es obligatorio en la posición " + i + ".");
                }

                if (punto.getPrecisionM() != null && punto.getPrecisionM().compareTo(BigDecimal.ZERO) < 0) {
                    throw new BadRequestException("La precisión en metros no puede ser negativa en la posición " + i + ".");
                }
                if (punto.getVelocidadMs() != null && punto.getVelocidadMs().compareTo(BigDecimal.ZERO) < 0) {
                    throw new BadRequestException("La velocidad en m/s no puede ser negativa en la posición " + i + ".");
                }
                if (punto.getRumboGrados() != null && (punto.getRumboGrados().compareTo(BigDecimal.ZERO) < 0 || punto.getRumboGrados().compareTo(BigDecimal.valueOf(360)) > 0)) {
                    throw new BadRequestException("El rumbo en grados debe encontrarse en el rango [0.0, 360.0] en la posición " + i + ".");
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

    public ResponseDto<?> validateRegistrarUbicaciones(String uuid, RegistrarUbicacionesBatchReqDto reqDto) {
        return validateRegistrarUbicaciones("dummy@paseape.com", uuid, reqDto);
    }

    public ResponseDto<?> validateObtenerUbicacionEnVivo(String userEmail, String uuid) {
        try {
            if (ObjectUtils.isEmpty(userEmail) || userEmail.trim().isEmpty()) {
                throw new BadRequestException(MessageCodes.ResponseCodeE01, "Usuario no autenticado.");
            }

            this.validateUuidInternal(uuid, "de la solicitud");

            if (usuarioRepository != null && clienteRepository != null) {
                Usuario usuario = usuarioRepository.findByCorreo(userEmail.trim().toLowerCase());
                if (usuario == null) {
                    throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el usuario autenticado.");
                }

                Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId());
                if (cliente == null) {
                    throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El usuario no cuenta con un perfil de cliente registrado.");
                }

                if (cliente.getEstado() != null && cliente.getEstado() != Constant.ESTADO_LOGICO_ACTIVO) {
                    throw new BadRequestException(MessageCodes.ResponseCodeBR04, "El cliente no se encuentra activo.");
                }

                if (solicitudPaseoRepository != null) {
                    SolicitudPaseo solicitud = solicitudPaseoRepository.findByUuidWithDetails(uuid.trim());
                    if (solicitud == null) {
                        solicitud = solicitudPaseoRepository.findByUuid(uuid.trim());
                    }
                    if (solicitud == null) {
                        throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró la solicitud de paseo.");
                    }

                    if (solicitud.getCliente() == null || !cliente.getId().equals(solicitud.getCliente().getId())) {
                        throw new BadRequestException(MessageCodes.ResponseCodeBR03, "La solicitud no pertenece al cliente autenticado.");
                    }

                    if (solicitud.getSolicitudPaseoEstado() != null) {
                        int estadoId = solicitud.getSolicitudPaseoEstado().getId();
                        if (estadoId < Constant.ID_ESTADO_SOLICITUD_EN_CURSO) {
                            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El paseo aún no ha comenzado.");
                        } else if (estadoId == Constant.ID_ESTADO_SOLICITUD_FINALIZADA) {
                            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El paseo ya ha finalizado. Consulta el recorrido histórico consolidado.");
                        } else if (estadoId != Constant.ID_ESTADO_SOLICITUD_EN_CURSO) {
                            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "La solicitud no se encuentra en curso.");
                        }
                    }
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

    public ResponseDto<?> validateObtenerRecorridoConsolidado(String userEmail, String uuid) {
        try {
            if (ObjectUtils.isEmpty(userEmail) || userEmail.trim().isEmpty()) {
                throw new BadRequestException(MessageCodes.ResponseCodeE01, "Usuario no autenticado.");
            }

            this.validateUuidInternal(uuid, "de la solicitud");

            if (usuarioRepository != null && solicitudPaseoRepository != null) {
                Usuario usuario = usuarioRepository.findByCorreo(userEmail.trim().toLowerCase());
                if (usuario == null) {
                    throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el usuario autenticado.");
                }

                SolicitudPaseo solicitud = solicitudPaseoRepository.findByUuidWithDetails(uuid.trim());
                if (solicitud == null) {
                    solicitud = solicitudPaseoRepository.findByUuid(uuid.trim());
                }
                if (solicitud == null) {
                    throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró la solicitud de paseo.");
                }

                boolean esCliente = solicitud.getCliente() != null && solicitud.getCliente().getUsuario() != null
                        && usuario.getId().equals(solicitud.getCliente().getUsuario().getId());
                boolean esPaseador = solicitud.getPaseador() != null && solicitud.getPaseador().getUsuario() != null
                        && usuario.getId().equals(solicitud.getPaseador().getUsuario().getId());
                boolean esAdmin = usuario.getTipoUsuario() != null
                        && Integer.valueOf(Constant.ID_ADMINISTRADOR).equals(usuario.getTipoUsuario().getId());

                if (!esCliente && !esPaseador && !esAdmin) {
                    throw new BadRequestException(MessageCodes.ResponseCodeBR03, "No tiene permisos para consultar el recorrido de esta solicitud.");
                }

                if (solicitud.getSolicitudPaseoEstado() == null
                        || !Integer.valueOf(Constant.ID_ESTADO_SOLICITUD_FINALIZADA).equals(solicitud.getSolicitudPaseoEstado().getId())) {
                    throw new BadRequestException(MessageCodes.ResponseCodeBR03, "El recorrido solo puede consultarse cuando el paseo ha finalizado.");
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

    public ResponseDto<?> validateUuid(String uuid, String nombreCampo) {
        try {
            this.validateUuidInternal(uuid, nombreCampo);
        } catch (BadRequestException brex) {
            return BuildErrorForValidation(
                    brex.statusCode != null ? brex.statusCode : StatusCodes.Code400,
                    brex.code != null ? brex.code : MessageCodes.ResponseCodeBR01,
                    brex.getMessage()
            );
        }
        return null;
    }

    private void validateUuidInternal(String uuid, String nombreEntidad) throws BadRequestException {
        if (ObjectUtils.isEmpty(uuid)) {
            throw new BadRequestException("El identificador " + nombreEntidad + " no debe estar vacío.");
        }
        if (!UUID_PATTERN.matcher(uuid.trim()).matches()) {
            throw new BadRequestException("El identificador de " + nombreEntidad + " no tiene un formato UUID válido.");
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
