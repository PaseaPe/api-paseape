package com.paseape.apipaseape.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "solicitud_paseo_recorrido")
@SQLRestriction("estado = 1")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudPaseoRecorridoEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uuid", nullable = false, length = 36, unique = true)
    private String uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_paseo_id", nullable = false, unique = true)
    private SolicitudPaseoEntity solicitudPaseo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_origen_traza_id", nullable = false)
    private TipoOrigenTrazaEntity tipoOrigenTraza;

    @Column(name = "distancia_metros", precision = 10, scale = 2, nullable = false)
    private BigDecimal distanciaMetros;

    @Column(name = "duracion_segundos", nullable = false)
    private Integer duracionSegundos;

    @Column(name = "velocidad_promedio_kmh", precision = 5, scale = 2)
    private BigDecimal velocidadPromedioKmh;

    @Column(name = "coordenadas_codificadas", columnDefinition = "LONGTEXT", nullable = false)
    private String coordenadasCodificadas;

    @Column(name = "coordenadas_total_puntos", nullable = false)
    private Integer coordenadasTotalPuntos;

    @Column(name = "estado", nullable = false)
    private Integer estado;
}
