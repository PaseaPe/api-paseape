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
public class DiaSemana {

    private Integer id;
    private String uuid;
    private String descripcion;
    private Integer numeroDia;
    private Boolean estado;
    private LocalDateTime creadoEl;
    private LocalDateTime actualizadoEl;
    private String creadoPor;
    private String actualizadoPor;

    public DiaSemana(Integer id) {
        this.id = id;
    }
}
