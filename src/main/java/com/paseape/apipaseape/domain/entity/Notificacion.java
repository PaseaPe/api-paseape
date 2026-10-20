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
public class Notificacion {

    private Long id;
    private String uuid;
    private Usuario remitenteUsuario;
    private Usuario destinatarioUsuario;
    private TipoNotificacion tipoNotificacion;
    private String titulo;
    private String mensaje;
    private Long referenciaId;
    private String referenciaTipo;
    private Boolean leido;
    private LocalDateTime fechaLeido;
    private Integer estado;
    private LocalDateTime creadoEl;
    private LocalDateTime actualizadoEl;
    private String creadoPor;
    private String actualizadoPor;
}
