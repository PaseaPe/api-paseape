package com.paseape.apipaseape.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SistemaHeartbeat {
    private Integer id;
    private String componente;
    private LocalDateTime ultimoLatido;
    private Long latidosAcumulados;
    private String ipOrigen;
    private String descripcion;
    private Integer estado;

    public void registrarNuevoLatido(String ipOrigen) {
        this.ultimoLatido = LocalDateTime.now();
        this.latidosAcumulados = (this.latidosAcumulados == null ? 0L : this.latidosAcumulados) + 1L;
        this.ipOrigen = ipOrigen;
    }
}
