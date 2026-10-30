package com.paseape.apipaseape.infrastructure.mapper.dto;

import com.paseape.apipaseape.domain.entity.Mascota;
import com.paseape.apipaseape.domain.entity.SolicitudPaseo;
import com.paseape.apipaseape.domain.entity.SolicitudPaseoMascota;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudMascotaResDto;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudPaseoResDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ISolicitudPaseoDtoMapper {

    @Mapping(target = "tipoModalidadPaseoId", source = "domain.tipoModalidadPaseo.id")
    @Mapping(target = "tipoModalidadPaseoDescripcion", source = "domain.tipoModalidadPaseo.descripcion")
    @Mapping(target = "estadoId", source = "domain.solicitudPaseoEstado.id")
    @Mapping(target = "estadoDescripcion", source = "domain.solicitudPaseoEstado.descripcion")
    @Mapping(target = "tipoMetodoPagoId", source = "domain.tipoMetodoPago.id")
    @Mapping(target = "tipoMetodoPagoDescripcion", source = "domain.tipoMetodoPago.descripcion")
    @Mapping(target = "tipoDuracionPaseoId", source = "domain.tipoDuracionPaseo.id")
    @Mapping(target = "tipoDuracionPaseoDescripcion", source = "domain.tipoDuracionPaseo.descripcion")
    @Mapping(target = "distritoId", source = "domain.distrito.id")
    @Mapping(target = "distritoDescripcion", source = "domain.distrito.descripcion")
    @Mapping(target = "bloqueHorarioId", source = "domain.bloqueHorario.id")
    @Mapping(target = "bloqueHorarioCodigo", source = "domain.bloqueHorario.codigo")
    @Mapping(target = "clienteUuid", source = "domain.cliente.uuid")
    @Mapping(target = "clienteNombreCompleto", expression = "java(resolverClienteNombreCompleto(domain))")
    @Mapping(target = "paseadorUuid", source = "domain.paseador.uuid")
    @Mapping(target = "paseadorNombreCompleto", expression = "java(resolverPaseadorNombreCompleto(domain))")
    @Mapping(target = "mascotas", expression = "java(toMascotasDtoListFromSpm(solicitudMascotas))")
    SolicitudPaseoResDto toResDto(SolicitudPaseo domain, List<SolicitudPaseoMascota> solicitudMascotas);

    @Mapping(target = "tipoModalidadPaseoId", source = "domain.tipoModalidadPaseo.id")
    @Mapping(target = "tipoModalidadPaseoDescripcion", source = "domain.tipoModalidadPaseo.descripcion")
    @Mapping(target = "estadoId", source = "domain.solicitudPaseoEstado.id")
    @Mapping(target = "estadoDescripcion", source = "domain.solicitudPaseoEstado.descripcion")
    @Mapping(target = "tipoMetodoPagoId", source = "domain.tipoMetodoPago.id")
    @Mapping(target = "tipoMetodoPagoDescripcion", source = "domain.tipoMetodoPago.descripcion")
    @Mapping(target = "tipoDuracionPaseoId", source = "domain.tipoDuracionPaseo.id")
    @Mapping(target = "tipoDuracionPaseoDescripcion", source = "domain.tipoDuracionPaseo.descripcion")
    @Mapping(target = "distritoId", source = "domain.distrito.id")
    @Mapping(target = "distritoDescripcion", source = "domain.distrito.descripcion")
    @Mapping(target = "bloqueHorarioId", source = "domain.bloqueHorario.id")
    @Mapping(target = "bloqueHorarioCodigo", source = "domain.bloqueHorario.codigo")
    @Mapping(target = "clienteUuid", source = "domain.cliente.uuid")
    @Mapping(target = "clienteNombreCompleto", expression = "java(resolverClienteNombreCompleto(domain))")
    @Mapping(target = "paseadorUuid", source = "domain.paseador.uuid")
    @Mapping(target = "paseadorNombreCompleto", expression = "java(resolverPaseadorNombreCompleto(domain))")
    @Mapping(target = "mascotas", expression = "java(toMascotasDtoListFromMascotas(mascotas))")
    SolicitudPaseoResDto toResDtoFromMascotas(SolicitudPaseo domain, List<Mascota> mascotas);

    default String resolverClienteNombreCompleto(SolicitudPaseo domain) {
        if (domain == null || domain.getCliente() == null || domain.getCliente().getUsuario() == null) {
            return null;
        }
        var u = domain.getCliente().getUsuario();
        String nombres = u.getNombres() != null ? u.getNombres().trim() : "";
        String apellidos = u.getApellidos() != null ? u.getApellidos().trim() : "";
        String full = (nombres + " " + apellidos).trim();
        return full.isEmpty() ? null : full;
    }

    default String resolverPaseadorNombreCompleto(SolicitudPaseo domain) {
        if (domain == null || domain.getPaseador() == null || domain.getPaseador().getUsuario() == null) {
            return null;
        }
        var u = domain.getPaseador().getUsuario();
        String nombres = u.getNombres() != null ? u.getNombres().trim() : "";
        String apellidos = u.getApellidos() != null ? u.getApellidos().trim() : "";
        String full = (nombres + " " + apellidos).trim();
        return full.isEmpty() ? null : full;
    }

    default List<SolicitudMascotaResDto> toMascotasDtoListFromSpm(List<SolicitudPaseoMascota> list) {
        if (list == null) {
            return List.of();
        }
        return list.stream()
                .filter(spm -> spm != null && spm.getMascota() != null)
                .map(spm -> toMascotaResDto(spm.getMascota()))
                .toList();
    }

    default List<SolicitudMascotaResDto> toMascotasDtoListFromMascotas(List<Mascota> list) {
        if (list == null) {
            return List.of();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(this::toMascotaResDto)
                .toList();
    }

    default SolicitudMascotaResDto toMascotaResDto(Mascota m) {
        if (m == null) {
            return null;
        }
        return SolicitudMascotaResDto.builder()
                .uuid(m.getUuid())
                .nombre(m.getNombre())
                .raza(m.getTipoRaza() != null ? m.getTipoRaza().getDescripcion() : null)
                .tamano(m.getTipoTamanoMascota() != null ? m.getTipoTamanoMascota().getDescripcion() : null)
                .build();
    }
}
