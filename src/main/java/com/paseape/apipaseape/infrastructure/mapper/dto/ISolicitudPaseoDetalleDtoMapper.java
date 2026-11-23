package com.paseape.apipaseape.infrastructure.mapper.dto;

import com.paseape.apipaseape.domain.entity.Mascota;
import com.paseape.apipaseape.domain.entity.Paseador;
import com.paseape.apipaseape.domain.entity.SolicitudPaseo;
import com.paseape.apipaseape.domain.entity.SolicitudPaseoMascota;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudMascotaDetalleResDto;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudPaseadorDetalleResDto;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudPaseoDetalleResDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ISolicitudPaseoDetalleDtoMapper {

    @Mapping(target = "tipoModalidadPaseoId", source = "domain.tipoModalidadPaseo.id")
    @Mapping(target = "tipoModalidadPaseoDescripcion", source = "domain.tipoModalidadPaseo.descripcion")
    @Mapping(target = "estadoId", source = "domain.solicitudPaseoEstado.id")
    @Mapping(target = "estadoDescripcion", source = "domain.solicitudPaseoEstado.descripcion")
    @Mapping(target = "tipoMetodoPagoId", source = "domain.tipoMetodoPago.id")
    @Mapping(target = "tipoMetodoPagoDescripcion", source = "domain.tipoMetodoPago.descripcion")
    @Mapping(target = "tipoDuracionPaseoId", source = "domain.tipoDuracionPaseo.id")
    @Mapping(target = "tipoDuracionPaseoDescripcion", source = "domain.tipoDuracionPaseo.descripcion")
    @Mapping(target = "duracionMinutos", source = "domain.tipoDuracionPaseo.minutos")
    @Mapping(target = "distritoId", source = "domain.distrito.id")
    @Mapping(target = "distritoDescripcion", source = "domain.distrito.descripcion")
    @Mapping(target = "bloqueHorarioId", source = "domain.bloqueHorario.id")
    @Mapping(target = "bloqueHorarioCodigo", source = "domain.bloqueHorario.codigo")
    @Mapping(target = "bloqueHorarioHoraInicio", source = "domain.bloqueHorario.horaInicio")
    @Mapping(target = "bloqueHorarioHoraFin", source = "domain.bloqueHorario.horaFin")
    @Mapping(target = "clienteUuid", source = "domain.cliente.uuid")
    @Mapping(target = "clienteNombreCompleto", expression = "java(resolverClienteNombreCompleto(domain))")
    @Mapping(target = "paseador", expression = "java(toPaseadorDetalleResDto(domain.getPaseador()))")
    @Mapping(target = "mascotas", expression = "java(toMascotasDetalleDtoList(solicitudMascotas))")
    SolicitudPaseoDetalleResDto toDetalleResDto(SolicitudPaseo domain, List<SolicitudPaseoMascota> solicitudMascotas);

    @Mapping(target = "usuarioUuid", source = "usuario.uuid")
    @Mapping(target = "nombres", source = "usuario.nombres")
    @Mapping(target = "apellidos", source = "usuario.apellidos")
    @Mapping(target = "nombreCompleto", expression = "java(resolverPaseadorNombreCompleto(paseador))")
    @Mapping(target = "correo", source = "usuario.correo")
    @Mapping(target = "telefono", source = "usuario.telefono")
    @Mapping(target = "fotoPerfilUrl", source = "usuario.fotoPerfilUrl")
    @Mapping(target = "distritoCoberturaId", source = "distritoCobertura.id")
    @Mapping(target = "distritoCoberturaDescripcion", source = "distritoCobertura.descripcion")
    SolicitudPaseadorDetalleResDto toPaseadorDetalleResDto(Paseador paseador);

    @Mapping(target = "tipoMascotaId", source = "tipoMascota.id")
    @Mapping(target = "tipoMascotaDescripcion", source = "tipoMascota.descripcion")
    @Mapping(target = "tipoRazaId", source = "tipoRaza.id")
    @Mapping(target = "tipoRazaDescripcion", source = "tipoRaza.descripcion")
    @Mapping(target = "tipoGeneroMascotaId", source = "tipoGeneroMascota.id")
    @Mapping(target = "tipoGeneroMascotaDescripcion", source = "tipoGeneroMascota.descripcion")
    @Mapping(target = "tipoTamanoMascotaId", source = "tipoTamanoMascota.id")
    @Mapping(target = "tipoTamanoMascotaDescripcion", source = "tipoTamanoMascota.descripcion")
    @Mapping(target = "tipoNivelEnergiaId", source = "tipoNivelEnergia.id")
    @Mapping(target = "tipoNivelEnergiaDescripcion", source = "tipoNivelEnergia.descripcion")
    SolicitudMascotaDetalleResDto toMascotaDetalleResDto(Mascota mascota);

    default List<SolicitudMascotaDetalleResDto> toMascotasDetalleDtoList(List<SolicitudPaseoMascota> list) {
        if (list == null) {
            return List.of();
        }
        return list.stream()
                .filter(spm -> spm != null && spm.getMascota() != null)
                .map(spm -> toMascotaDetalleResDto(spm.getMascota()))
                .toList();
    }

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

    default String resolverPaseadorNombreCompleto(Paseador paseador) {
        if (paseador == null || paseador.getUsuario() == null) {
            return null;
        }
        var u = paseador.getUsuario();
        String nombres = u.getNombres() != null ? u.getNombres().trim() : "";
        String apellidos = u.getApellidos() != null ? u.getApellidos().trim() : "";
        String full = (nombres + " " + apellidos).trim();
        return full.isEmpty() ? null : full;
    }
}
