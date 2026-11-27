package com.paseape.apipaseape.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paseador {

    private Long id;
    private String uuid;
    private Usuario usuario;
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;
    private String antecedentesPolicialesUrl;
    private Integer experienciaAnos;
    private String biografia;
    private BigDecimal tarifaHoraPen;
    private DistritoLima distritoCobertura;
    private PaseadorEstadoVerificacion estadoVerificacion;
    private Integer paseosCompletados;
    private BigDecimal calificacionPromedio;
    @Builder.Default
    private Integer totalCalificaciones = 0;
    @Builder.Default
    private Integer sumaCalificaciones = 0;
    private Integer maxMascotasPredeterminado;
    private Integer estado;
    private LocalDateTime creadoEl;
    private LocalDateTime actualizadoEl;
    private String creadoPor;
    private String actualizadoPor;

    public void incrementarPaseosCompletados() {
        this.paseosCompletados = (this.paseosCompletados == null ? 0 : this.paseosCompletados) + 1;
    }

    public void registrarCalificacion(int puntuacion) {
        int totalActual = this.totalCalificaciones == null ? 0 : this.totalCalificaciones;
        int sumaActual = this.sumaCalificaciones == null ? 0 : this.sumaCalificaciones;
        this.totalCalificaciones = totalActual + 1;
        this.sumaCalificaciones = sumaActual + puntuacion;
        this.calificacionPromedio = BigDecimal.valueOf(this.sumaCalificaciones)
                .divide(BigDecimal.valueOf(this.totalCalificaciones), 2, RoundingMode.HALF_UP);
    }
}
