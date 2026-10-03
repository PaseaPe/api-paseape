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

import java.math.BigDecimal;

@Entity
@Table(name = "mascotas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MascotaEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uuid", nullable = false, length = 36, unique = true)
    private String uuid;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteEntity cliente;

    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "tipo_mascota_id", nullable = false)
    private TipoMascotaEntity tipoMascota;

    @ManyToOne
    @JoinColumn(name = "tipo_raza_id", nullable = false)
    private TipoRazaEntity tipoRaza;

    @ManyToOne
    @JoinColumn(name = "tipo_genero_mascota_id", nullable = false)
    private TipoGeneroMascotaEntity tipoGeneroMascota;

    @ManyToOne
    @JoinColumn(name = "tipo_tamaño_mascota_id", nullable = false)
    private TipoTamanoMascotaEntity tipoTamanoMascota;

    @ManyToOne
    @JoinColumn(name = "tipo_nivel_energia_id", nullable = false)
    private TipoNivelEnergiaEntity tipoNivelEnergia;

    @Column(name = "edad_años")
    private Integer edadAnos;

    @Column(name = "edad_meses")
    private Integer edadMeses;

    @Column(name = "peso_kg", precision = 5, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "esterilizado", nullable = false)
    private Integer esterilizado;

    @Column(name = "sociable_con_perros", nullable = false)
    private Integer sociableConPerros;

    @Column(name = "sociable_con_personas", nullable = false)
    private Integer sociableConPersonas;

    @Column(name = "precauciones_medicas", columnDefinition = "TEXT")
    private String precaucionesMedicas;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "estado", nullable = false)
    private Integer estado;
}
