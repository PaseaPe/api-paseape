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
public class BrevoEmail {

    private Long id;
    private String uuid;
    private Usuario usuario;
    private String destinatarioEmail;
    private String destinatarioNombre;
    private String remitenteEmail;
    private String asunto;
    private String tipoNotificacion;
    private String payloadEnviado;
    private String respuestaServicio;
    private Integer codigoHttp;
    private String brevoMessageId;
    private Integer exitoso;
    private String errorMensaje;
    private Integer estado;
    private LocalDateTime creadoEl;
    private LocalDateTime actualizadoEl;
    private String creadoPor;
    private String actualizadoPor;
}
