package com.paseape.apipaseape.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "administradores")
@SQLRestriction("estado = 1")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdministradorEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uuid", nullable = false, length = 36, unique = true)
    private String uuid;

    @ManyToOne
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private UsuarioEntity usuario;

    @Column(name = "codigo_empleado", nullable = false, length = 50, unique = true)
    private String codigoEmpleado;

    @Column(name = "area_departamento", length = 100)
    private String areaDepartamento;

    @Column(name = "superadmin", nullable = false)
    private Integer superadmin;

    @Column(name = "estado", nullable = false)
    private Integer estado;
}
