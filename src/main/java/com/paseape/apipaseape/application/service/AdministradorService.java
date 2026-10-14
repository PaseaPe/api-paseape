package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.IAdministradorRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Administrador;
import com.paseape.apipaseape.domain.entity.TipoProveedorAuth;
import com.paseape.apipaseape.domain.entity.TipoUsuario;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.domain.entity.UsuarioEstado;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarAdministradorReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.CrearAdministradorReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.AdministradorResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.IAdministradorDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

import static com.paseape.apipaseape.infrastructure.constant.Constant.ESTADO_LOGICO_ACTIVO;
import static com.paseape.apipaseape.infrastructure.constant.Constant.ID_ACTIVO;
import static com.paseape.apipaseape.infrastructure.constant.Constant.ID_ADMINISTRADOR;
import static com.paseape.apipaseape.infrastructure.constant.Constant.ID_LOCAL;

@Service
@RequiredArgsConstructor
public class AdministradorService {

    private final IAdministradorRepository administradorRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IAdministradorDtoMapper administradorDtoMapper;
    private final PasswordEncoder passwordEncoder;

    private static final String PREFIJO_FOLIO = "EMP-";
    private static final String FORMATO_FOLIO = "EMP-%05d";

    @Transactional(rollbackFor = Exception.class)
    public AdministradorResDto crearAdmin(CrearAdministradorReqDto reqDto) throws BadRequestException {
        String normalizedEmail = reqDto.getCorreo().trim().toLowerCase();

        if (usuarioRepository.existsByCorreo(normalizedEmail)) {
            throw new BadRequestException("El correo ingresado ya se encuentra registrado: " + normalizedEmail);
        }

        String codigoEmpleadoGenerado = generarSiguienteCodigoEmpleado();

        Usuario nuevoUsuario = Usuario.builder()
                .uuid(UUID.randomUUID().toString())
                .nombres(reqDto.getNombres().trim())
                .apellidos(reqDto.getApellidos().trim())
                .correo(normalizedEmail)
                .correoVerificado(1)
                .contrasenaHash(passwordEncoder.encode(reqDto.getContrasena()))
                .telefono(StringUtils.hasText(reqDto.getTelefono()) ? reqDto.getTelefono().trim() : null)
                .fotoPerfilUrl(StringUtils.hasText(reqDto.getFotoPerfilUrl()) ? reqDto.getFotoPerfilUrl().trim() : null)
                .tipoUsuario(new TipoUsuario(ID_ADMINISTRADOR))
                .usuarioEstado(new UsuarioEstado(ID_ACTIVO))
                .tipoProveedorAuth(new TipoProveedorAuth(ID_LOCAL))
                .providerId(null)
                .estado(ESTADO_LOGICO_ACTIVO)
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        Administrador nuevoAdmin = Administrador.builder()
                .uuid(UUID.randomUUID().toString())
                .usuario(usuarioGuardado)
                .codigoEmpleado(codigoEmpleadoGenerado)
                .areaDepartamento(StringUtils.hasText(reqDto.getAreaDepartamento()) ? reqDto.getAreaDepartamento().trim() : null)
                .superadmin(reqDto.getSuperadmin() != null ? reqDto.getSuperadmin() : 0)
                .estado(ESTADO_LOGICO_ACTIVO)
                .build();

        Administrador adminGuardado = administradorRepository.save(nuevoAdmin);
        return administradorDtoMapper.toResDto(adminGuardado);
    }

    @Transactional(rollbackFor = Exception.class)
    public AdministradorResDto editarAdmin(String adminUuid, ActualizarAdministradorReqDto reqDto) throws BadRequestException {
        Administrador admin = administradorRepository.findByUuid(adminUuid.trim());
        if (admin == null) {
            throw new BadRequestException("No existe un administrador registrado con el UUID: " + adminUuid);
        }

        Usuario usuario = admin.getUsuario();
        if (usuario == null && admin.getId() != null) {
            usuario = usuarioRepository.findById(admin.getId());
        }

        if (usuario == null) {
            throw new BadRequestException("No se encontro el registro de usuario asociado al administrador.");
        }

        if (StringUtils.hasText(reqDto.getNombres())) {
            usuario.setNombres(reqDto.getNombres().trim());
        }
        if (StringUtils.hasText(reqDto.getApellidos())) {
            usuario.setApellidos(reqDto.getApellidos().trim());
        }
        if (reqDto.getTelefono() != null) {
            usuario.setTelefono(StringUtils.hasText(reqDto.getTelefono()) ? reqDto.getTelefono().trim() : null);
        }
        if (reqDto.getFotoPerfilUrl() != null) {
            usuario.setFotoPerfilUrl(StringUtils.hasText(reqDto.getFotoPerfilUrl()) ? reqDto.getFotoPerfilUrl().trim() : null);
        }
        if (reqDto.getUsuarioEstadoId() != null) {
            usuario.setUsuarioEstado(new UsuarioEstado(reqDto.getUsuarioEstadoId()));
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        if (reqDto.getAreaDepartamento() != null) {
            admin.setAreaDepartamento(StringUtils.hasText(reqDto.getAreaDepartamento()) ? reqDto.getAreaDepartamento().trim() : null);
        }
        if (reqDto.getSuperadmin() != null) {
            admin.setSuperadmin(reqDto.getSuperadmin());
        }

        admin.setUsuario(usuarioActualizado);
        Administrador adminActualizado = administradorRepository.save(admin);

        return administradorDtoMapper.toResDto(adminActualizado);
    }

    private String generarSiguienteCodigoEmpleado() {
        Administrador ultimo = administradorRepository.findUltimoRegistrado();
        int siguienteNumero = 1;

        if (ultimo != null && StringUtils.hasText(ultimo.getCodigoEmpleado())) {
            String codigo = ultimo.getCodigoEmpleado().trim();
            if (codigo.startsWith(PREFIJO_FOLIO)) {
                String parteNumerica = codigo.substring(PREFIJO_FOLIO.length());
                try {
                    siguienteNumero = Integer.parseInt(parteNumerica) + 1;
                } catch (NumberFormatException ignored) {
                    siguienteNumero = 1;
                }
            }
        }

        return String.format(FORMATO_FOLIO, siguienteNumero);
    }
}