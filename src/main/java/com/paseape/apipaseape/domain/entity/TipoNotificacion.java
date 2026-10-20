package com.paseape.apipaseape.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoNotificacion {

    private Integer id;
    private String uuid;
    private String descripcion;
    private String detalle;
    private String plantillaAsunto;
    private Boolean estado;
    private LocalDateTime creadoEl;
    private LocalDateTime actualizadoEl;
    private String creadoPor;
    private String actualizadoPor;

    public TipoNotificacion(Integer id) {
        this.id = id;
    }
}
