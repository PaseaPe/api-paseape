package com.paseape.apipaseape.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "brevo_email")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrevoEmailEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uuid", nullable = false, length = 36, unique = true)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private UsuarioEntity usuario;

    @Column(name = "destinatario_email", nullable = false, length = 150)
    private String destinatarioEmail;

    @Column(name = "destinatario_nombre", length = 100)
    private String destinatarioNombre;

    @Column(name = "remitente_email", nullable = false, length = 150)
    private String remitenteEmail;

    @Column(name = "asunto", nullable = false, length = 200)
    private String asunto;

    @Column(name = "tipo_notificacion", nullable = false, length = 50)
    private String tipoNotificacion;

    @Column(name = "payload_enviado", columnDefinition = "JSON")
    private String payloadEnviado;

    @Column(name = "respuesta_servicio", columnDefinition = "TEXT")
    private String respuestaServicio;

    @Column(name = "codigo_http")
    private Integer codigoHttp;

    @Column(name = "brevo_message_id", length = 100)
    private String brevoMessageId;

    @Column(name = "exitoso", nullable = false)
    private Integer exitoso;

    @Column(name = "error_mensaje", columnDefinition = "TEXT")
    private String errorMensaje;

    @Column(name = "estado", nullable = false)
    private Integer estado;
}
