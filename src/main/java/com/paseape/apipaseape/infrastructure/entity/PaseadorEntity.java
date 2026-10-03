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

import java.math.BigDecimal;

@Entity
@Table(name = "paseadores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaseadorEntity extends Auditable {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uuid", nullable = false, length = 36, unique = true)
    private String uuid;

    @ManyToOne
    @JoinColumn(name = "id", insertable = false, updatable = false)
    private UsuarioEntity usuario;

    @ManyToOne
    @JoinColumn(name = "tipo_documento_id")
    private TipoDocumentoEntity tipoDocumento;

    @Column(name = "numero_documento", length = 20, unique = true)
    private String numeroDocumento;

    @Column(name = "antecedentes_policiales_url")
    private String antecedentesPolicialesUrl;

    @Column(name = "experiencia_años")
    private Integer experienciaAnos;

    @Column(name = "biografia", columnDefinition = "TEXT")
    private String biografia;

    @Column(name = "tarifa_hora_pen", precision = 10, scale = 2)
    private BigDecimal tarifaHoraPen;

    @ManyToOne
    @JoinColumn(name = "distrito_cobertura_id")
    private DistritoLimaEntity distritoCobertura;

    @ManyToOne
    @JoinColumn(name = "paseador_estado_verificacion_id", nullable = false)
    private PaseadorEstadoVerificacionEntity estadoVerificacion;

    @Column(name = "paseos_completados", nullable = false)
    private Integer paseosCompletados;

    @Column(name = "calificacion_promedio", precision = 3, scale = 2, nullable = false)
    private BigDecimal calificacionPromedio;

    @Column(name = "estado", nullable = false)
    private Integer estado;
}
