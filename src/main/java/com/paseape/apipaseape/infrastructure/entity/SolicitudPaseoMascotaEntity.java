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

@Entity
@Table(name = "solicitud_paseo_mascota")
@SQLRestriction("estado = 1")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudPaseoMascotaEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uuid", nullable = false, length = 36, unique = true)
    private String uuid;

    @ManyToOne
    @JoinColumn(name = "solicitud_paseo_id", nullable = false)
    private SolicitudPaseoEntity solicitudPaseo;

    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false)
    private MascotaEntity mascota;

    @Column(name = "estado", nullable = false)
    private Integer estado;
}
