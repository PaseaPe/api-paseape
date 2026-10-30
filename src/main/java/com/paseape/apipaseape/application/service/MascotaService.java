package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.*;
import com.paseape.apipaseape.domain.entity.*;
import com.paseape.apipaseape.infrastructure.dto.response.DigitalDocumentResDto;
import com.paseape.apipaseape.infrastructure.dto.response.DocumentUrlResDto;
import com.paseape.apipaseape.infrastructure.dto.response.FileDownloadResDto;
import com.paseape.apipaseape.infrastructure.mapper.dto.IMascotaDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.paseape.apipaseape.infrastructure.dto.request.ActualizarMascotaReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.MascotaReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.RegistrarMascotaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.MascotaResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.paseape.apipaseape.infrastructure.constant.Constant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MascotaService {

    private final IMascotaRepository mascotaRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IClienteRepository clienteRepository;
    private final IMascotaDtoMapper mascotaDtoMapper;

    private final IFolderDigitalDocumentRepository folderRepository;
    private final IDigitalDocumentRepository digitalDocumentRepository;
    private final IStorageService storageService;
    private final DigitalDocumentService digitalDocumentService;

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

        FolderDigitalDocument folder = FolderDigitalDocument.builder()
                .uuid(UUID.randomUUID().toString())
                .nombre("FOLDER_MASCOTA_" + dto.getNombre().trim().toUpperCase())
                .estado(ESTADO_LOGICO_ACTIVO)
                .build();
        FolderDigitalDocument folderGuardado = folderRepository.save(folder);

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
                .folderDigitalDocument(folderGuardado)
                .estado(ESTADO_LOGICO_ACTIVO)
                .build();

        Mascota mascotaSaved = mascotaRepository.save(mascota);
        if (dto.getFotoMascota() != null) {
            digitalDocumentService.subirFotoMascota(mascotaSaved.getUuid(), mascotaSaved.getCliente().getId(), dto.getFotoMascota());
        }
        return mascotaRepository.findById(mascotaSaved.getId());
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
        return poblarRespuestaMascota(mascotaGuardada);
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

        Mascota mascotaActualizada = mascotaRepository.save(mascota);
        return poblarRespuestaMascota(mascotaActualizada);
    }

    @Transactional(readOnly = true)
    public List<MascotaResDto> listarMascotasPorUsuario(String uuid) throws BadRequestException {
        Usuario usuario = usuarioRepository.findByUuid(uuid);
        if (usuario == null){
            throw new BadRequestException("Usuario no encontrado");
        }
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId());
        if (cliente == null){
            throw new BadRequestException("Cliente no encontrado");
        }

        List<Mascota> mascotas = mascotaRepository.findAllByClienteIdAndEstado(cliente.getId(),1);
        return mascotaDtoMapper.toResDtoList(mascotas);
    }

    @Transactional(readOnly = true)
    public MascotaResDto obtenerMascotaPorUuid(String uuid) throws BadRequestException {
        Mascota mascota = mascotaRepository.findByUuid(uuid);
        if (mascota == null){
            throw new BadRequestException("Mascota no encontrada");
        }
        return poblarRespuestaMascota(mascota);
    }

    private MascotaResDto poblarRespuestaMascota(Mascota mascota) throws BadRequestException {
        MascotaResDto dto = mascotaDtoMapper.toResDto(mascota);
        if (mascota.getFolderDigitalDocument() != null && mascota.getFolderDigitalDocument().getId() != null) {
            DigitalDocument fotoMascota = digitalDocumentRepository.findByFolderIdAndDigitalDocumentTypeId(mascota.getFolderDigitalDocument().getId(), ID_DOC_FOTO_MASCOTA);
            if (fotoMascota != null) {
                dto.setFotoMascota(digitalDocumentService.descargarDocumento(fotoMascota.getUuid()));
            }
        }
        return dto;
    }

}
