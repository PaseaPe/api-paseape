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
public class DisponibilidadPaseador {

    private Long id;
    private String uuid;
    private Paseador paseador;
    private DiaSemana diaSemana;
    private BloqueHorario bloqueHorario;
    private Integer maxMascotas;
    private Integer estado;
    private LocalDateTime creadoEl;
    private LocalDateTime actualizadoEl;
    private String creadoPor;
    private String actualizadoPor;
}
