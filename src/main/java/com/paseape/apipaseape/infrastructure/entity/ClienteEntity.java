package com.paseape.apipaseape.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "clientes")
@SQLRestriction("estado = 1")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteEntity extends Auditable {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uuid", nullable = false, length = 36, unique = true)
    private String uuid;

    @ManyToOne
    @JoinColumn(name = "id", insertable = false, updatable = false)
    private UsuarioEntity usuario;

    @Column(name = "direccion_referencia")
    private String direccionReferencia;

    @ManyToOne
    @JoinColumn(name = "distrito_id")
    private DistritoLimaEntity distrito;

    @Column(name = "contacto_emergencia_nombre", length = 100)
    private String contactoEmergenciaNombre;

    @Column(name = "contacto_emergencia_telefono", length = 15)
    private String contactoEmergenciaTelefono;

    @Column(name = "notas_adicionales", columnDefinition = "TEXT")
    private String notasAdicionales;

    @Column(name = "estado", nullable = false)
    private Integer estado;
}
