package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.IDigitalDocumentRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoMascotaRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoRepository;
import com.paseape.apipaseape.domain.entity.DigitalDocument;
import com.paseape.apipaseape.domain.entity.Mascota;
import com.paseape.apipaseape.domain.entity.SolicitudPaseo;
import com.paseape.apipaseape.domain.entity.SolicitudPaseoMascota;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudMascotaDetalleResDto;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudPaseoDetalleResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.ISolicitudPaseoDetalleDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolicitudPaseoDetalleService {

    private final ISolicitudPaseoRepository solicitudPaseoRepository;
    private final ISolicitudPaseoMascotaRepository solicitudPaseoMascotaRepository;
    private final ISolicitudPaseoDetalleDtoMapper solicitudPaseoDetalleDtoMapper;
    private final IDigitalDocumentRepository digitalDocumentRepository;
    private final DigitalDocumentService digitalDocumentService;

    @Transactional(readOnly = true)
    public SolicitudPaseoDetalleResDto obtenerDetalle(String uuid) throws BadRequestException {
        SolicitudPaseo solicitud = solicitudPaseoRepository.findByUuidWithDetails(uuid.trim());
        if (solicitud == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR98, "No se encontró la solicitud de paseo especificada.");
        }

        List<SolicitudPaseoMascota> mascotas = solicitudPaseoMascotaRepository.findAllBySolicitudPaseoIdWithDetails(solicitud.getId());
        SolicitudPaseoDetalleResDto detalleDto = solicitudPaseoDetalleDtoMapper.toDetalleResDto(solicitud, mascotas);

        if (detalleDto != null && detalleDto.getMascotas() != null && mascotas != null) {
            Map<String, Mascota> mascotaPorUuid = mascotas.stream()
                    .map(SolicitudPaseoMascota::getMascota)
                    .filter(m -> m != null && m.getUuid() != null)
                    .collect(Collectors.toMap(Mascota::getUuid, Function.identity(), (a, b) -> a));

            for (SolicitudMascotaDetalleResDto mascotaDto : detalleDto.getMascotas()) {
                Mascota mascota = mascotaPorUuid.get(mascotaDto.getUuid());
                if (mascota != null
                        && mascota.getFolderDigitalDocument() != null
                        && mascota.getFolderDigitalDocument().getId() != null) {
                    DigitalDocument fotoMascota = digitalDocumentRepository.findByFolderIdAndDigitalDocumentTypeId(
                            mascota.getFolderDigitalDocument().getId(),
                            Constant.ID_DOC_FOTO_MASCOTA
                    );
                    if (fotoMascota != null) {
                        mascotaDto.setFotoMascota(digitalDocumentService.descargarDocumento(fotoMascota.getUuid()));
                    }
                }
            }
        }

        return detalleDto;
    }
}
