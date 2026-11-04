package com.paseape.apipaseape.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
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
@Table(name = "disponibilidad_paseador")
@SQLRestriction("estado = 1")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibilidadPaseadorEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uuid", nullable = false, length = 36, unique = true)
    private String uuid;

    @Column(name = "paseador_id", nullable = false)
    private Long paseadorId;

    @Column(name = "dia_semana_id", nullable = false)
    private Integer diaSemanaId;

    @Column(name = "bloque_horario_id", nullable = false)
    private Integer bloqueHorarioId;

    @Column(name = "max_mascotas", nullable = false)
    private Integer maxMascotas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dia_semana_id", insertable = false, updatable = false)
    private DiaSemanaEntity diaSemana;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bloque_horario_id", insertable = false, updatable = false)
    private BloqueHorarioEntity bloqueHorario;

    @Column(name = "estado", nullable = false)
    private Boolean estado;
}

