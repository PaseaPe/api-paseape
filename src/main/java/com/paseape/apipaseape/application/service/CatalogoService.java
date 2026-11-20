package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.IDistritoLimaRepository;
import com.paseape.apipaseape.application.repository.ITipoGeneroMascotaRepository;
import com.paseape.apipaseape.application.repository.ITipoMascotaRepository;
import com.paseape.apipaseape.application.repository.ITipoNivelEnergiaRepository;
import com.paseape.apipaseape.application.repository.ITipoRazaRepository;
import com.paseape.apipaseape.application.repository.ITipoTamanoMascotaRepository;
import com.paseape.apipaseape.domain.entity.DistritoLima;
import com.paseape.apipaseape.domain.entity.TipoGeneroMascota;
import com.paseape.apipaseape.domain.entity.TipoMascota;
import com.paseape.apipaseape.domain.entity.TipoNivelEnergia;
import com.paseape.apipaseape.domain.entity.TipoRaza;
import com.paseape.apipaseape.domain.entity.TipoTamanoMascota;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.dto.request.catalogo.CatalogoBaseReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.catalogo.DistritoLimaReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.catalogo.TipoRazaReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.catalogo.TipoTamanoMascotaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.catalogo.CatalogoAdminResDto;
import com.paseape.apipaseape.infrastructure.dto.response.catalogo.DistritoLimaAdminResDto;
import com.paseape.apipaseape.infrastructure.dto.response.catalogo.TipoRazaAdminResDto;
import com.paseape.apipaseape.infrastructure.dto.response.catalogo.TipoTamanoMascotaAdminResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.ICatalogoDtoMapper;
import com.paseape.apipaseape.infrastructure.util.UtilMethods;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final IDistritoLimaRepository distritoLimaRepository;
    private final ITipoMascotaRepository tipoMascotaRepository;
    private final ITipoRazaRepository tipoRazaRepository;
    private final ITipoTamanoMascotaRepository tipoTamanoMascotaRepository;
    private final ITipoNivelEnergiaRepository tipoNivelEnergiaRepository;
    private final ITipoGeneroMascotaRepository tipoGeneroMascotaRepository;
    private final ICatalogoDtoMapper catalogoDtoMapper;

    // =========================================================================
    // 1. DISTRITOS LIMA
    // =========================================================================

    @Transactional(readOnly = true)
    public List<DistritoLimaAdminResDto> listarDistritos(Integer estado) {
        List<DistritoLima> items = estado != null
                ? distritoLimaRepository.findAllByEstado(estado)
                : distritoLimaRepository.findAll();
        return convertirFechasDistritos(catalogoDtoMapper.toDistritoResDtoList(items));
    }

    @Transactional(rollbackFor = Exception.class)
    public DistritoLimaAdminResDto crearDistrito(DistritoLimaReqDto reqDto) throws BadRequestException {
        String descripcion = reqDto.getDescripcion().trim();
        if (distritoLimaRepository.findByDescripcion(descripcion) != null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "Ya existe un distrito con la descripción '" + descripcion + "'.");
        }
        String ubigeo = StringUtils.hasText(reqDto.getCodigoUbigeo()) ? reqDto.getCodigoUbigeo().trim() : null;
        if (ubigeo != null && distritoLimaRepository.findByCodigoUbigeo(ubigeo) != null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "Ya existe un distrito con el código ubigeo '" + ubigeo + "'.");
        }

        DistritoLima nuevo = DistritoLima.builder()
                .uuid(UUID.randomUUID().toString())
                .descripcion(descripcion)
                .codigoUbigeo(ubigeo)
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();

        return convertirFechasDistrito(catalogoDtoMapper.toDistritoResDto(distritoLimaRepository.save(nuevo)));
    }

    @Transactional(rollbackFor = Exception.class)
    public DistritoLimaAdminResDto actualizarDistrito(String uuid, DistritoLimaReqDto reqDto) throws BadRequestException {
        DistritoLima existente = distritoLimaRepository.findByUuid(uuid.trim());
        if (existente == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el distrito especificado.");
        }

        String descripcion = reqDto.getDescripcion().trim();
        DistritoLima duplicadoDesc = distritoLimaRepository.findByDescripcion(descripcion);
        if (duplicadoDesc != null && !duplicadoDesc.getId().equals(existente.getId())) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "Ya existe otro distrito con la descripción '" + descripcion + "'.");
        }

        String ubigeo = StringUtils.hasText(reqDto.getCodigoUbigeo()) ? reqDto.getCodigoUbigeo().trim() : null;
        if (ubigeo != null) {
            DistritoLima duplicadoUbigeo = distritoLimaRepository.findByCodigoUbigeo(ubigeo);
            if (duplicadoUbigeo != null && !duplicadoUbigeo.getId().equals(existente.getId())) {
                throw new BadRequestException(MessageCodes.ResponseCodeBR03, "Ya existe otro distrito con el código ubigeo '" + ubigeo + "'.");
            }
        }

        existente.setDescripcion(descripcion);
        existente.setCodigoUbigeo(ubigeo);
        return convertirFechasDistrito(catalogoDtoMapper.toDistritoResDto(distritoLimaRepository.save(existente)));
    }

    @Transactional(rollbackFor = Exception.class)
    public DistritoLimaAdminResDto eliminarDistrito(String uuid) throws BadRequestException {
        return cambiarEstadoDistrito(uuid, Constant.ESTADO_LOGICO_INACTIVO);
    }

    @Transactional(rollbackFor = Exception.class)
    public DistritoLimaAdminResDto activarDistrito(String uuid) throws BadRequestException {
        return cambiarEstadoDistrito(uuid, Constant.ESTADO_LOGICO_ACTIVO);
    }

    private DistritoLimaAdminResDto cambiarEstadoDistrito(String uuid, int nuevoEstado) throws BadRequestException {
        DistritoLima existente = distritoLimaRepository.findByUuid(uuid.trim());
        if (existente == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el distrito especificado.");
        }
        if (Integer.valueOf(nuevoEstado).equals(existente.getEstado())) {
            String msg = nuevoEstado == Constant.ESTADO_LOGICO_INACTIVO
                    ? "El distrito ya se encuentra inactivo."
                    : "El distrito ya se encuentra activo.";
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, msg);
        }
        existente.setEstado(nuevoEstado);
        return convertirFechasDistrito(catalogoDtoMapper.toDistritoResDto(distritoLimaRepository.save(existente)));
    }

    // =========================================================================
    // 2. TIPOS DE MASCOTA
    // =========================================================================

    @Transactional(readOnly = true)
    public List<CatalogoAdminResDto> listarTiposMascota(Integer estado) {
        List<TipoMascota> items = estado != null
                ? tipoMascotaRepository.findAllByEstado(estado)
                : tipoMascotaRepository.findAll();
        return convertirFechasBase(catalogoDtoMapper.toTipoMascotaResDtoList(items));
    }

    @Transactional(rollbackFor = Exception.class)
    public CatalogoAdminResDto crearTipoMascota(CatalogoBaseReqDto reqDto) throws BadRequestException {
        String descripcion = reqDto.getDescripcion().trim();
        if (tipoMascotaRepository.findByDescripcion(descripcion) != null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "Ya existe un tipo de mascota con la descripción '" + descripcion + "'.");
        }

        TipoMascota nuevo = TipoMascota.builder()
                .uuid(UUID.randomUUID().toString())
                .descripcion(descripcion)
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();

        return convertirFechasBaseItem(catalogoDtoMapper.toTipoMascotaResDto(tipoMascotaRepository.save(nuevo)));
    }

    @Transactional(rollbackFor = Exception.class)
    public CatalogoAdminResDto actualizarTipoMascota(String uuid, CatalogoBaseReqDto reqDto) throws BadRequestException {
        TipoMascota existente = tipoMascotaRepository.findByUuid(uuid.trim());
        if (existente == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el tipo de mascota especificado.");
        }

        String descripcion = reqDto.getDescripcion().trim();
        TipoMascota duplicado = tipoMascotaRepository.findByDescripcion(descripcion);
        if (duplicado != null && !duplicado.getId().equals(existente.getId())) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "Ya existe otro tipo de mascota con la descripción '" + descripcion + "'.");
        }

        existente.setDescripcion(descripcion);
        return convertirFechasBaseItem(catalogoDtoMapper.toTipoMascotaResDto(tipoMascotaRepository.save(existente)));
    }

    @Transactional(rollbackFor = Exception.class)
    public CatalogoAdminResDto eliminarTipoMascota(String uuid) throws BadRequestException {
        return cambiarEstadoTipoMascota(uuid, Constant.ESTADO_LOGICO_INACTIVO);
    }

    @Transactional(rollbackFor = Exception.class)
    public CatalogoAdminResDto activarTipoMascota(String uuid) throws BadRequestException {
        return cambiarEstadoTipoMascota(uuid, Constant.ESTADO_LOGICO_ACTIVO);
    }

    private CatalogoAdminResDto cambiarEstadoTipoMascota(String uuid, int nuevoEstado) throws BadRequestException {
        TipoMascota existente = tipoMascotaRepository.findByUuid(uuid.trim());
        if (existente == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el tipo de mascota especificado.");
        }
        if (Integer.valueOf(nuevoEstado).equals(existente.getEstado())) {
            String msg = nuevoEstado == Constant.ESTADO_LOGICO_INACTIVO
                    ? "El tipo de mascota ya se encuentra inactivo."
                    : "El tipo de mascota ya se encuentra activo.";
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, msg);
        }
        existente.setEstado(nuevoEstado);
        return convertirFechasBaseItem(catalogoDtoMapper.toTipoMascotaResDto(tipoMascotaRepository.save(existente)));
    }

    // =========================================================================
    // 3. RAZAS
    // =========================================================================

    @Transactional(readOnly = true)
    public List<TipoRazaAdminResDto> listarRazas(Integer estado) {
        List<TipoRaza> items = estado != null
                ? tipoRazaRepository.findAllByEstado(estado)
                : tipoRazaRepository.findAll();
        return convertirFechasRazas(catalogoDtoMapper.toTipoRazaResDtoList(items));
    }

    @Transactional(rollbackFor = Exception.class)
    public TipoRazaAdminResDto crearRaza(TipoRazaReqDto reqDto) throws BadRequestException {
        TipoMascota tipoMascota = tipoMascotaRepository.findByUuid(reqDto.getTipoMascotaUuid().trim());
        if (tipoMascota == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "El tipo de mascota especificado no existe.");
        }

        String descripcion = reqDto.getDescripcion().trim();
        TipoRaza duplicada = tipoRazaRepository.findByDescripcion(descripcion);
        if (duplicada != null && duplicada.getTipoMascota() != null
                && tipoMascota.getId().equals(duplicada.getTipoMascota().getId())) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "Ya existe una raza '" + descripcion + "' para ese tipo de mascota.");
        }

        TipoRaza nuevo = TipoRaza.builder()
                .uuid(UUID.randomUUID().toString())
                .tipoMascota(tipoMascota)
                .descripcion(descripcion)
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();

        return convertirFechasRaza(catalogoDtoMapper.toTipoRazaResDto(tipoRazaRepository.save(nuevo)));
    }

    @Transactional(rollbackFor = Exception.class)
    public TipoRazaAdminResDto actualizarRaza(String uuid, TipoRazaReqDto reqDto) throws BadRequestException {
        TipoRaza existente = tipoRazaRepository.findByUuid(uuid.trim());
        if (existente == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró la raza especificada.");
        }

        TipoMascota tipoMascota = tipoMascotaRepository.findByUuid(reqDto.getTipoMascotaUuid().trim());
        if (tipoMascota == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "El tipo de mascota especificado no existe.");
        }

        String descripcion = reqDto.getDescripcion().trim();
        TipoRaza duplicada = tipoRazaRepository.findByDescripcion(descripcion);
        if (duplicada != null && !duplicada.getId().equals(existente.getId())
                && duplicada.getTipoMascota() != null
                && tipoMascota.getId().equals(duplicada.getTipoMascota().getId())) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "Ya existe otra raza '" + descripcion + "' para ese tipo de mascota.");
        }

        existente.setTipoMascota(tipoMascota);
        existente.setDescripcion(descripcion);
        return convertirFechasRaza(catalogoDtoMapper.toTipoRazaResDto(tipoRazaRepository.save(existente)));
    }

    @Transactional(rollbackFor = Exception.class)
    public TipoRazaAdminResDto eliminarRaza(String uuid) throws BadRequestException {
        return cambiarEstadoRaza(uuid, Constant.ESTADO_LOGICO_INACTIVO);
    }

    @Transactional(rollbackFor = Exception.class)
    public TipoRazaAdminResDto activarRaza(String uuid) throws BadRequestException {
        return cambiarEstadoRaza(uuid, Constant.ESTADO_LOGICO_ACTIVO);
    }

    private TipoRazaAdminResDto cambiarEstadoRaza(String uuid, int nuevoEstado) throws BadRequestException {
        TipoRaza existente = tipoRazaRepository.findByUuid(uuid.trim());
        if (existente == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró la raza especificada.");
        }
        if (Integer.valueOf(nuevoEstado).equals(existente.getEstado())) {
            String msg = nuevoEstado == Constant.ESTADO_LOGICO_INACTIVO
                    ? "La raza ya se encuentra inactiva."
                    : "La raza ya se encuentra activa.";
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, msg);
        }
        existente.setEstado(nuevoEstado);
        return convertirFechasRaza(catalogoDtoMapper.toTipoRazaResDto(tipoRazaRepository.save(existente)));
    }

    // =========================================================================
    // 4. TAMAÑOS DE MASCOTA
    // =========================================================================

    @Transactional(readOnly = true)
    public List<TipoTamanoMascotaAdminResDto> listarTamanosMascota(Integer estado) {
        List<TipoTamanoMascota> items = estado != null
                ? tipoTamanoMascotaRepository.findAllByEstado(estado)
                : tipoTamanoMascotaRepository.findAll();
        return convertirFechasTamanos(catalogoDtoMapper.toTipoTamanoResDtoList(items));
    }

    @Transactional(rollbackFor = Exception.class)
    public TipoTamanoMascotaAdminResDto crearTamanoMascota(TipoTamanoMascotaReqDto reqDto) throws BadRequestException {
        String descripcion = reqDto.getDescripcion().trim();
        if (tipoTamanoMascotaRepository.findByDescripcion(descripcion) != null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "Ya existe un tamaño de mascota con la descripción '" + descripcion + "'.");
        }

        String rangoPesoRef = StringUtils.hasText(reqDto.getRangoPesoRef()) ? reqDto.getRangoPesoRef().trim() : null;
        TipoTamanoMascota nuevo = TipoTamanoMascota.builder()
                .uuid(UUID.randomUUID().toString())
                .descripcion(descripcion)
                .rangoPesoRef(rangoPesoRef)
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();

        return convertirFechasTamano(catalogoDtoMapper.toTipoTamanoResDto(tipoTamanoMascotaRepository.save(nuevo)));
    }

    @Transactional(rollbackFor = Exception.class)
    public TipoTamanoMascotaAdminResDto actualizarTamanoMascota(String uuid, TipoTamanoMascotaReqDto reqDto) throws BadRequestException {
        TipoTamanoMascota existente = tipoTamanoMascotaRepository.findByUuid(uuid.trim());
        if (existente == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el tamaño de mascota especificado.");
        }

        String descripcion = reqDto.getDescripcion().trim();
        TipoTamanoMascota duplicado = tipoTamanoMascotaRepository.findByDescripcion(descripcion);
        if (duplicado != null && !duplicado.getId().equals(existente.getId())) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "Ya existe otro tamaño de mascota con la descripción '" + descripcion + "'.");
        }

        existente.setDescripcion(descripcion);
        existente.setRangoPesoRef(StringUtils.hasText(reqDto.getRangoPesoRef()) ? reqDto.getRangoPesoRef().trim() : null);
        return convertirFechasTamano(catalogoDtoMapper.toTipoTamanoResDto(tipoTamanoMascotaRepository.save(existente)));
    }

    @Transactional(rollbackFor = Exception.class)
    public TipoTamanoMascotaAdminResDto eliminarTamanoMascota(String uuid) throws BadRequestException {
        return cambiarEstadoTamanoMascota(uuid, Constant.ESTADO_LOGICO_INACTIVO);
    }

    @Transactional(rollbackFor = Exception.class)
    public TipoTamanoMascotaAdminResDto activarTamanoMascota(String uuid) throws BadRequestException {
        return cambiarEstadoTamanoMascota(uuid, Constant.ESTADO_LOGICO_ACTIVO);
    }

    private TipoTamanoMascotaAdminResDto cambiarEstadoTamanoMascota(String uuid, int nuevoEstado) throws BadRequestException {
        TipoTamanoMascota existente = tipoTamanoMascotaRepository.findByUuid(uuid.trim());
        if (existente == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el tamaño de mascota especificado.");
        }
        if (Integer.valueOf(nuevoEstado).equals(existente.getEstado())) {
            String msg = nuevoEstado == Constant.ESTADO_LOGICO_INACTIVO
                    ? "El tamaño de mascota ya se encuentra inactivo."
                    : "El tamaño de mascota ya se encuentra activo.";
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, msg);
        }
        existente.setEstado(nuevoEstado);
        return convertirFechasTamano(catalogoDtoMapper.toTipoTamanoResDto(tipoTamanoMascotaRepository.save(existente)));
    }

    // =========================================================================
    // 5. NIVELES DE ENERGÍA
    // =========================================================================

    @Transactional(readOnly = true)
    public List<CatalogoAdminResDto> listarNivelesEnergia(Integer estado) {
        List<TipoNivelEnergia> items = estado != null
                ? tipoNivelEnergiaRepository.findAllByEstado(estado)
                : tipoNivelEnergiaRepository.findAll();
        return convertirFechasBase(catalogoDtoMapper.toTipoNivelEnergiaResDtoList(items));
    }

    @Transactional(rollbackFor = Exception.class)
    public CatalogoAdminResDto crearNivelEnergia(CatalogoBaseReqDto reqDto) throws BadRequestException {
        String descripcion = reqDto.getDescripcion().trim();
        if (tipoNivelEnergiaRepository.findByDescripcion(descripcion) != null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "Ya existe un nivel de energía con la descripción '" + descripcion + "'.");
        }

        TipoNivelEnergia nuevo = TipoNivelEnergia.builder()
                .uuid(UUID.randomUUID().toString())
                .descripcion(descripcion)
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();

        return convertirFechasBaseItem(catalogoDtoMapper.toTipoNivelEnergiaResDto(tipoNivelEnergiaRepository.save(nuevo)));
    }

    @Transactional(rollbackFor = Exception.class)
    public CatalogoAdminResDto actualizarNivelEnergia(String uuid, CatalogoBaseReqDto reqDto) throws BadRequestException {
        TipoNivelEnergia existente = tipoNivelEnergiaRepository.findByUuid(uuid.trim());
        if (existente == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el nivel de energía especificado.");
        }

        String descripcion = reqDto.getDescripcion().trim();
        TipoNivelEnergia duplicado = tipoNivelEnergiaRepository.findByDescripcion(descripcion);
        if (duplicado != null && !duplicado.getId().equals(existente.getId())) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "Ya existe otro nivel de energía con la descripción '" + descripcion + "'.");
        }

        existente.setDescripcion(descripcion);
        return convertirFechasBaseItem(catalogoDtoMapper.toTipoNivelEnergiaResDto(tipoNivelEnergiaRepository.save(existente)));
    }

    @Transactional(rollbackFor = Exception.class)
    public CatalogoAdminResDto eliminarNivelEnergia(String uuid) throws BadRequestException {
        return cambiarEstadoNivelEnergia(uuid, Constant.ESTADO_LOGICO_INACTIVO);
    }

    @Transactional(rollbackFor = Exception.class)
    public CatalogoAdminResDto activarNivelEnergia(String uuid) throws BadRequestException {
        return cambiarEstadoNivelEnergia(uuid, Constant.ESTADO_LOGICO_ACTIVO);
    }

    private CatalogoAdminResDto cambiarEstadoNivelEnergia(String uuid, int nuevoEstado) throws BadRequestException {
        TipoNivelEnergia existente = tipoNivelEnergiaRepository.findByUuid(uuid.trim());
        if (existente == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el nivel de energía especificado.");
        }
        if (Integer.valueOf(nuevoEstado).equals(existente.getEstado())) {
            String msg = nuevoEstado == Constant.ESTADO_LOGICO_INACTIVO
                    ? "El nivel de energía ya se encuentra inactivo."
                    : "El nivel de energía ya se encuentra activo.";
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, msg);
        }
        existente.setEstado(nuevoEstado);
        return convertirFechasBaseItem(catalogoDtoMapper.toTipoNivelEnergiaResDto(tipoNivelEnergiaRepository.save(existente)));
    }

    // =========================================================================
    // 6. GÉNEROS DE MASCOTA
    // =========================================================================

    @Transactional(readOnly = true)
    public List<CatalogoAdminResDto> listarGenerosMascota(Integer estado) {
        List<TipoGeneroMascota> items = estado != null
                ? tipoGeneroMascotaRepository.findAllByEstado(estado)
                : tipoGeneroMascotaRepository.findAll();
        return convertirFechasBase(catalogoDtoMapper.toTipoGeneroResDtoList(items));
    }

    @Transactional(rollbackFor = Exception.class)
    public CatalogoAdminResDto crearGeneroMascota(CatalogoBaseReqDto reqDto) throws BadRequestException {
        String descripcion = reqDto.getDescripcion().trim();
        if (tipoGeneroMascotaRepository.findByDescripcion(descripcion) != null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "Ya existe un género de mascota con la descripción '" + descripcion + "'.");
        }

        TipoGeneroMascota nuevo = TipoGeneroMascota.builder()
                .uuid(UUID.randomUUID().toString())
                .descripcion(descripcion)
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();

        return convertirFechasBaseItem(catalogoDtoMapper.toTipoGeneroResDto(tipoGeneroMascotaRepository.save(nuevo)));
    }

    @Transactional(rollbackFor = Exception.class)
    public CatalogoAdminResDto actualizarGeneroMascota(String uuid, CatalogoBaseReqDto reqDto) throws BadRequestException {
        TipoGeneroMascota existente = tipoGeneroMascotaRepository.findByUuid(uuid.trim());
        if (existente == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el género de mascota especificado.");
        }

        String descripcion = reqDto.getDescripcion().trim();
        TipoGeneroMascota duplicado = tipoGeneroMascotaRepository.findByDescripcion(descripcion);
        if (duplicado != null && !duplicado.getId().equals(existente.getId())) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, "Ya existe otro género de mascota con la descripción '" + descripcion + "'.");
        }

        existente.setDescripcion(descripcion);
        return convertirFechasBaseItem(catalogoDtoMapper.toTipoGeneroResDto(tipoGeneroMascotaRepository.save(existente)));
    }

    @Transactional(rollbackFor = Exception.class)
    public CatalogoAdminResDto eliminarGeneroMascota(String uuid) throws BadRequestException {
        return cambiarEstadoGeneroMascota(uuid, Constant.ESTADO_LOGICO_INACTIVO);
    }

    @Transactional(rollbackFor = Exception.class)
    public CatalogoAdminResDto activarGeneroMascota(String uuid) throws BadRequestException {
        return cambiarEstadoGeneroMascota(uuid, Constant.ESTADO_LOGICO_ACTIVO);
    }

    private CatalogoAdminResDto cambiarEstadoGeneroMascota(String uuid, int nuevoEstado) throws BadRequestException {
        TipoGeneroMascota existente = tipoGeneroMascotaRepository.findByUuid(uuid.trim());
        if (existente == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró el género de mascota especificado.");
        }
        if (Integer.valueOf(nuevoEstado).equals(existente.getEstado())) {
            String msg = nuevoEstado == Constant.ESTADO_LOGICO_INACTIVO
                    ? "El género de mascota ya se encuentra inactivo."
                    : "El género de mascota ya se encuentra activo.";
            throw new BadRequestException(MessageCodes.ResponseCodeBR03, msg);
        }
        existente.setEstado(nuevoEstado);
        return convertirFechasBaseItem(catalogoDtoMapper.toTipoGeneroResDto(tipoGeneroMascotaRepository.save(existente)));
    }

    // =========================================================================
    // CONVERSIÓN DE FECHAS UTC -> LIMA
    // =========================================================================

    private List<DistritoLimaAdminResDto> convertirFechasDistritos(List<DistritoLimaAdminResDto> list) {
        list.forEach(this::convertirFechasDistrito);
        return list;
    }

    private DistritoLimaAdminResDto convertirFechasDistrito(DistritoLimaAdminResDto dto) {
        if (dto != null) {
            dto.setCreadoEl(UtilMethods.convertirUtcALima(dto.getCreadoEl()));
            dto.setActualizadoEl(UtilMethods.convertirUtcALima(dto.getActualizadoEl()));
        }
        return dto;
    }

    private List<TipoRazaAdminResDto> convertirFechasRazas(List<TipoRazaAdminResDto> list) {
        list.forEach(this::convertirFechasRaza);
        return list;
    }

    private TipoRazaAdminResDto convertirFechasRaza(TipoRazaAdminResDto dto) {
        if (dto != null) {
            dto.setCreadoEl(UtilMethods.convertirUtcALima(dto.getCreadoEl()));
            dto.setActualizadoEl(UtilMethods.convertirUtcALima(dto.getActualizadoEl()));
        }
        return dto;
    }

    private List<TipoTamanoMascotaAdminResDto> convertirFechasTamanos(List<TipoTamanoMascotaAdminResDto> list) {
        list.forEach(this::convertirFechasTamano);
        return list;
    }

    private TipoTamanoMascotaAdminResDto convertirFechasTamano(TipoTamanoMascotaAdminResDto dto) {
        if (dto != null) {
            dto.setCreadoEl(UtilMethods.convertirUtcALima(dto.getCreadoEl()));
            dto.setActualizadoEl(UtilMethods.convertirUtcALima(dto.getActualizadoEl()));
        }
        return dto;
    }

    private List<CatalogoAdminResDto> convertirFechasBase(List<CatalogoAdminResDto> list) {
        list.forEach(this::convertirFechasBaseItem);
        return list;
    }

    private CatalogoAdminResDto convertirFechasBaseItem(CatalogoAdminResDto dto) {
        if (dto != null) {
            dto.setCreadoEl(UtilMethods.convertirUtcALima(dto.getCreadoEl()));
            dto.setActualizadoEl(UtilMethods.convertirUtcALima(dto.getActualizadoEl()));
        }
        return dto;
    }
}
