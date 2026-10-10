package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.infrastructure.mapper.dto.IMascotaDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IMascotaRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.Mascota;
import com.paseape.apipaseape.domain.entity.TipoGeneroMascota;
import com.paseape.apipaseape.domain.entity.TipoMascota;
import com.paseape.apipaseape.domain.entity.TipoNivelEnergia;
import com.paseape.apipaseape.domain.entity.TipoRaza;
import com.paseape.apipaseape.domain.entity.TipoTamanoMascota;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarMascotaReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.MascotaReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.RegistrarMascotaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.MascotaResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.paseape.apipaseape.infrastructure.constant.Constant.ESTADO_LOGICO_ACTIVO;

@Service
@RequiredArgsConstructor
public class MascotaService {

    private final IMascotaRepository mascotaRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IClienteRepository clienteRepository;
    private final IMascotaDtoMapper mascotaDtoMapper;

    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorCorreo(String correo) {
        if (!StringUtils.hasText(correo)) {
            return null;
        }
        return usuarioRepository.findByCorreo(correo.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public Cliente obtenerClientePorUsuarioId(Long userId) {
        return clienteRepository.findByUsuarioId(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Mascota registrarMascota(Cliente cliente, MascotaReqDto dto) throws BadRequestException {
        Mascota mascota = Mascota.builder()
                .uuid(UUID.randomUUID().toString())
                .cliente(cliente)
                .nombre(dto.getNombre().trim())
                .tipoMascota(new TipoMascota(dto.getTipoMascotaId()))
                .tipoRaza(new TipoRaza(dto.getTipoRazaId()))
                .tipoGeneroMascota(new TipoGeneroMascota(dto.getTipoGeneroMascotaId()))
                .tipoTamanoMascota(new TipoTamanoMascota(dto.getTipoTamanoMascotaId()))
                .tipoNivelEnergia(new TipoNivelEnergia(dto.getTipoNivelEnergiaId()))
                .edadAnos(dto.getEdadAnos() != null ? dto.getEdadAnos() : 0)
                .edadMeses(dto.getEdadMeses() != null ? dto.getEdadMeses() : 0)
                .pesoKg(dto.getPesoKg())
                .esterilizado(dto.getEsterilizado() != null ? dto.getEsterilizado() : 0)
                .sociableConPerros(dto.getSociableConPerros() != null ? dto.getSociableConPerros() : 1)
                .sociableConPersonas(dto.getSociableConPersonas() != null ? dto.getSociableConPersonas() : 1)
                .precaucionesMedicas(dto.getPrecaucionesMedicas())
                .fotoUrl(dto.getFotoUrl())
                .estado(ESTADO_LOGICO_ACTIVO)
                .build();

        return mascotaRepository.save(mascota);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Mascota> registrarMascotas(Cliente cliente, List<MascotaReqDto> dtos) throws BadRequestException {
        List<Mascota> guardadas = new ArrayList<>();
        if (dtos != null && !dtos.isEmpty()) {
            for (MascotaReqDto dto : dtos) {
                if (dto != null && dto.getNombre() != null && !dto.getNombre().trim().isEmpty()) {
                    guardadas.add(registrarMascota(cliente, dto));
                }
            }
        }
        return guardadas;
    }

    @Transactional(rollbackFor = Exception.class)
    public MascotaResDto registrarMascotaUnitaria(RegistrarMascotaReqDto reqDto) throws BadRequestException {
        Usuario usuario = usuarioRepository.findByUuid(reqDto.getUsuarioUuid().trim());
        if (usuario == null) {
            throw new BadRequestException("No existe un usuario con el UUID: " + reqDto.getUsuarioUuid());
        }

        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId());
        if (cliente == null) {
            throw new BadRequestException("El usuario especificado no posee un perfil de cliente registrado.");
        }

        Mascota mascotaGuardada = registrarMascota(cliente, reqDto.getMascota());
        return mascotaDtoMapper.toResDto(mascotaGuardada);
    }

    @Transactional(rollbackFor = Exception.class)
    public MascotaResDto actualizarMascota(String mascotaUuid, Long clienteId, ActualizarMascotaReqDto reqDto) throws BadRequestException {
        Mascota mascota = mascotaRepository.findByUuid(mascotaUuid);
        if (mascota == null) {
            throw new BadRequestException("La mascota especificada no existe.");
        }

        // Verificar ownership: la mascota debe pertenecer al cliente autenticado
        if (mascota.getCliente() == null || mascota.getCliente().getId() == null || !mascota.getCliente().getId().equals(clienteId)) {
            throw new BadRequestException("No tiene autorización para modificar esta mascota.");
        }

        // Patch semantics: solo actualizar campos enviados (no nulos)
        if (StringUtils.hasText(reqDto.getNombre())) {
            mascota.setNombre(reqDto.getNombre().trim());
        }
        if (reqDto.getTipoMascotaId() != null) {
            mascota.setTipoMascota(new TipoMascota(reqDto.getTipoMascotaId()));
        }
        if (reqDto.getTipoRazaId() != null) {
            mascota.setTipoRaza(new TipoRaza(reqDto.getTipoRazaId()));
        }
        if (reqDto.getTipoGeneroMascotaId() != null) {
            mascota.setTipoGeneroMascota(new TipoGeneroMascota(reqDto.getTipoGeneroMascotaId()));
        }
        if (reqDto.getTipoTamanoMascotaId() != null) {
            mascota.setTipoTamanoMascota(new TipoTamanoMascota(reqDto.getTipoTamanoMascotaId()));
        }
        if (reqDto.getTipoNivelEnergiaId() != null) {
            mascota.setTipoNivelEnergia(new TipoNivelEnergia(reqDto.getTipoNivelEnergiaId()));
        }
        if (reqDto.getEdadAnos() != null) {
            mascota.setEdadAnos(reqDto.getEdadAnos());
        }
        if (reqDto.getEdadMeses() != null) {
            mascota.setEdadMeses(reqDto.getEdadMeses());
        }
        if (reqDto.getPesoKg() != null) {
            mascota.setPesoKg(reqDto.getPesoKg());
        }
        if (reqDto.getEsterilizado() != null) {
            mascota.setEsterilizado(reqDto.getEsterilizado());
        }
        if (reqDto.getSociableConPerros() != null) {
            mascota.setSociableConPerros(reqDto.getSociableConPerros());
        }
        if (reqDto.getSociableConPersonas() != null) {
            mascota.setSociableConPersonas(reqDto.getSociableConPersonas());
        }
        if (reqDto.getPrecaucionesMedicas() != null) {
            mascota.setPrecaucionesMedicas(reqDto.getPrecaucionesMedicas().trim());
        }
        if (reqDto.getFotoUrl() != null) {
            mascota.setFotoUrl(reqDto.getFotoUrl().trim());
        }

        Mascota mascotaActualizada = mascotaRepository.save(mascota);
        return mascotaDtoMapper.toResDto(mascotaActualizada);
    }
}
