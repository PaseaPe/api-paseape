package com.paseape.apipaseape.application.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IDistritoLimaRepository;
import com.paseape.apipaseape.application.repository.IMascotaRepository;
import com.paseape.apipaseape.application.repository.IPaseadorEstadoVerificacionRepository;
import com.paseape.apipaseape.application.repository.IPaseadorRepository;
import com.paseape.apipaseape.application.repository.ITipoDocumentoRepository;
import com.paseape.apipaseape.application.repository.ITipoGeneroMascotaRepository;
import com.paseape.apipaseape.application.repository.ITipoMascotaRepository;
import com.paseape.apipaseape.application.repository.ITipoNivelEnergiaRepository;
import com.paseape.apipaseape.application.repository.ITipoProveedorAuthRepository;
import com.paseape.apipaseape.application.repository.ITipoRazaRepository;
import com.paseape.apipaseape.application.repository.ITipoTamanoMascotaRepository;
import com.paseape.apipaseape.application.repository.ITipoUsuarioRepository;
import com.paseape.apipaseape.application.repository.IUsuarioEstadoRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.DistritoLima;
import com.paseape.apipaseape.domain.entity.Mascota;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.domain.entity.PaseadorEstadoVerificacion;
import com.paseape.apipaseape.domain.entity.TipoDocumento;
import com.paseape.apipaseape.domain.entity.TipoGeneroMascota;
import com.paseape.apipaseape.domain.entity.TipoMascota;
import com.paseape.apipaseape.domain.entity.TipoNivelEnergia;
import com.paseape.apipaseape.domain.entity.TipoProveedorAuth;
import com.paseape.apipaseape.domain.entity.TipoRaza;
import com.paseape.apipaseape.domain.entity.TipoTamanoMascota;
import com.paseape.apipaseape.domain.entity.TipoUsuario;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.domain.entity.UsuarioEstado;
import com.paseape.apipaseape.infrastructure.dto.request.*;
import com.paseape.apipaseape.infrastructure.dto.response.AuthResDto;
import com.paseape.apipaseape.infrastructure.dto.response.ForgotPasswordResDto;
import com.paseape.apipaseape.infrastructure.dto.response.LogoutResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.repository.http.BrevoEmailHttpRepository;
import com.paseape.apipaseape.infrastructure.repository.http.GoogleTokenVerifierHttpRepository;
import com.paseape.apipaseape.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.UUID;

import static com.paseape.apipaseape.infrastructure.constant.Constant.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleTokenVerifierHttpRepository googleTokenVerifier;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    private final IUsuarioRepository usuarioRepository;
    private final IClienteRepository clienteRepository;
    private final IPaseadorRepository paseadorRepository;
    private final IMascotaRepository mascotaRepository;

    private final ITipoUsuarioRepository tipoUsuarioRepository;
    private final IUsuarioEstadoRepository usuarioEstadoRepository;
    private final ITipoProveedorAuthRepository tipoProveedorAuthRepository;
    private final IDistritoLimaRepository distritoLimaRepository;
    private final ITipoDocumentoRepository tipoDocumentoRepository;
    private final IPaseadorEstadoVerificacionRepository paseadorEstadoVerificacionRepository;
    private final ITipoMascotaRepository tipoMascotaRepository;
    private final ITipoRazaRepository tipoRazaRepository;
    private final ITipoGeneroMascotaRepository tipoGeneroMascotaRepository;
    private final ITipoTamanoMascotaRepository tipoTamanoMascotaRepository;
    private final ITipoNivelEnergiaRepository tipoNivelEnergiaRepository;
    private final BrevoEmailHttpRepository brevoEmailHttpRepository;

    @Transactional(readOnly = true)
    public AuthResDto authenticateWithGoogle(GoogleAuthReqDto reqDto) throws BadRequestException {
        if (reqDto == null || !StringUtils.hasText(reqDto.getIdToken())) {
            throw new BadRequestException("El token de Google (id_token) es estrictamente obligatorio.");
        }

        GoogleIdToken.Payload payload = googleTokenVerifier.verify(reqDto.getIdToken());
        String email = payload.getEmail() != null ? payload.getEmail().trim().toLowerCase() : null;
        String googleSub = payload.getSubject();

        if (!StringUtils.hasText(email)) {
            throw new BadRequestException("No se pudo obtener el correo electronico del token de Google.");
        }

        Usuario usuario = usuarioRepository.findByCorreo(email);
        if (usuario == null && StringUtils.hasText(googleSub)) {
            usuario = usuarioRepository.findByProviderId(googleSub);
        }

        if (usuario == null) {
            throw new BadRequestException("El usuario con correo " + email + " no se encuentra registrado. Complete su registro antes de iniciar sesion.");
        }

        if (usuario.getUsuarioEstado() != null &&
                usuario.getUsuarioEstado().getId() != null &&
                usuario.getUsuarioEstado().getId() == ID_BLOQUEADO) {
            throw new BadRequestException("La cuenta de usuario se encuentra bloqueada.");
        }

        if (usuario.getEstado() != null && usuario.getEstado() == 0) {
            throw new BadRequestException("La cuenta de usuario se encuentra inactiva.");
        }

        String role = resolveRoleName(usuario.getTipoUsuario() != null ? usuario.getTipoUsuario().getId() : null);
        String token = jwtTokenProvider.generateToken(usuario.getId(), usuario.getCorreo(), role);

        String fullName = (StringUtils.hasText(usuario.getNombres()) ? usuario.getNombres() : "") +
                (StringUtils.hasText(usuario.getApellidos()) ? " " + usuario.getApellidos() : "");

        return AuthResDto.builder()
                .id(usuario.getId())
                .email(usuario.getCorreo())
                .fullName(fullName.trim())
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
            throw new BadRequestException("Credenciales de acceso invalidas.");
        }

        if (!StringUtils.hasText(usuario.getContrasenaHash()) ||
                !passwordEncoder.matches(reqDto.getContrasena(), usuario.getContrasenaHash())) {
            throw new BadRequestException("Credenciales de acceso invalidas.");
        }

        if (usuario.getUsuarioEstado() != null &&
                usuario.getUsuarioEstado().getId() != null &&
                usuario.getUsuarioEstado().getId() == ID_BLOQUEADO) {
            throw new BadRequestException("La cuenta de usuario se encuentra bloqueada.");
        }

        if (usuario.getEstado() != null && usuario.getEstado() == 0) {
            throw new BadRequestException("La cuenta de usuario se encuentra inactiva.");
        }

        String role = resolveRoleName(usuario.getTipoUsuario() != null ? usuario.getTipoUsuario().getId() : null);
        String token = jwtTokenProvider.generateToken(usuario.getId(), usuario.getCorreo(), role);

        String fullName = (StringUtils.hasText(usuario.getNombres()) ? usuario.getNombres() : "") +
                (StringUtils.hasText(usuario.getApellidos()) ? " " + usuario.getApellidos() : "");

        return AuthResDto.builder()
                .id(usuario.getId())
                .email(usuario.getCorreo())
                .fullName(fullName.trim())
                .pictureUrl(usuario.getFotoPerfilUrl())
                .role(role)
                .token(token)
                .build();
    }

    public LogoutResDto logout(String correoAutenticado) {
        SecurityContextHolder.clearContext();

        boolean esUsuarioGoogle = false;
        Usuario usuario = usuarioRepository.findByCorreo(correoAutenticado);
        if (usuario != null && usuario.getTipoProveedorAuth() != null) {
            esUsuarioGoogle = usuario.getTipoProveedorAuth().getId() == ID_GOOGLE;
        }

        return LogoutResDto.builder()
                .correo(correoAutenticado)
                .mensaje("Sesion invalidada exitosamente en el servidor.")
                .requiereRevocacionGoogle(esUsuarioGoogle)
                .build();
    }

    @Transactional
    public AuthResDto registerWithGoogle(UsuarioReqDto reqDto) throws BadRequestException {
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(reqDto.getIdToken());
        String email = payload.getEmail().trim().toLowerCase();
        String googleSub = payload.getSubject();

        if (usuarioRepository.existsByCorreo(email)) {
            throw new BadRequestException("Ya existe una cuenta registrada con el correo: " + email);
        }
        if (usuarioRepository.findByProviderId(googleSub) != null) {
            throw new BadRequestException("La cuenta de Google ya esta vinculada a otro perfil de usuario.");
        }

        String nombres = StringUtils.hasText(reqDto.getNombres()) ? reqDto.getNombres().trim() : (String) payload.get("given_name");
        String apellidos = StringUtils.hasText(reqDto.getApellidos()) ? reqDto.getApellidos().trim() : (String) payload.get("family_name");
        if (!StringUtils.hasText(nombres)) {
            nombres = (String) payload.get("name");
        }
        if (!StringUtils.hasText(apellidos)) {
            apellidos = "";
        }

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
                .tipoUsuario(resolveTipoUsuario(reqDto.getTipoUsuarioId()))
                .usuarioEstado(resolveUsuarioEstado(ID_ACTIVO))
                .tipoProveedorAuth(resolveTipoProveedorAuth(ID_GOOGLE))
                .providerId(googleSub)
                .estado(1)
                .build();

        Usuario savedUser = usuarioRepository.save(nuevoUsuario);
        procesarSubtipo(savedUser, reqDto);

        String role = resolveRoleName(savedUser.getTipoUsuario().getId());
        String jwtToken = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getCorreo(), role);

        return AuthResDto.builder()
                .id(savedUser.getId())
                .email(savedUser.getCorreo())
                .fullName(savedUser.getNombres() + " " + savedUser.getApellidos())
                .pictureUrl(savedUser.getFotoPerfilUrl())
                .role(role)
                .token(jwtToken)
                .build();
    }

    @Transactional
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
                .tipoUsuario(resolveTipoUsuario(reqDto.getTipoUsuarioId()))
                .usuarioEstado(resolveUsuarioEstado(ID_ACTIVO))
                .tipoProveedorAuth(resolveTipoProveedorAuth(ID_LOCAL))
                .providerId(null)
                .estado(1)
                .build();

        Usuario savedUser = usuarioRepository.save(nuevoUsuario);
        procesarSubtipo(savedUser, reqDto);

        String role = resolveRoleName(savedUser.getTipoUsuario().getId());
        String jwtToken = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getCorreo(), role);

        return AuthResDto.builder()
                .id(savedUser.getId())
                .email(savedUser.getCorreo())
                .fullName(savedUser.getNombres() + " " + savedUser.getApellidos())
                .pictureUrl(savedUser.getFotoPerfilUrl())
                .role(role)
                .token(jwtToken)
                .build();
    }

    private void procesarSubtipo(Usuario usuario, UsuarioReqDto reqDto) throws BadRequestException {
        if (usuario.getTipoUsuario().getId() == ID_CLIENTE) {
            DistritoLima distrito = null;
            if (reqDto.getDistritoId() != null) {
                distrito = distritoLimaRepository.findById(reqDto.getDistritoId());
                if (distrito == null) {
                    throw new BadRequestException("El distrito especificado con ID " + reqDto.getDistritoId() + " no existe.");
                }
            }

            Cliente nuevoCliente = Cliente.builder()
                    .id(usuario.getId())
                    .uuid(UUID.randomUUID().toString())
                    .usuario(usuario)
                    .direccionReferencia(reqDto.getDireccionReferencia())
                    .distrito(distrito)
                    .contactoEmergenciaNombre(reqDto.getContactoEmergenciaNombre())
                    .contactoEmergenciaTelefono(reqDto.getContactoEmergenciaTelefono())
                    .notasAdicionales(reqDto.getNotasAdicionales())
                    .estado(1)
                    .build();

            Cliente savedCliente = clienteRepository.save(nuevoCliente);

            if (reqDto.getMascota() != null && StringUtils.hasText(reqDto.getMascota().getNombre())) {
                registrarMascota(savedCliente, reqDto.getMascota());
            }

        } else if (usuario.getTipoUsuario().getId() == ID_PASEADOR) {
            if (StringUtils.hasText(reqDto.getNumeroDocumento())) {
                Paseador existingPaseador = paseadorRepository.findByNumeroDocumento(reqDto.getNumeroDocumento().trim());
                if (existingPaseador != null) {
                    throw new BadRequestException("El numero de documento " + reqDto.getNumeroDocumento() + " ya esta registrado.");
                }
            }

            TipoDocumento tipoDoc = null;
            if (reqDto.getTipoDocumentoId() != null) {
                tipoDoc = tipoDocumentoRepository.findById(reqDto.getTipoDocumentoId());
                if (tipoDoc == null) {
                    throw new BadRequestException("El tipo de documento con ID " + reqDto.getTipoDocumentoId() + " no existe.");
                }
            }

            DistritoLima distritoCobertura = null;
            if (reqDto.getDistritoCoberturaId() != null) {
                distritoCobertura = distritoLimaRepository.findById(reqDto.getDistritoCoberturaId());
                if (distritoCobertura == null) {
                    throw new BadRequestException("El distrito de cobertura con ID " + reqDto.getDistritoCoberturaId() + " no existe.");
                }
            }

            PaseadorEstadoVerificacion estadoVerificacion = paseadorEstadoVerificacionRepository.findById(ID_PENDIENTE);

            Paseador nuevoPaseador = Paseador.builder()
                    .id(usuario.getId())
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
                    .estado(1)
                    .build();

            paseadorRepository.save(nuevoPaseador);
        }
    }

    private void registrarMascota(Cliente cliente, MascotaReqDto dto) throws BadRequestException {
        TipoMascota tipoMascota = tipoMascotaRepository.findById(dto.getTipoMascotaId());
        if (tipoMascota == null) {
            throw new BadRequestException("El tipo de mascota con ID " + dto.getTipoMascotaId() + " no existe.");
        }

        TipoRaza tipoRaza = tipoRazaRepository.findById(dto.getTipoRazaId());
        if (tipoRaza == null) {
            throw new BadRequestException("La raza con ID " + dto.getTipoRazaId() + " no existe.");
        }

        TipoGeneroMascota tipoGenero = tipoGeneroMascotaRepository.findById(dto.getTipoGeneroMascotaId());
        if (tipoGenero == null) {
            throw new BadRequestException("El genero con ID " + dto.getTipoGeneroMascotaId() + " no existe.");
        }

        TipoTamanoMascota tipoTamano = tipoTamanoMascotaRepository.findById(dto.getTipoTamanoMascotaId());
        if (tipoTamano == null) {
            throw new BadRequestException("El tamano con ID " + dto.getTipoTamanoMascotaId() + " no existe.");
        }

        TipoNivelEnergia tipoEnergia = tipoNivelEnergiaRepository.findById(dto.getTipoNivelEnergiaId());
        if (tipoEnergia == null) {
            throw new BadRequestException("El nivel de energia con ID " + dto.getTipoNivelEnergiaId() + " no existe.");
        }

        Mascota mascota = Mascota.builder()
                .uuid(UUID.randomUUID().toString())
                .cliente(cliente)
                .nombre(dto.getNombre().trim())
                .tipoMascota(tipoMascota)
                .tipoRaza(tipoRaza)
                .tipoGeneroMascota(tipoGenero)
                .tipoTamanoMascota(tipoTamano)
                .tipoNivelEnergia(tipoEnergia)
                .edadAnos(dto.getEdadAnos() != null ? dto.getEdadAnos() : 0)
                .edadMeses(dto.getEdadMeses() != null ? dto.getEdadMeses() : 0)
                .pesoKg(dto.getPesoKg())
                .esterilizado(dto.getEsterilizado() != null ? dto.getEsterilizado() : 0)
                .sociableConPerros(dto.getSociableConPerros() != null ? dto.getSociableConPerros() : 1)
                .sociableConPersonas(dto.getSociableConPersonas() != null ? dto.getSociableConPersonas() : 1)
                .precaucionesMedicas(dto.getPrecaucionesMedicas())
                .fotoUrl(dto.getFotoUrl())
                .estado(1)
                .build();

        mascotaRepository.save(mascota);
    }

    @Transactional
    public ForgotPasswordResDto forgotPassword(ForgotPasswordReqDto reqDto) throws BadRequestException {
        String normalizedEmail = reqDto.getCorreo().trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByCorreo(normalizedEmail);

        // Prevención de enumeración de usuarios: Si no existe, se retorna el mensaje de éxito genérico
        if (usuario == null) {
            return ForgotPasswordResDto.builder()
                    .correo(normalizedEmail)
                    .mensaje("Si el correo se encuentra registrado en PaseaPe, recibira un mensaje con las instrucciones.")
                    .build();
        }

        // Si el usuario es exclusivo de Google y no tiene contraseña local
        if (usuario.getTipoProveedorAuth() != null &&
                usuario.getTipoProveedorAuth().getId() == ID_GOOGLE &&
                !StringUtils.hasText(usuario.getContrasenaHash())) {
            throw new BadRequestException("Esta cuenta fue registrada mediante Google Sign-In. Debe iniciar sesion utilizando el boton de Google.");
        }

        String resetToken = jwtTokenProvider.generatePasswordResetToken(usuario.getId(), usuario.getCorreo());
        String fullName = (usuario.getNombres() != null ? usuario.getNombres() : "") +
                (usuario.getApellidos() != null ? " " + usuario.getApellidos() : "");

        String htmlBody = String.format(
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;'>"
                        + "<h2 style='color: #2E7D32;'>PaseaPe - Recuperación de Contraseña</h2>"
                        + "<p>Hola <strong>%s</strong>,</p>"
                        + "<p>Hemos recibido una solicitud para restablecer la contraseña de tu cuenta.</p>"
                        + "<p>Usa el siguiente token para cambiar tu contraseña en la app (valido por 15 minutos):</p>"
                        + "<div style='background-color: #f4f6f8; padding: 12px; font-family: monospace; font-size: 13px; word-break: break-all; border-radius: 4px;'>%s</div>"
                        + "<p style='margin-top: 20px; color: #666; font-size: 12px;'>Si no solicitaste este cambio, puedes ignorar este correo de forma segura.</p>"
                        + "</div>",
                fullName.trim(), resetToken
        );

        brevoEmailHttpRepository.sendEmail(
                usuario,
                usuario.getCorreo(),
                fullName.trim(),
                ASUNTO_RECUPERACION_CONTRASENA,
                htmlBody,
                NOTIF_RECUPERACION_CONTRASENA
        );

        return ForgotPasswordResDto.builder()
                .correo(usuario.getCorreo())
                .mensaje("Si el correo se encuentra registrado en PaseaPe, recibira un mensaje con las instrucciones.")
                .build();
    }

    @Transactional
    public void resetPassword(ResetPasswordReqDto reqDto) throws BadRequestException {
        if (!jwtTokenProvider.validatePasswordResetToken(reqDto.getToken())) {
            throw new BadRequestException("El token de restablecimiento es invalido o ha expirado.");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(reqDto.getToken());
        Usuario usuario = usuarioRepository.findById(userId);

        if (usuario == null) {
            throw new BadRequestException("El usuario asociado al token no existe.");
        }

        if (usuario.getEstado() != null && usuario.getEstado() == 0) {
            throw new BadRequestException("La cuenta de usuario se encuentra inactiva.");
        }

        usuario.setContrasenaHash(passwordEncoder.encode(reqDto.getNuevaContrasena()));
        usuarioRepository.save(usuario);
    }

    private TipoUsuario resolveTipoUsuario(Integer id) throws BadRequestException {
        TipoUsuario entity = tipoUsuarioRepository.findById(id);
        if (entity == null) {
            throw new BadRequestException("El tipo de usuario indicado no existe.");
        }
        return entity;
    }

    private UsuarioEstado resolveUsuarioEstado(Integer id) throws BadRequestException {
        UsuarioEstado entity = usuarioEstadoRepository.findById(id);
        if (entity == null) {
            throw new BadRequestException("El estado de usuario indicado no existe.");
        }
        return entity;
    }

    private TipoProveedorAuth resolveTipoProveedorAuth(Integer id) throws BadRequestException {
        TipoProveedorAuth entity = tipoProveedorAuthRepository.findById(id);
        if (entity == null) {
            throw new BadRequestException("El proveedor de autenticacion no existe.");
        }
        return entity;
    }

    private String resolveRoleName(Integer tipoUsuarioId) {
        if (tipoUsuarioId != null) {
            if (tipoUsuarioId == ID_PASEADOR) {
                return ROL_PASEADOR;
            }
            if (tipoUsuarioId == ID_ADMINISTRADOR) {
                return ROL_ADMINISTRADOR;
            }
        }
        return ROL_CLIENTE;
    }
}