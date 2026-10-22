package com.paseape.apipaseape.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SolicitudPaseoResDto {

    private String uuid;
    private String codigoReserva;
    private Integer tipoModalidadPaseoId;
    private String tipoModalidadPaseoDescripcion;
    private Integer estadoId;
    private String estadoDescripcion;
    private Integer tipoMetodoPagoId;
    private String tipoMetodoPagoDescripcion;
    private Integer tipoDuracionPaseoId;
    private String tipoDuracionPaseoDescripcion;
    private Integer distritoId;
    private String distritoDescripcion;
    private String direccionRecojo;
    private String referenciaDireccion;
    private BigDecimal tarifaHoraPenSnapshot;
    private BigDecimal montoTotal;
    private String paseadorUuid;
    private String paseadorNombreCompleto;
    private String clienteUuid;
    private String clienteNombreCompleto;
    private List<SolicitudMascotaResDto> mascotas;
    private LocalDateTime creadoEl;
}
