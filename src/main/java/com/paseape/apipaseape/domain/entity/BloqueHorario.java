package com.paseape.apipaseape.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloqueHorario {

    private Integer id;
    private String uuid;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String codigo;
    private Integer orden;
    private Boolean estado;
    private LocalDateTime creadoEl;
    private LocalDateTime actualizadoEl;
    private String creadoPor;
    private String actualizadoPor;

    public BloqueHorario(Integer id) {
        this.id = id;
    }
}
