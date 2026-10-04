package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.infrastructure.mapper.dto.IApplicationParameterDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paseape.apipaseape.application.repository.IDistritoLimaRepository;
import com.paseape.apipaseape.application.repository.ITipoDocumentoRepository;
import com.paseape.apipaseape.application.repository.ITipoGeneroMascotaRepository;
import com.paseape.apipaseape.application.repository.ITipoMascotaRepository;
import com.paseape.apipaseape.application.repository.ITipoNivelEnergiaRepository;
import com.paseape.apipaseape.application.repository.ITipoRazaRepository;
import com.paseape.apipaseape.application.repository.ITipoTamanoMascotaRepository;
import com.paseape.apipaseape.application.repository.ITipoUsuarioRepository;
import com.paseape.apipaseape.domain.entity.TipoRaza;
import com.paseape.apipaseape.domain.entity.TipoUsuario;
import com.paseape.apipaseape.infrastructure.dto.response.parameter.ApplicationParameterResDto;
import com.paseape.apipaseape.infrastructure.dto.response.parameter.CatalogoItemResDto;
import com.paseape.apipaseape.infrastructure.dto.response.parameter.DistritoItemResDto;
import com.paseape.apipaseape.infrastructure.dto.response.parameter.TipoDocumentoItemResDto;
import com.paseape.apipaseape.infrastructure.dto.response.parameter.TipoRazaItemResDto;
import com.paseape.apipaseape.infrastructure.dto.response.parameter.TipoTamanoMascotaItemResDto;

import java.util.List;

import static com.paseape.apipaseape.infrastructure.constant.Constant.ESTADO_LOGICO_ACTIVO;
import static com.paseape.apipaseape.infrastructure.constant.Constant.ID_ADMINISTRADOR;

@Service
@RequiredArgsConstructor
public class ApplicationParameterService {

    private final ITipoUsuarioRepository tipoUsuarioRepository;
    private final ITipoDocumentoRepository tipoDocumentoRepository;
    private final IDistritoLimaRepository distritoLimaRepository;
    private final ITipoMascotaRepository tipoMascotaRepository;
    private final ITipoRazaRepository tipoRazaRepository;
    private final ITipoTamanoMascotaRepository tipoTamanoMascotaRepository;
    private final ITipoNivelEnergiaRepository tipoNivelEnergiaRepository;
    private final ITipoGeneroMascotaRepository tipoGeneroMascotaRepository;
    private final IApplicationParameterDtoMapper parameterDtoMapper;

    @Transactional(readOnly = true)
    public ApplicationParameterResDto getAllParameters() {
        List<TipoUsuario> tiposUsuario = tipoUsuarioRepository
                .findAllByEstado(ESTADO_LOGICO_ACTIVO)
                .stream()
                .filter(item -> item.getId() != ID_ADMINISTRADOR)
                .toList();

        List<CatalogoItemResDto> tiposUsuarioDto = parameterDtoMapper.toTipoUsuarioDtoList(tiposUsuario);
        List<TipoDocumentoItemResDto> tiposDocumentoDto = parameterDtoMapper.toTipoDocumentoDtoList(tipoDocumentoRepository.findAllByEstado(ESTADO_LOGICO_ACTIVO));
        List<DistritoItemResDto> distritosDto = parameterDtoMapper.toDistritoDtoList(distritoLimaRepository.findAllByEstado(ESTADO_LOGICO_ACTIVO));
        List<CatalogoItemResDto> tiposMascotaDto = parameterDtoMapper.toTipoMascotaDtoList(tipoMascotaRepository.findAllByEstado(ESTADO_LOGICO_ACTIVO));
        List<TipoRazaItemResDto> razasDto = parameterDtoMapper.toTipoRazaDtoList(tipoRazaRepository.findAllByEstado(ESTADO_LOGICO_ACTIVO));
        List<CatalogoItemResDto> generosMascotaDto = parameterDtoMapper.toTipoGeneroMascotaDtoList(tipoGeneroMascotaRepository.findAllByEstado(ESTADO_LOGICO_ACTIVO));
        List<TipoTamanoMascotaItemResDto> tamanosMascotaDto = parameterDtoMapper.toTipoTamanoMascotaDtoList(tipoTamanoMascotaRepository.findAllByEstado(ESTADO_LOGICO_ACTIVO));
        List<CatalogoItemResDto> nivelesEnergiaDto = parameterDtoMapper.toTipoNivelEnergiaDtoList(tipoNivelEnergiaRepository.findAllByEstado(ESTADO_LOGICO_ACTIVO));

        return ApplicationParameterResDto.builder()
                .tiposUsuario(tiposUsuarioDto)
                .tiposDocumento(tiposDocumentoDto)
                .distritos(distritosDto)
                .tiposMascota(tiposMascotaDto)
                .razas(razasDto)
                .generosMascota(generosMascotaDto)
                .tamanosMascota(tamanosMascotaDto)
                .nivelesEnergia(nivelesEnergiaDto)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TipoRazaItemResDto> getRazasByTipoMascotaId(Integer tipoMascotaId) {
        if (tipoMascotaId == null) {
            return List.of();
        }
        List<TipoRaza> razas = tipoRazaRepository.findAllByTipoMascotaIdAndEstado(tipoMascotaId, ESTADO_LOGICO_ACTIVO);
        return parameterDtoMapper.toTipoRazaDtoList(razas);
    }
}