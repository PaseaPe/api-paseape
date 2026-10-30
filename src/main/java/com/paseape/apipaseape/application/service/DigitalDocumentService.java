package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.IDigitalDocumentRepository;
import com.paseape.apipaseape.application.repository.IFolderDigitalDocumentRepository;
import com.paseape.apipaseape.application.repository.IMascotaRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.domain.entity.*;
import com.paseape.apipaseape.infrastructure.dto.response.DigitalDocumentResDto;
import com.paseape.apipaseape.infrastructure.dto.response.DocumentUrlResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.IDigitalDocumentDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.paseape.apipaseape.infrastructure.constant.Constant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DigitalDocumentService {

    private static final String RUTA_USUARIO = "usuario/";
    private static final String RUTA_MASCOTA = "mascota/";
    private static final String SUBRUTA_ANTECEDENTES = "/antecedentes";

    private static final String MSG_USUARIO_NO_EXISTE = "El usuario especificado no existe.";
    private static final String MSG_MASCOTA_NO_EXISTE = "La mascota especificada no existe.";
    private static final String MSG_SIN_PERMISOS_MASCOTA = "No tiene permisos para subir archivos a esta mascota.";
    private static final String MSG_DOCUMENTO_NO_EXISTE = "El documento especificado no existe o ya ha sido eliminado.";

    private final IDigitalDocumentRepository digitalDocumentRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IMascotaRepository mascotaRepository;
    private final IFolderDigitalDocumentRepository folderDigitalDocumentRepository;
    private final IStorageService storageService;

    private final IDigitalDocumentDtoMapper digitalDocumentDtoMapper;

    @Transactional(rollbackFor = Exception.class)
    public DocumentUrlResDto subirFotoUsuario(String usuarioUuid, MultipartFile file) throws BadRequestException {
        Usuario usuario = buscarUsuario(usuarioUuid);
        String carpeta = RUTA_USUARIO + usuario.getUuid();

        return reemplazarDocumento(
                obtenerOCrearFolderUsuario(usuario),
                ID_DOC_FOTO_PERFIL,
                ArchivoMetadata.de(file),
                () -> storageService.uploadFile(carpeta, file));
    }

    @Transactional(rollbackFor = Exception.class)
    public DocumentUrlResDto subirFotoUsuarioBytes(String usuarioUuid, byte[] content, String originalFilename, String contentType) throws BadRequestException {
        Usuario usuario = buscarUsuario(usuarioUuid);
        String carpeta = RUTA_USUARIO + usuario.getUuid();

        return reemplazarDocumento(
                obtenerOCrearFolderUsuario(usuario),
                ID_DOC_FOTO_PERFIL,
                new ArchivoMetadata(originalFilename, contentType, content.length),
                () -> storageService.uploadFile(carpeta, content, originalFilename, contentType));
    }

    @Transactional(rollbackFor = Exception.class)
    public DocumentUrlResDto subirFotoMascota(String mascotaUuid, Long clienteId, MultipartFile file) throws BadRequestException {
        Mascota mascota = buscarMascotaDelCliente(mascotaUuid, clienteId);
        String carpeta = RUTA_MASCOTA + mascota.getUuid();

        return reemplazarDocumento(
                obtenerOCrearFolderMascota(mascota),
                ID_DOC_FOTO_MASCOTA,
                ArchivoMetadata.de(file),
                () -> storageService.uploadFile(carpeta, file));
    }

    @Transactional(rollbackFor = Exception.class)
    public List<DigitalDocumentResDto> subirAntecedentesPaseador(String usuarioUuid, List<MultipartFile> files) throws BadRequestException {
        Usuario usuario = buscarUsuario(usuarioUuid);
        FolderDigitalDocument folder = obtenerOCrearFolderUsuario(usuario);
        String carpeta = RUTA_USUARIO + usuario.getUuid() + SUBRUTA_ANTECEDENTES;

        List<DigitalDocumentResDto> respuestas = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String objectName = storageService.uploadFile(carpeta, file);
            // Si la transacción falla, el archivo recién subido no debe quedar huérfano.
            registrarLimpiezaStorage(null, objectName);

            DigitalDocument guardado = digitalDocumentRepository.save(
                    construirDocumento(folder, ID_DOC_ANTECEDENTE_POLICIAL, objectName, ArchivoMetadata.de(file)));
            respuestas.add(digitalDocumentDtoMapper.toResDto(guardado));
        }

        return respuestas;
    }

    @Transactional(readOnly = true)
    public DocumentUrlResDto descargarDocumento(String documentUuid) throws BadRequestException {
        DigitalDocument document = digitalDocumentRepository.findByUuid(documentUuid);
        if (document == null) {
            throw new BadRequestException("El documento solicitado no existe.");
        }
        return construirDocumentUrlResDto(document);
    }

    @Transactional(rollbackFor = Exception.class)
    public void eliminarDocumento(String documentUuid) throws BadRequestException {
        DigitalDocument document = digitalDocumentRepository.findByUuid(documentUuid);
        if (document == null) {
            throw new BadRequestException(MSG_DOCUMENTO_NO_EXISTE);
        }

        document.setEstado(ESTADO_LOGICO_INACTIVO);
        digitalDocumentRepository.save(document);
    }

    @Transactional(readOnly = true)
    public String urlFotoPerfil(Usuario usuario) throws BadRequestException {
        FolderDigitalDocument folder = usuario.getFolderDigitalDocument();
        if (folder == null) {
            return null;
        }

        DigitalDocument foto = digitalDocumentRepository.findByFolderIdAndDigitalDocumentTypeId(folder.getId(), ID_DOC_FOTO_PERFIL);
        if (foto == null) {
            return null;
        }

        return construirDocumentUrlResDto(foto).getUrl();
    }

    @Transactional(readOnly = true)
    public List<DigitalDocument> documentList(Long folderId) {
        return digitalDocumentRepository.findAllByFolderId(folderId);
    }

    // ------------------------------------------------------------------
    // Búsquedas y validaciones
    // ------------------------------------------------------------------

    private Usuario buscarUsuario(String usuarioUuid) throws BadRequestException {
        Usuario usuario = usuarioRepository.findByUuid(usuarioUuid);
        if (usuario == null) {
            throw new BadRequestException(MSG_USUARIO_NO_EXISTE);
        }
        return usuario;
    }

    private Mascota buscarMascotaDelCliente(String mascotaUuid, Long clienteId) throws BadRequestException {
        Mascota mascota = mascotaRepository.findByUuid(mascotaUuid);
        if (mascota == null) {
            throw new BadRequestException(MSG_MASCOTA_NO_EXISTE);
        }
        if (mascota.getCliente() == null || !mascota.getCliente().getId().equals(clienteId)) {
            throw new BadRequestException(MSG_SIN_PERMISOS_MASCOTA);
        }
        return mascota;
    }

    // ------------------------------------------------------------------
    // Folders
    // ------------------------------------------------------------------

    private FolderDigitalDocument obtenerOCrearFolderUsuario(Usuario usuario) {
        FolderDigitalDocument folder = usuario.getFolderDigitalDocument();
        if (folder == null) {
            folder = crearFolder("FOLDER_USUARIO_" + usuario.getNombres().toUpperCase());
            usuario.setFolderDigitalDocument(folder);
            usuarioRepository.save(usuario);
        }
        return folder;
    }

    private FolderDigitalDocument obtenerOCrearFolderMascota(Mascota mascota) {
        FolderDigitalDocument folder = mascota.getFolderDigitalDocument();
        if (folder == null) {
            folder = crearFolder("FOLDER_MASCOTA_" + mascota.getNombre().toUpperCase());
            mascota.setFolderDigitalDocument(folder);
            mascotaRepository.save(mascota);
        }
        return folder;
    }

    private FolderDigitalDocument crearFolder(String nombre) {
        return folderDigitalDocumentRepository.save(
                FolderDigitalDocument.builder()
                        .uuid(UUID.randomUUID().toString())
                        .nombre(nombre)
                        .estado(ESTADO_LOGICO_ACTIVO)
                        .build());
    }

    // ------------------------------------------------------------------
    // Persistencia de documentos
    // ------------------------------------------------------------------

    /**
     * Sube un archivo y lo asocia como el único documento de ese tipo dentro del folder
     * (crea el registro o actualiza el existente, y descarta el archivo anterior del storage).
     */
    private DocumentUrlResDto reemplazarDocumento(FolderDigitalDocument folder,
                                                  Integer docTypeId,
                                                  ArchivoMetadata archivo,
                                                  StorageUpload upload) throws BadRequestException {
        DigitalDocument existente = digitalDocumentRepository.findByFolderIdAndDigitalDocumentTypeId(folder.getId(), docTypeId);
        String previousObjectName = (existente != null) ? existente.getObjectName() : null;

        String newObjectName = upload.execute();
        registrarLimpiezaStorage(previousObjectName, newObjectName);

        DigitalDocument guardado = digitalDocumentRepository.save(
                (existente != null)
                        ? actualizarDocumento(existente, newObjectName, archivo)
                        : construirDocumento(folder, docTypeId, newObjectName, archivo));

        return construirDocumentUrlResDto(guardado);
    }

    private DigitalDocument actualizarDocumento(DigitalDocument documento, String objectName, ArchivoMetadata archivo) {
        documento.setObjectName(objectName);
        documento.setNombreOriginal(archivo.getNombre());
        documento.setContentType(archivo.getContentType());
        documento.setTamanoBytes(archivo.getTamanoBytes());
        documento.setEstado(ESTADO_LOGICO_ACTIVO);
        return documento;
    }

    private DigitalDocument construirDocumento(FolderDigitalDocument folder, Integer docTypeId, String objectName, ArchivoMetadata archivo) {
        return DigitalDocument.builder()
                .uuid(UUID.randomUUID().toString())
                .folderDigitalDocument(folder)
                .digitalDocumentType(new DigitalDocumentType(docTypeId))
                .objectName(objectName)
                .nombreOriginal(archivo.getNombre())
                .contentType(archivo.getContentType())
                .tamanoBytes(archivo.getTamanoBytes())
                .estado(ESTADO_LOGICO_ACTIVO)
                .build();
    }

    private DocumentUrlResDto construirDocumentUrlResDto(DigitalDocument document) throws BadRequestException {
        String preSignedUrl = storageService.generatePreSignedUrl(document.getObjectName(), STORAGE_PAR_EXPIRATION_MINUTES);

        return DocumentUrlResDto.builder()
                .url(preSignedUrl)
                .nombreOriginal(document.getNombreOriginal())
                .contentType(document.getContentType())
                .tamanoBytes(document.getTamanoBytes())
                .expirationMinutes(STORAGE_PAR_EXPIRATION_MINUTES)
                .build();
    }

    // ------------------------------------------------------------------
    // Limpieza del storage (sincronizada con la transacción)
    // ------------------------------------------------------------------

    /**
     * Mantiene consistentes BD y storage:
     * - COMMIT   -> elimina {@code objetoSiCommit} (p. ej. la versión anterior del archivo).
     * - ROLLBACK -> elimina {@code objetoSiRollback} (p. ej. el archivo recién subido).
     */
    private void registrarLimpiezaStorage(String objetoSiCommit, String objetoSiRollback) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            if (!StringUtils.hasText(objetoSiCommit) || !objetoSiCommit.equals(objetoSiRollback)) {
                eliminarArchivoSilencioso(objetoSiCommit);
            }
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    if (objetoSiCommit != null && !objetoSiCommit.equals(objetoSiRollback)) {
                        eliminarArchivoSilencioso(objetoSiCommit);
                    }
                } else {
                    eliminarArchivoSilencioso(objetoSiRollback);
                }
            }
        });
    }

    private void eliminarArchivoSilencioso(String objectName) {
        if (!StringUtils.hasText(objectName)) {
            return;
        }
        try {
            storageService.deleteFile(objectName);
        } catch (Exception ex) {
            log.warn("[OCI-STORAGE] No se pudo eliminar el archivo {}: {}", objectName, ex.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Tipos auxiliares
    // ------------------------------------------------------------------

    @FunctionalInterface
    private interface StorageUpload {
        String execute() throws BadRequestException;
    }

    @Value
    private static class ArchivoMetadata {
        String nombre;
        String contentType;
        long tamanoBytes;

        static ArchivoMetadata de(MultipartFile file) {
            return new ArchivoMetadata(file.getOriginalFilename(), file.getContentType(), file.getSize());
        }
    }
}