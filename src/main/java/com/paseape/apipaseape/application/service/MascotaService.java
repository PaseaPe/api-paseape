package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IDigitalDocumentRepository;
import com.paseape.apipaseape.application.repository.IFolderDigitalDocumentRepository;
import com.paseape.apipaseape.application.repository.IMascotaRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.DigitalDocument;
import com.paseape.apipaseape.domain.entity.DigitalDocumentType;
import com.paseape.apipaseape.domain.entity.FolderDigitalDocument;
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
import com.paseape.apipaseape.infrastructure.dto.response.DigitalDocumentResDto;
import com.paseape.apipaseape.infrastructure.dto.response.FileDownloadResDto;
import com.paseape.apipaseape.infrastructure.dto.response.MascotaResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.IMascotaDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.paseape.apipaseape.infrastructure.constant.Constant.ESTADO_LOGICO_ACTIVO;
import static com.paseape.apipaseape.infrastructure.constant.Constant.ID_DOC_FOTO_MASCOTA;

@Service
@RequiredArgsConstructor
public class MascotaService {

    private final IMascotaRepository mascotaRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IClienteRepository clienteRepository;
    private final IFolderDigitalDocumentRepository folderRepository;
    private final IDigitalDocumentRepository digitalDocumentRepository;
    private final IStorageService storageService;
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
        return clienteRepository.findById(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Mascota registrarMascota(Cliente cliente, MascotaReqDto dto) throws BadRequestException {
        String mascotaUuid = UUID.randomUUID().toString();

        FolderDigitalDocument folder = FolderDigitalDocument.builder()
                .uuid(UUID.randomUUID().toString())
                .nombre("FOLDER_MASCOTA_" + dto.getNombre().trim().toUpperCase())
                .estado(ESTADO_LOGICO_ACTIVO)
                .build();
        FolderDigitalDocument folderGuardado = folderRepository.save(folder);

        Mascota mascota = Mascota.builder()
                .uuid(mascotaUuid)
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

        Cliente cliente = clienteRepository.findById(usuario.getId());
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

        if (mascota.getCliente() == null || mascota.getCliente().getId() == null || !mascota.getCliente().getId().equals(clienteId)) {
            throw new BadRequestException("No tiene autorización para modificar esta mascota.");
        }

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
        if (usuario == null) {
            throw new BadRequestException("Usuario no encontrado");
        }
        Cliente cliente = clienteRepository.findById(usuario.getId());
        if (cliente == null) {
            throw new BadRequestException("Cliente no encontrado");
        }

        List<Mascota> mascotas = mascotaRepository.findAllByClienteIdAndEstado(cliente.getId(), ESTADO_LOGICO_ACTIVO);
        List<MascotaResDto> listaDto = new ArrayList<>();
        for (Mascota m : mascotas) {
            listaDto.add(poblarRespuestaMascota(m));
        }
        return listaDto;
    }

    @Transactional(readOnly = true)
    public MascotaResDto obtenerMascotaPorUuid(String uuid) throws BadRequestException {
        Mascota mascota = mascotaRepository.findByUuid(uuid);
        if (mascota == null) {
            throw new BadRequestException("Mascota no encontrada");
        }
        return poblarRespuestaMascota(mascota);
    }

    @Transactional(rollbackFor = Exception.class)
    public DigitalDocumentResDto subirFotoMascota(String mascotaUuid, Long clienteId, MultipartFile file) throws BadRequestException {
        Mascota mascota = mascotaRepository.findByUuid(mascotaUuid);
        if (mascota == null) {
            throw new BadRequestException("La mascota especificada no existe.");
        }

        if (mascota.getCliente() == null || !mascota.getCliente().getId().equals(clienteId)) {
            throw new BadRequestException("No tiene permisos para subir archivos a esta mascota.");
        }

        FolderDigitalDocument folder = mascota.getFolderDigitalDocument();
        if (folder == null) {
            folder = FolderDigitalDocument.builder()
                    .uuid(UUID.randomUUID().toString())
                    .nombre("FOLDER_MASCOTA_" + mascota.getNombre().toUpperCase())
                    .estado(ESTADO_LOGICO_ACTIVO)
                    .build();
            folder = folderRepository.save(folder);
            mascota.setFolderDigitalDocument(folder);
            mascotaRepository.save(mascota);
        }

        String targetFolder = "mascotas/" + mascota.getUuid();
        String objectName = storageService.uploadFile(targetFolder, file);

        DigitalDocument document = DigitalDocument.builder()
                .uuid(UUID.randomUUID().toString())
                .folderDigitalDocument(folder)
                .digitalDocumentType(new DigitalDocumentType(ID_DOC_FOTO_MASCOTA))
                .objectName(objectName)
                .nombreOriginal(file.getOriginalFilename())
                .contentType(file.getContentType())
                .tamanoBytes(file.getSize())
                .estado(ESTADO_LOGICO_ACTIVO)
                .build();

        DigitalDocument guardado = digitalDocumentRepository.save(document);
        return mascotaDtoMapper.toDocumentDto(guardado);
    }

    @Transactional(readOnly = true)
    public FileDownloadResDto descargarFotoMascota(String documentUuid) throws BadRequestException {
        DigitalDocument document = digitalDocumentRepository.findByUuid(documentUuid);
        if (document == null) {
            throw new BadRequestException("El documento solicitado no existe.");
        }
        return storageService.downloadFile(document.getObjectName());
    }

    private MascotaResDto poblarRespuestaMascota(Mascota mascota) {
        MascotaResDto dto = mascotaDtoMapper.toResDto(mascota);
        if (mascota.getFolderDigitalDocument() != null && mascota.getFolderDigitalDocument().getId() != null) {
            List<DigitalDocument> docs = digitalDocumentRepository.findAllByFolderId(mascota.getFolderDigitalDocument().getId());
            dto.setDocumentos(mascotaDtoMapper.toDocumentDtoList(docs));
        } else {
            dto.setDocumentos(List.of());
        }
        return dto;
    }
}