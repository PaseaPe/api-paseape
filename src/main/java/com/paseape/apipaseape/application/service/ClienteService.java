package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IDigitalDocumentRepository;
import com.paseape.apipaseape.application.repository.IMascotaRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.DigitalDocument;
import com.paseape.apipaseape.domain.entity.Mascota;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.dto.response.ClienteResDto;
import com.paseape.apipaseape.infrastructure.dto.response.UsuarioResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.IClienteDtoMapper;
import com.paseape.apipaseape.infrastructure.mapper.dto.IMascotaDtoMapper;
import com.paseape.apipaseape.infrastructure.mapper.dto.IUsuarioDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.paseape.apipaseape.infrastructure.constant.Constant.ID_DOC_FOTO_MASCOTA;
import static com.paseape.apipaseape.infrastructure.constant.Constant.ID_DOC_FOTO_PERFIL;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final DigitalDocumentService digitalDocumentService;

    private final IClienteRepository clienteRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IMascotaRepository mascotaRepository;
    private final IDigitalDocumentRepository digitalDocumentRepository;

    private final IUsuarioDtoMapper usuarioDtoMapper;
    private final IClienteDtoMapper clienteDtoMapper;
    private final IMascotaDtoMapper mascotaDtoMapper;

    @Transactional(readOnly = true)
    public UsuarioResDto obtenerCliente(String uuid) throws BadRequestException {
        Usuario usuario = usuarioRepository.findByUuid(uuid);
        if (usuario == null){
            throw new BadRequestException("No se encontro el usuario");
        };
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId());
        if (cliente == null){
            throw new BadRequestException("No se encontro el cliente");
        };

        List<Mascota> mascotas = mascotaRepository.findAllByClienteId(cliente.getId());

        UsuarioResDto usuarioResDto = usuarioDtoMapper.toResDto(usuario,cliente,null, clienteDtoMapper,null);
        usuarioResDto.getCliente().setMascotas(mascotaDtoMapper.toResDtoList(mascotas));
        usuarioResDto.setFotoPerfilUrl(digitalDocumentService.urlFotoPerfil(usuario));

        return usuarioResDto;
    }

}
