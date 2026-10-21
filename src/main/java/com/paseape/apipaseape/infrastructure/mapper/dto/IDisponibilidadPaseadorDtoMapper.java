package com.paseape.apipaseape.infrastructure.mapper.dto;

import com.paseape.apipaseape.domain.entity.DisponibilidadPaseador;
import com.paseape.apipaseape.infrastructure.dto.response.DisponibilidadPaseadorResDto;
import com.paseape.apipaseape.infrastructure.dto.response.DisponibilidadSlotResDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IDisponibilidadPaseadorDtoMapper {

    DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Mapping(target = "id", source = "id")
    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "diaSemanaId", source = "diaSemanaId")
    @Mapping(target = "diaSemanaDescripcion", source = "diaSemana.descripcion")
    @Mapping(target = "bloqueHorarioId", source = "bloqueHorarioId")
    @Mapping(target = "bloqueCodigo", source = "bloqueHorario.codigo")
    @Mapping(target = "horaInicio", expression = "java(domain.getBloqueHorario() != null && domain.getBloqueHorario().getHoraInicio() != null ? domain.getBloqueHorario().getHoraInicio().format(TIME_FORMATTER) : null)")
    @Mapping(target = "horaFin", expression = "java(domain.getBloqueHorario() != null && domain.getBloqueHorario().getHoraFin() != null ? domain.getBloqueHorario().getHoraFin().format(TIME_FORMATTER) : null)")
    @Mapping(target = "maxMascotas", source = "maxMascotas")
    @Mapping(target = "activo", source = "estado")
    @Mapping(target = "mascotasOcupadas", ignore = true)
    @Mapping(target = "cuposDisponibles", ignore = true)
    @Mapping(target = "estaLleno", ignore = true)
    @Mapping(target = "disponible", ignore = true)
    DisponibilidadSlotResDto toSlotResDto(DisponibilidadPaseador domain);

    List<DisponibilidadSlotResDto> toSlotResDtoList(List<DisponibilidadPaseador> domainList);

    default DisponibilidadPaseadorResDto toResDto(Integer maxMascotasPredeterminado, List<DisponibilidadPaseador> disponibilidades) {
        return DisponibilidadPaseadorResDto.builder()
                .maxMascotasPredeterminado(maxMascotasPredeterminado)
                .slots(toSlotResDtoList(disponibilidades))
                .build();
    }
}
