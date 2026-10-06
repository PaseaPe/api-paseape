package com.paseape.apipaseape.application.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.paseape.apipaseape.domain.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IPaseadorRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarPerfilReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.ForgotPasswordReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.GoogleAuthReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.LoginReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.ResetPasswordReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.UsuarioReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.AuthResDto;
import com.paseape.apipaseape.infrastructure.dto.response.ForgotPasswordResDto;
import com.paseape.apipaseape.infrastructure.dto.response.LogoutResDto;
import com.paseape.apipaseape.infrastructure.dto.response.PerfilResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.IPerfilDtoMapper;
import com.paseape.apipaseape.infrastructure.repository.http.BrevoEmailHttpRepository;
import com.paseape.apipaseape.infrastructure.repository.http.GoogleTokenVerifierHttpRepository;
import com.paseape.apipaseape.infrastructure.security.JwtTokenProvider;

import java.math.BigDecimal;
import java.util.UUID;

import static com.paseape.apipaseape.infrastructure.constant.Constant.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleTokenVerifierHttpRepository googleTokenVerifier;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final MascotaService mascotaService;
    private final BrevoEmailHttpRepository brevoEmailHttpRepository;

    private final IUsuarioRepository usuarioRepository;
    private final IClienteRepository clienteRepository;
    private final IPaseadorRepository paseadorRepository;
    private final IPerfilDtoMapper perfilDtoMapper;

    @Transactional(readOnly = true)
    public AuthResDto authenticateWithGoogle(GoogleAuthReqDto reqDto) throws BadRequestException {
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(reqDto.getIdToken());
        String email = payload.getEmail().trim().toLowerCase();
        String sub = payload.getSubject();

        Usuario usuario = usuarioRepository.findByCorreo(email);
        if (usuario == null) {
            usuario = usuarioRepository.findByProviderId(sub);
        }
        if (usuario == null) {
            throw new BadRequestException("El usuario con correo " + email + " no se encuentra registrado.");
        }

        validarEstadoUsuario(usuario);

        String role = resolveRoleName(usuario.getTipoUsuario() != null ? usuario.getTipoUsuario().getId() : null);
        String token = jwtTokenProvider.generateToken(usuario.getId(), usuario.getCorreo(), role);

        return AuthResDto.builder()
                .id(usuario.getId())
                .email(usuario.getCorreo())
                .fullName(construirNombreCompleto(usuario.getNombres(), usuario.getApellidos()))
                .pictureUrl(usuario.getFotoPerfilUrl())
                .role(role)
                .token(token)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResDto loginLocal(LoginReqDto reqDto) throws BadRequestException {
        String normalizedEmail = reqDto.getCorreo().trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByCorreo(normalizedEmail);

        if (usuario == null) {
            throw new BadRequestException("Credenciales de acceso inválidas.");
        }

        if (!StringUtils.hasText(usuario.getContrasenaHash()) ||
                !passwordEncoder.matches(reqDto.getContrasena(), usuario.getContrasenaHash())) {
            throw new BadRequestException("Credenciales de acceso inválidas.");
        }

        validarEstadoUsuario(usuario);

        String role = resolveRoleName(usuario.getTipoUsuario() != null ? usuario.getTipoUsuario().getId() : null);
        String token = jwtTokenProvider.generateToken(usuario.getId(), usuario.getCorreo(), role);

        return AuthResDto.builder()
                .id(usuario.getId())
                .email(usuario.getCorreo())
                .fullName(construirNombreCompleto(usuario.getNombres(), usuario.getApellidos()))
                .pictureUrl(usuario.getFotoPerfilUrl())
                .role(role)
                .token(token)
                .build();
    }

    @Transactional(readOnly = true)
    public LogoutResDto logout(String userIdentifier) {
        SecurityContextHolder.clearContext();

        boolean esGoogle = false;
        Usuario usuario = usuarioRepository.findByCorreo(userIdentifier);
        if (usuario != null && usuario.getTipoProveedorAuth() != null) {
            esGoogle = usuario.getTipoProveedorAuth().getId() == ID_GOOGLE;
        }

        return LogoutResDto.builder()
                .correo(userIdentifier)
                .mensaje("Sesión cerrada correctamente.")
                .requiereRevocacionGoogle(esGoogle)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public AuthResDto registerWithGoogle(UsuarioReqDto reqDto) throws BadRequestException {
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(reqDto.getIdToken());
        String email = payload.getEmail().trim().toLowerCase();
        String googleSub = payload.getSubject();

        if (usuarioRepository.existsByCorreo(email)) {
            throw new BadRequestException("Ya existe una cuenta registrada con el correo: " + email);
        }
        if (usuarioRepository.findByProviderId(googleSub) != null) {
            throw new BadRequestException("La cuenta de Google ya está vinculada a otro perfil de usuario.");
        }

        String nombres = StringUtils.hasText(reqDto.getNombres()) ? reqDto.getNombres().trim() : (String) payload.get("given_name");
        String apellidos = StringUtils.hasText(reqDto.getApellidos()) ? reqDto.getApellidos().trim() : (String) payload.get("family_name");
        if (!StringUtils.hasText(nombres)) nombres = (String) payload.get("name");
        if (!StringUtils.hasText(apellidos)) apellidos = "";

        String fotoUrl = StringUtils.hasText(reqDto.getFotoPerfilUrl()) ? reqDto.getFotoPerfilUrl() : (String) payload.get("picture");

        Usuario nuevoUsuario = Usuario.builder()
                .uuid(UUID.randomUUID().toString())
                .nombres(nombres)
                .apellidos(apellidos)
                .correo(email)
                .correoVerificado(1)
                .contrasenaHash(null)
                .telefono(reqDto.getTelefono())
                .fotoPerfilUrl(fotoUrl)
                .tipoUsuario(new TipoUsuario(reqDto.getTipoUsuarioId()))
                .usuarioEstado(new UsuarioEstado(ID_ACTIVO))
                .tipoProveedorAuth(new TipoProveedorAuth(ID_GOOGLE))
                .providerId(googleSub)
                .estado(ESTADO_LOGICO_ACTIVO)
                .build();

        Usuario savedUser = usuarioRepository.save(nuevoUsuario);
        procesarSubtipo(savedUser, reqDto);

        String role = resolveRoleName(savedUser.getTipoUsuario().getId());
        String jwtToken = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getCorreo(), role);

        return AuthResDto.builder()
                .id(savedUser.getId())
                .email(savedUser.getCorreo())
                .fullName(construirNombreCompleto(savedUser.getNombres(), savedUser.getApellidos()))
                .pictureUrl(savedUser.getFotoPerfilUrl())
                .role(role)
                .token(jwtToken)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public AuthResDto registerLocal(UsuarioReqDto reqDto) throws BadRequestException {
        String normalizedEmail = reqDto.getCorreo().trim().toLowerCase();

        if (usuarioRepository.existsByCorreo(normalizedEmail)) {
            throw new BadRequestException("El correo ingresado ya se encuentra registrado: " + normalizedEmail);
        }

        Usuario nuevoUsuario = Usuario.builder()
                .uuid(UUID.randomUUID().toString())
                .nombres(reqDto.getNombres().trim())
                .apellidos(reqDto.getApellidos().trim())
                .correo(normalizedEmail)
                .correoVerificado(0)
                .contrasenaHash(passwordEncoder.encode(reqDto.getContrasena()))
                .telefono(reqDto.getTelefono())
                .fotoPerfilUrl(reqDto.getFotoPerfilUrl())
                .tipoUsuario(new TipoUsuario(reqDto.getTipoUsuarioId()))
                .usuarioEstado(new UsuarioEstado(ID_ACTIVO))
                .tipoProveedorAuth(new TipoProveedorAuth(ID_LOCAL))
                .providerId(null)
                .estado(ESTADO_LOGICO_ACTIVO)
                .build();

        Usuario savedUser = usuarioRepository.save(nuevoUsuario);
        procesarSubtipo(savedUser, reqDto);

        String role = resolveRoleName(savedUser.getTipoUsuario().getId());
        String jwtToken = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getCorreo(), role);

        return AuthResDto.builder()
                .id(savedUser.getId())
                .email(savedUser.getCorreo())
                .fullName(construirNombreCompleto(savedUser.getNombres(), savedUser.getApellidos()))
                .pictureUrl(savedUser.getFotoPerfilUrl())
                .role(role)
                .token(jwtToken)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public ForgotPasswordResDto forgotPassword(ForgotPasswordReqDto reqDto) throws BadRequestException {
        String normalizedEmail = reqDto.getCorreo().trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByCorreo(normalizedEmail);

        if (usuario == null) {
            return ForgotPasswordResDto.builder()
                    .correo(normalizedEmail)
                    .mensaje(MENSAJE_RECUPERACION_GENERICO)
                    .build();
        }

        if (usuario.getTipoProveedorAuth() != null &&
                usuario.getTipoProveedorAuth().getId() == ID_GOOGLE &&
                !StringUtils.hasText(usuario.getContrasenaHash())) {
            throw new BadRequestException("Esta cuenta fue registrada mediante Google Sign-In. Debe iniciar sesión utilizando Google.");
        }

        validarEstadoUsuario(usuario);

        String resetToken = jwtTokenProvider.generatePasswordResetToken(usuario.getId(), usuario.getCorreo());
        String fullName = construirNombreCompleto(usuario.getNombres(), usuario.getApellidos());

        String htmlBody = String.format(
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;'>"
                        + "<h2 style='color: #2E7D32;'>PaseaPe - Recuperación de Contraseña</h2>"
                        + "<p>Hola <strong>%s</strong>,</p>"
                        + "<p>Hemos recibido una solicitud para restablecer la contraseña de tu cuenta.</p>"
                        + "<p>Usa el siguiente token para cambiar tu contraseña en la app (válido por 15 minutos):</p>"
                        + "<div style='background-color: #f4f6f8; padding: 12px; font-family: monospace; font-size: 13px; word-break: break-all; border-radius: 4px;'>%s</div>"
                        + "<p style='margin-top: 20px; color: #666; font-size: 12px;'>Si no solicitaste este cambio, puedes ignorar este correo de forma segura.</p>"
                        + "</div>",
                fullName, resetToken
        );

        brevoEmailHttpRepository.sendEmail(
                usuario,
                usuario.getCorreo(),
                fullName,
                ASUNTO_RECUPERACION_CONTRASENA,
                htmlBody,
                NOTIF_RECUPERACION_CONTRASENA
        );

        return ForgotPasswordResDto.builder()
                .correo(usuario.getCorreo())
                .mensaje(MENSAJE_RECUPERACION_GENERICO)
                .build();
    }

    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorCorreo(String correo) {
        if (!StringUtils.hasText(correo)) {
            return null;
        }
        return usuarioRepository.findByCorreo(correo.trim().toLowerCase());
    }

    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordReqDto reqDto) throws BadRequestException {
        if (!jwtTokenProvider.validatePasswordResetToken(reqDto.getToken())) {
            throw new BadRequestException("El token de restablecimiento es inválido o ha expirado.");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(reqDto.getToken());
        Usuario usuario = usuarioRepository.findById(userId);

        if (usuario == null) {
            throw new BadRequestException("El usuario asociado al token no existe.");
        }

        if (usuario.getEstado() != null && usuario.getEstado() == ESTADO_LOGICO_INACTIVO) {
            throw new BadRequestException("La cuenta de usuario se encuentra inactiva.");
        }

        usuario.setContrasenaHash(passwordEncoder.encode(reqDto.getNuevaContrasena()));
        usuarioRepository.save(usuario);
    }

    @Transactional(rollbackFor = Exception.class)
    public PerfilResDto actualizarPerfil(Long userId, ActualizarPerfilReqDto reqDto) throws BadRequestException {
        Usuario usuario = usuarioRepository.findById(userId);
        if (usuario == null) {
            throw new BadRequestException("El usuario no existe.");
        }

        validarEstadoUsuario(usuario);

        // Actualizar campos base de usuario
        if (StringUtils.hasText(reqDto.getNombres())) {
            usuario.setNombres(reqDto.getNombres().trim());
        }
        if (StringUtils.hasText(reqDto.getApellidos())) {
            usuario.setApellidos(reqDto.getApellidos().trim());
        }
        if (StringUtils.hasText(reqDto.getTelefono())) {
            usuario.setTelefono(reqDto.getTelefono().trim());
        }
        if (StringUtils.hasText(reqDto.getFotoPerfilUrl())) {
            usuario.setFotoPerfilUrl(reqDto.getFotoPerfilUrl().trim());
        }

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Actualizar subtipo e hidratar la respuesta según el tipo de usuario
        Integer tipoUsuarioId = usuarioGuardado.getTipoUsuario() != null ? usuarioGuardado.getTipoUsuario().getId() : null;
        PerfilResDto perfilResDto = perfilDtoMapper.toPerfilResDto(usuarioGuardado);

        if (tipoUsuarioId != null && tipoUsuarioId == ID_CLIENTE) {
            Cliente clienteActualizado = actualizarCliente(usuarioGuardado, reqDto);
            perfilDtoMapper.enrichClienteFields(clienteActualizado, perfilResDto);
        } else if (tipoUsuarioId != null && tipoUsuarioId == ID_PASEADOR) {
            Paseador paseadorActualizado = actualizarPaseador(usuarioGuardado, reqDto);
            perfilDtoMapper.enrichPaseadorFields(paseadorActualizado, perfilResDto);
        }

        return perfilResDto;
    }

    private Cliente actualizarCliente(Usuario usuario, ActualizarPerfilReqDto reqDto) throws BadRequestException {
        Cliente cliente = clienteRepository.findById(usuario.getId());
        if (cliente == null) {
            throw new BadRequestException("El perfil de cliente no existe.");
        }

        if (StringUtils.hasText(reqDto.getDireccionReferencia())) {
            cliente.setDireccionReferencia(reqDto.getDireccionReferencia().trim());
        }
        if (reqDto.getDistritoId() != null) {
            cliente.setDistrito(new DistritoLima(reqDto.getDistritoId()));
        }
        if (StringUtils.hasText(reqDto.getContactoEmergenciaNombre())) {
            cliente.setContactoEmergenciaNombre(reqDto.getContactoEmergenciaNombre().trim());
        }
        if (StringUtils.hasText(reqDto.getContactoEmergenciaTelefono())) {
            cliente.setContactoEmergenciaTelefono(reqDto.getContactoEmergenciaTelefono().trim());
        }
        if (StringUtils.hasText(reqDto.getNotasAdicionales())) {
            cliente.setNotasAdicionales(reqDto.getNotasAdicionales().trim());
        }

        return clienteRepository.save(cliente);
    }

    private Paseador actualizarPaseador(Usuario usuario, ActualizarPerfilReqDto reqDto) throws BadRequestException {
        Paseador paseador = paseadorRepository.findById(usuario.getId());
        if (paseador == null) {
            throw new BadRequestException("El perfil de paseador no existe.");
        }

        if (StringUtils.hasText(reqDto.getBiografia())) {
            paseador.setBiografia(reqDto.getBiografia().trim());
        }
        if (reqDto.getTarifaHoraPen() != null) {
            paseador.setTarifaHoraPen(reqDto.getTarifaHoraPen());
        }
        if (reqDto.getDistritoCoberturaId() != null) {
            paseador.setDistritoCobertura(new DistritoLima(reqDto.getDistritoCoberturaId()));
        }

        return paseadorRepository.save(paseador);
    }

    private void procesarSubtipo(Usuario usuario, UsuarioReqDto reqDto) throws BadRequestException {
        if (usuario.getTipoUsuario().getId() == ID_CLIENTE) {
            DistritoLima distrito = reqDto.getDistritoId() != null ? new DistritoLima(reqDto.getDistritoId()) : null;

            Cliente nuevoCliente = Cliente.builder()
                    .uuid(UUID.randomUUID().toString())
                    .usuario(usuario)
                    .direccionReferencia(reqDto.getDireccionReferencia())
                    .distrito(distrito)
                    .contactoEmergenciaNombre(reqDto.getContactoEmergenciaNombre())
                    .contactoEmergenciaTelefono(reqDto.getContactoEmergenciaTelefono())
                    .notasAdicionales(reqDto.getNotasAdicionales())
                    .estado(ESTADO_LOGICO_ACTIVO)
                    .build();

            Cliente savedCliente = clienteRepository.save(nuevoCliente);

            if (reqDto.getMascotas() != null && !reqDto.getMascotas().isEmpty()) {
                mascotaService.registrarMascotas(savedCliente, reqDto.getMascotas());
            }

        } else if (usuario.getTipoUsuario().getId() == ID_PASEADOR) {
            if (StringUtils.hasText(reqDto.getNumeroDocumento())) {
                Paseador existing = paseadorRepository.findByNumeroDocumento(reqDto.getNumeroDocumento().trim());
                if (existing != null) {
                    throw new BadRequestException("El número de documento " + reqDto.getNumeroDocumento() + " ya está en uso.");
                }
            }

            TipoDocumento tipoDoc = reqDto.getTipoDocumentoId() != null ? new TipoDocumento(reqDto.getTipoDocumentoId()) : null;
            DistritoLima distritoCobertura = reqDto.getDistritoCoberturaId() != null ? new DistritoLima(reqDto.getDistritoCoberturaId()) : null;
            PaseadorEstadoVerificacion estadoVerificacion = new PaseadorEstadoVerificacion(ID_PENDIENTE);

            Paseador nuevoPaseador = Paseador.builder()
                    .uuid(UUID.randomUUID().toString())
                    .usuario(usuario)
                    .tipoDocumento(tipoDoc)
                    .numeroDocumento(reqDto.getNumeroDocumento() != null ? reqDto.getNumeroDocumento().trim() : null)
                    .antecedentesPolicialesUrl(reqDto.getAntecedentesPolicialesUrl())
                    .experienciaAnos(reqDto.getExperienciaAnos() != null ? reqDto.getExperienciaAnos() : 0)
                    .biografia(reqDto.getBiografia())
                    .tarifaHoraPen(reqDto.getTarifaHoraPen() != null ? reqDto.getTarifaHoraPen() : BigDecimal.ZERO)
                    .distritoCobertura(distritoCobertura)
                    .estadoVerificacion(estadoVerificacion)
                    .paseosCompletados(0)
                    .calificacionPromedio(BigDecimal.ZERO)
                    .estado(ESTADO_LOGICO_ACTIVO)
                    .build();

            paseadorRepository.save(nuevoPaseador);
        }
    }

    private void validarEstadoUsuario(Usuario usuario) throws BadRequestException {
        if (usuario.getUsuarioEstado() != null && usuario.getUsuarioEstado().getId() == ID_BLOQUEADO) {
            throw new BadRequestException("La cuenta de usuario se encuentra bloqueada.");
        }
        if (usuario.getEstado() != null && usuario.getEstado() == ESTADO_LOGICO_INACTIVO) {
            throw new BadRequestException("La cuenta de usuario se encuentra inactiva.");
        }
    }

    private String resolveRoleName(Integer tipoUsuarioId) {
        if (tipoUsuarioId != null && tipoUsuarioId == ID_ADMINISTRADOR) {
            return ROL_ADMINISTRADOR;
        }
        if (tipoUsuarioId != null && tipoUsuarioId == ID_PASEADOR) {
            return ROL_PASEADOR;
        }
        return ROL_CLIENTE;
    }

    private String construirNombreCompleto(String nombres, String apellidos) {
        String n = StringUtils.hasText(nombres) ? nombres.trim() : "";
        String a = StringUtils.hasText(apellidos) ? apellidos.trim() : "";
        return (n + " " + a).trim();
    }
}