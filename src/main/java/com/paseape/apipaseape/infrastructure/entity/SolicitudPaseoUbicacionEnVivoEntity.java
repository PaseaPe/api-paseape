package com.paseape.apipaseape.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_paseo_ubicacion_en_vivo")
@SQLRestriction("estado = 1")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudPaseoUbicacionEnVivoEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uuid", nullable = false, length = 36, unique = true)
    private String uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_paseo_id", nullable = false, unique = true)
    private SolicitudPaseoEntity solicitudPaseo;

    @Column(name = "latitud_actual", precision = 10, scale = 8, nullable = false)
    private BigDecimal latitudActual;

    @Column(name = "longitud_actual", precision = 11, scale = 8, nullable = false)
    private BigDecimal longitudActual;

    @Column(name = "precision_m", precision = 6, scale = 2)
    private BigDecimal precisionM;

    @Column(name = "velocidad_ms", precision = 6, scale = 2)
    private BigDecimal velocidadMs;

    @Column(name = "rumbo_grados", precision = 5, scale = 2)
    private BigDecimal rumboGrados;

    @Column(name = "fecha_hora_gps", nullable = false)
    private LocalDateTime fechaHoraGps;

    @Column(name = "coordenadas_codificadas", columnDefinition = "LONGTEXT", nullable = false)
    private String coordenadasCodificadas;

    @Column(name = "distancia_metros_acumulada", precision = 10, scale = 2, nullable = false)
    private BigDecimal distanciaMetrosAcumulada;

    @Column(name = "total_puntos", nullable = false)
    private Integer totalPuntos;

    @Column(name = "estado", nullable = false)
    private Integer estado;
}
