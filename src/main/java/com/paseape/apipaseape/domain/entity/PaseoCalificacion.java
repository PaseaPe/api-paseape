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
public class PaseoCalificacion {

    private Long id;
    private String uuid;
    private SolicitudPaseo solicitudPaseo;
    private Cliente cliente;
    private Paseador paseador;
    private Integer puntuacion;
    private String comentario;
    private LocalDateTime fechaComentario;
    private Boolean visible;
    private String motivoOcultamiento;
    private Integer estado;
    private LocalDateTime creadoEl;
    private LocalDateTime actualizadoEl;
    private String creadoPor;
    private String actualizadoPor;
}
