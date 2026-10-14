package com.paseape.apipaseape.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IMascotaRepository;
import com.paseape.apipaseape.application.repository.IPaseadorRepository;
import com.paseape.apipaseape.application.repository.IUsuarioEstadoRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.domain.entity.PaseadorEstadoVerificacion;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.domain.entity.UsuarioEstado;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarEstadoUsuarioReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarVerificacionPaseadorReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.MascotaResDto;
import com.paseape.apipaseape.infrastructure.dto.response.PaseadorAdminResDto;
import com.paseape.apipaseape.infrastructure.dto.response.UsuarioAdminResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.IMascotaDtoMapper;
import com.paseape.apipaseape.infrastructure.mapper.dto.IPaseadorAdminDtoMapper;
import com.paseape.apipaseape.infrastructure.mapper.dto.IUsuarioAdminDtoMapper;

import java.util.List;

import static com.paseape.apipaseape.infrastructure.constant.Constant.ESTADO_LOGICO_ACTIVO;
import static com.paseape.apipaseape.infrastructure.constant.Constant.ID_APROBADO;
import static com.paseape.apipaseape.infrastructure.constant.Constant.ID_CLIENTE;

@Service
@RequiredArgsConstructor
public class AdminUsuarioService {

    private final IUsuarioRepository usuarioRepository;
    private final IUsuarioEstadoRepository usuarioEstadoRepository;
    private final IClienteRepository clienteRepository;
    private final IMascotaRepository mascotaRepository;
    private final IPaseadorRepository paseadorRepository;
    private final IUsuarioAdminDtoMapper usuarioAdminDtoMapper;
    private final IMascotaDtoMapper mascotaDtoMapper;
    private final IPaseadorAdminDtoMapper paseadorAdminDtoMapper;

    // --- Usuarios ---

    @Transactional(readOnly = true)
    public List<UsuarioAdminResDto> listarUsuarios(Integer tipoUsuarioId, Integer usuarioEstadoId) {
        List<Usuario> usuarios = usuarioRepository.findAllByEstado(ESTADO_LOGICO_ACTIVO);

        if (tipoUsuarioId != null) {
            usuarios = usuarios.stream()
                    .filter(usuario -> usuario.getTipoUsuario() != null && tipoUsuarioId.equals(usuario.getTipoUsuario().getId()))
                    .toList();
        }

        if (usuarioEstadoId != null) {
            usuarios = usuarios.stream()
                    .filter(usuario -> usuario.getUsuarioEstado() != null && usuarioEstadoId.equals(usuario.getUsuarioEstado().getId()))
                    .toList();
        }

        return usuarioAdminDtoMapper.toResDtoList(usuarios);
    }

    @Transactional(readOnly = true)
    public UsuarioAdminResDto obtenerUsuario(String uuid) throws BadRequestException {
        Usuario usuario = usuarioRepository.findByUuid(uuid);
        if (usuario == null) {
            throw new BadRequestException("El usuario especificado no existe.");
        }

        UsuarioAdminResDto resDto = usuarioAdminDtoMapper.toResDto(usuario);

        // Si es CLIENTE, incluye todas sus mascotas
        if (usuario.getTipoUsuario() != null && usuario.getTipoUsuario().getId() == ID_CLIENTE) {
            Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId());
            if (cliente != null) {
                List<MascotaResDto> mascotas = mascotaDtoMapper.toResDtoList(
                        mascotaRepository.findAllByClienteId(cliente.getId()));
                resDto.setMascotas(mascotas);
            }
        }

        return resDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public UsuarioAdminResDto actualizarEstado(String uuid, ActualizarEstadoUsuarioReqDto reqDto) throws BadRequestException {
        Usuario usuario = usuarioRepository.findByUuid(uuid);
        if (usuario == null) {
            throw new BadRequestException("El usuario especificado no existe.");
        }

        UsuarioEstado nuevoEstado = usuarioEstadoRepository.findById(reqDto.getUsuarioEstadoId());
        if (nuevoEstado == null) {
            throw new BadRequestException("El estado de usuario especificado no existe.");
        }

        usuario.setUsuarioEstado(nuevoEstado);
        usuarioRepository.save(usuario);
        return usuarioAdminDtoMapper.toResDto(usuario);
    }

    // --- Paseadores ---

    @Transactional(readOnly = true)
    public List<PaseadorAdminResDto> listarPaseadores(Integer estadoVerificacionId) {
        List<Paseador> paseadores;
        if (estadoVerificacionId != null) {
            paseadores = paseadorRepository.findAllByEstadoVerificacionId(estadoVerificacionId);
        } else {
            paseadores = paseadorRepository.findAllByEstado(ESTADO_LOGICO_ACTIVO);
        }
        return paseadorAdminDtoMapper.toResDtoList(paseadores);
    }

    @Transactional(readOnly = true)
    public PaseadorAdminResDto obtenerPaseador(String uuid) throws BadRequestException {
        Paseador paseador = paseadorRepository.findByUuid(uuid);
        if (paseador == null) {
            throw new BadRequestException("El paseador especificado no existe.");
        }
        return paseadorAdminDtoMapper.toResDto(paseador);
    }

    @Transactional(rollbackFor = Exception.class)
    public PaseadorAdminResDto actualizarVerificacion(String uuid, ActualizarVerificacionPaseadorReqDto reqDto) throws BadRequestException {
        Paseador paseador = paseadorRepository.findByUuid(uuid);
        if (paseador == null) {
            throw new BadRequestException("El paseador especificado no existe.");
        }

        Integer nuevoEstado = reqDto.getEstadoVerificacionId();
        if (nuevoEstado == null) {
            throw new BadRequestException("El estado de verificacion es obligatorio.");
        }

        if (nuevoEstado == ID_APROBADO && !StringUtils.hasText(paseador.getAntecedentesPolicialesUrl())) {
            throw new BadRequestException("No se puede aprobar al paseador: no ha adjuntado antecedentes policiales.");
        }

        paseador.setEstadoVerificacion(new PaseadorEstadoVerificacion(nuevoEstado));
        Paseador paseadorActualizado = paseadorRepository.save(paseador);
        return paseadorAdminDtoMapper.toResDto(paseadorActualizado);
    }
}
