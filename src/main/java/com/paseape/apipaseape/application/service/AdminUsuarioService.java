package com.paseape.apipaseape.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paseape.apipaseape.application.repository.IUsuarioEstadoRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.domain.entity.UsuarioEstado;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarEstadoUsuarioReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.UsuarioAdminResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.IUsuarioAdminDtoMapper;

import java.util.List;

import static com.paseape.apipaseape.infrastructure.constant.Constant.ESTADO_LOGICO_ACTIVO;

@Service
@RequiredArgsConstructor
public class AdminUsuarioService {

    private final IUsuarioRepository usuarioRepository;
    private final IUsuarioEstadoRepository usuarioEstadoRepository;
    private final IUsuarioAdminDtoMapper usuarioAdminDtoMapper;

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
}
