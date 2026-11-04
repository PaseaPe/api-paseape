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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_paseo")
@SQLRestriction("estado = 1")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudPaseoEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uuid", nullable = false, length = 36, unique = true)
    private String uuid;

    @Column(name = "codigo_reserva", nullable = false, length = 50, unique = true)
    private String codigoReserva;

    @ManyToOne
    @JoinColumn(name = "tipo_modalidad_paseo_id", nullable = false)
    private TipoModalidadPaseoEntity tipoModalidadPaseo;

    @ManyToOne
    @JoinColumn(name = "tipo_metodo_pago_id", nullable = false)
    private TipoMetodoPagoEntity tipoMetodoPago;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteEntity cliente;

    @ManyToOne
    @JoinColumn(name = "paseador_id")
    private PaseadorEntity paseador;

    @ManyToOne
    @JoinColumn(name = "distrito_id", nullable = false)
    private DistritoLimaEntity distrito;

    @Column(name = "direccion_recojo", nullable = false, length = 255)
    private String direccionRecojo;

    @Column(name = "referencia_direccion", length = 255)
    private String referenciaDireccion;

    @Column(name = "fecha_programada")
    private LocalDate fechaProgramada;

    @ManyToOne
    @JoinColumn(name = "bloque_horario_id")
    private BloqueHorarioEntity bloqueHorario;

    @ManyToOne
    @JoinColumn(name = "tipo_duracion_paseo_id", nullable = false)
    private TipoDuracionPaseoEntity tipoDuracionPaseo;

    @Column(name = "tarifa_hora_pen_snapshot", precision = 10, scale = 2, nullable = false)
    private BigDecimal tarifaHoraPenSnapshot;

    @Column(name = "monto_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal montoTotal;

    @ManyToOne
    @JoinColumn(name = "solicitud_paseo_estado_id", nullable = false)
    private SolicitudPaseoEstadoEntity solicitudPaseoEstado;

    @Column(name = "motivo_rechazo_cancelacion", columnDefinition = "TEXT")
    private String motivoRechazoCancelacion;

    @Column(name = "hora_vencimiento")
    private LocalDateTime horaVencimiento;

    @Column(name = "fecha_hora_aceptada")
    private LocalDateTime fechaHoraAceptada;

    @Column(name = "fecha_hora_inicio_real")
    private LocalDateTime fechaHoraInicioReal;

    @Column(name = "fecha_hora_fin_real")
    private LocalDateTime fechaHoraFinReal;

    @Column(name = "estado", nullable = false)
    private Integer estado;
}
