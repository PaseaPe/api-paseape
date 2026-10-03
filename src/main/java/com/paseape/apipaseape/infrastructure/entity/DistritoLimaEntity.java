package com.paseape.apipaseape.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "distritos_lima")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistritoLimaEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "uuid", nullable = false, length = 36, unique = true)
    private String uuid;

    @Column(name = "descripcion", nullable = false, length = 100, unique = true)
    private String descripcion;

    @Column(name = "codigo_ubigeo", length = 10)
    private String codigoUbigeo;

    @Column(name = "estado", nullable = false)
    private Integer estado;
}
