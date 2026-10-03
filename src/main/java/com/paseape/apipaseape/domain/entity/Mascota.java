package com.paseape.apipaseape.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mascota {

    private Long id;
    private String uuid;
    private Cliente cliente;
    private String nombre;
    private TipoMascota tipoMascota;
    private TipoRaza tipoRaza;
    private TipoGeneroMascota tipoGeneroMascota;
    private TipoTamanoMascota tipoTamanoMascota;
    private TipoNivelEnergia tipoNivelEnergia;
    private Integer edadAnos;
    private Integer edadMeses;
    private BigDecimal pesoKg;
    private Integer esterilizado;
    private Integer sociableConPerros;
    private Integer sociableConPersonas;
    private String precaucionesMedicas;
    private String fotoUrl;
    private Integer estado;
    private LocalDateTime creadoEl;
    private LocalDateTime actualizadoEl;
    private String creadoPor;
    private String actualizadoPor;
}
