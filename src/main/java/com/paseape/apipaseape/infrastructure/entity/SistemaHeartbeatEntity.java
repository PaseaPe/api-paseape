package com.paseape.apipaseape.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sistema_heartbeat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SistemaHeartbeatEntity extends Auditable{
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "componente", nullable = false, length = 50, unique = true)
    private String componente;

    @Column(name = "ultimo_latido", nullable = false)
    private LocalDateTime ultimoLatido;

    @Column(name = "latidos_acumulados", nullable = false)
    private Long latidosAcumulados;

    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    @Column(name = "descripcion", length = 150)
    private String descripcion;

    @Column(name = "estado")
    protected Integer estado;
}
