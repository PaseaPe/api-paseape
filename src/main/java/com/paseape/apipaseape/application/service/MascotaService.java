package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.infrastructure.mapper.dto.IMascotaDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IMascotaRepository;
import com.paseape.apipaseape.application.repository.ITipoGeneroMascotaRepository;
import com.paseape.apipaseape.application.repository.ITipoMascotaRepository;
import com.paseape.apipaseape.application.repository.ITipoNivelEnergiaRepository;
import com.paseape.apipaseape.application.repository.ITipoRazaRepository;
import com.paseape.apipaseape.application.repository.ITipoTamanoMascotaRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.Mascota;
import com.paseape.apipaseape.domain.entity.TipoGeneroMascota;
import com.paseape.apipaseape.domain.entity.TipoMascota;
import com.paseape.apipaseape.domain.entity.TipoNivelEnergia;
import com.paseape.apipaseape.domain.entity.TipoRaza;
import com.paseape.apipaseape.domain.entity.TipoTamanoMascota;
import com.paseape.apipaseape.domain.entity.Usuario;
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
    private final ITipoMascotaRepository tipoMascotaRepository;
    private final ITipoRazaRepository tipoRazaRepository;
    private final ITipoGeneroMascotaRepository tipoGeneroMascotaRepository;
    private final ITipoTamanoMascotaRepository tipoTamanoMascotaRepository;
    private final ITipoNivelEnergiaRepository tipoNivelEnergiaRepository;
    private final IMascotaDtoMapper mascotaDtoMapper;

    @Transactional
    public Mascota registrarMascota(Cliente cliente, MascotaReqDto dto) throws BadRequestException {
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
            throw new BadRequestException("El género con ID " + dto.getTipoGeneroMascotaId() + " no existe.");
        }

        TipoTamanoMascota tipoTamano = tipoTamanoMascotaRepository.findById(dto.getTipoTamanoMascotaId());
        if (tipoTamano == null) {
            throw new BadRequestException("El tamaño con ID " + dto.getTipoTamanoMascotaId() + " no existe.");
        }

        TipoNivelEnergia tipoEnergia = tipoNivelEnergiaRepository.findById(dto.getTipoNivelEnergiaId());
        if (tipoEnergia == null) {
            throw new BadRequestException("El nivel de energía con ID " + dto.getTipoNivelEnergiaId() + " no existe.");
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
                .estado(ESTADO_LOGICO_ACTIVO)
                .build();

        return mascotaRepository.save(mascota);
    }

    @Transactional
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

    @Transactional
    public MascotaResDto registrarMascotaUnitaria(RegistrarMascotaReqDto reqDto) throws BadRequestException {
        Usuario usuario = usuarioRepository.findByUuid(reqDto.getUsuarioUuid().trim());
        if (usuario == null) {
            throw new BadRequestException("No existe un usuario con el UUID: " + reqDto.getUsuarioUuid());
        }

        Cliente cliente = clienteRepository.findById(usuario.getId());
        if (cliente == null) {
            throw new BadRequestException("El usuario especificado no posee un perfil de cliente registrado.");
        }

        Mascota mascotaGuardada = registrarMascota(cliente, reqDto.getMascota());
        return mascotaDtoMapper.toResDto(mascotaGuardada);
    }
}
