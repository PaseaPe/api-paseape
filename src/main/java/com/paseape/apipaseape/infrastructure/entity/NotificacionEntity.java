package com.paseape.apipaseape.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@SQLRestriction("estado = 1")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uuid", nullable = false, length = 36, unique = true)
    private String uuid;

    @ManyToOne
    @JoinColumn(name = "remitente_usuario_id")
    private UsuarioEntity remitenteUsuario;

    @ManyToOne
    @JoinColumn(name = "destinatario_usuario_id", nullable = false)
    private UsuarioEntity destinatarioUsuario;

    @ManyToOne
    @JoinColumn(name = "tipo_notificacion_id", nullable = false)
    private TipoNotificacionEntity tipoNotificacion;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(name = "referencia_tipo", length = 50)
    private String referenciaTipo;

    @Column(name = "leido", nullable = false)
    private Boolean leido;

    @Column(name = "fecha_leido")
    private LocalDateTime fechaLeido;

    @Column(name = "estado", nullable = false)
    private Integer estado;
}
