# Persistencia

La persistencia en PaseaPe sigue el patrón de Puertos y Adaptadores:
- **Puerto:** Interfaz en `application/repository/` con métodos orientados al dominio.
- **Adaptador:** Componente en `infrastructure/repository/` que une el puerto con Spring Data JPA y MapStruct.
- **Spring Data JPA:** Interfaz en `infrastructure/repository/jpa/` que hereda de `JpaRepository`.

El adaptador es la única pieza del sistema que conoce tanto a las entidades JPA como a MapStruct.

---

## Puerto (Application Repository)

```java
package com.paseape.apipaseape.application.repository;

import com.paseape.apipaseape.domain.entity.SolicitudPaseo;
import java.util.List;

public interface ISolicitudPaseoRepository {
    SolicitudPaseo save(SolicitudPaseo solicitudPaseo);
    SolicitudPaseo findByUuid(String uuid);
    SolicitudPaseo findById(Long id);
    List<SolicitudPaseo> findAllByClienteId(Long clienteId);
}
```

- **Regla Estricta:** Prohibido retornar `Optional<T>`. Si no existe el registro, retorna `null`. Si es una colección, retorna `List<T>` (lista vacía si no hay registros, nunca `null`).
- Trabaja exclusivamente con entidades del paquete `domain/entity/`.

---

## Adaptador (Infrastructure Repository)

```java
package com.paseape.apipaseape.infrastructure.repository;

import com.paseape.apipaseape.application.repository.ISolicitudPaseoRepository;
import com.paseape.apipaseape.domain.entity.SolicitudPaseo;
import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.ISolicitudPaseoDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ISolicitudPaseoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class SolicitudPaseoRepository implements ISolicitudPaseoRepository {

    private final ISolicitudPaseoJpaRepository solicitudPaseoJpaRepository;
    private final ISolicitudPaseoDboMapper solicitudPaseoDboMapper;

    @Override
    public SolicitudPaseo save(SolicitudPaseo solicitudPaseo) {
        SolicitudPaseoEntity entity = solicitudPaseoDboMapper.toDbo(solicitudPaseo);
        entity = solicitudPaseoJpaRepository.save(entity);
        return solicitudPaseoDboMapper.toDomain(entity);
    }

    @Override
    public SolicitudPaseo findByUuid(String uuid) {
        SolicitudPaseoEntity entity = solicitudPaseoJpaRepository.findByUuid(uuid).orElse(null);
        return entity != null ? solicitudPaseoDboMapper.toDomain(entity) : null;
    }

    @Override
    public SolicitudPaseo findById(Long id) {
        SolicitudPaseoEntity entity = solicitudPaseoJpaRepository.findById(id).orElse(null);
        return entity != null ? solicitudPaseoDboMapper.toDomain(entity) : null;
    }

    @Override
    public List<SolicitudPaseo> findAllByClienteId(Long clienteId) {
        List<SolicitudPaseoEntity> entities = solicitudPaseoJpaRepository.findAllByClienteId(clienteId);
        return solicitudPaseoDboMapper.toDomain(entities);
    }
}
```

---

## Spring Data JPA

```java
package com.paseape.apipaseape.infrastructure.repository.jpa;

import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ISolicitudPaseoJpaRepository extends JpaRepository<SolicitudPaseoEntity, Long> {

    Optional<SolicitudPaseoEntity> findByUuid(String uuid);

    List<SolicitudPaseoEntity> findAllByClienteId(Long clienteId);

    // Consulta de alta concurrencia para cálculo dinámico de cupos ocupados
    @Query("""
        SELECT sp.bloqueHorario.id, COUNT(spm.id)
        FROM SolicitudPaseoEntity sp
        JOIN sp.solicitudPaseoMascotas spm
        WHERE sp.paseador.id = :paseadorId
          AND sp.fechaProgramada = :fecha
          AND sp.solicitudPaseoEstado.id IN (:estadosActivos)
        GROUP BY sp.bloqueHorario.id
    """)
    List<Object[]> countMascotasOcupadasAgrupadoPorBloque(
            @Param("paseadorId") Long paseadorId,
            @Param("fecha") LocalDate fecha,
            @Param("estadosActivos") List<Integer> estadosActivos
    );
}
```

---

## Entidades JPA, Auditoría y Borrado Lógico

```java
package com.paseape.apipaseape.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "solicitud_paseo")
@SQLRestriction("estado = 1")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudPaseoEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(name = "codigo_reserva", nullable = false, unique = true, length = 30)
    private String codigoReserva;

    @Column(name = "monto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "estado", nullable = false)
    private Integer estado;
}
```

- **Herencia Obligatoria de `Auditable`:** Todas las entidades JPA (`*Entity.java`) deben extender obligatoriamente de la clase base `Auditable` (`infrastructure/entity/Auditable.java`). Esta superclase (`@MappedSuperclass` y `@EntityListeners(AuditingEntityListener.class)`) aporta y gestiona automáticamente los campos de auditoría temporal y de autoría:
  - `creadoEl` (`creado_el` con `@CreatedDate`)
  - `actualizadoEl` (`actualizado_el` con `@LastModifiedDate`)
  - `creadoPor` (`creado_por` con `@CreatedBy`)
  - `actualizadoPor` (`actualizado_por` con `@LastModifiedBy`)
  Queda prohibido redeclarar manualmente estas columnas dentro de cada entidad.
- **`@SQLRestriction("estado = 1")`:** Obligatoria en entidades transaccionales y maestras (`*Entity.java`) para aplicar filtrado automático en consultas directas y relaciones (`@ManyToOne`, `@OneToMany`).
- **Excepción:** Las entidades de catálogo lookup tables (`TipoModalidadPaseo`, `DistritoLima`, `DiaSemana`, etc.) **no** llevan `@SQLRestriction`.
- **Prohibido el uso de `record`:** Modelar siempre con clases estándar y Lombok.

---

## Migraciones Flyway

La propiedad `spring.jpa.hibernate.ddl-auto` está configurada estrictamente en `none`: **todo cambio de esquema, tabla o catálogo debe realizarse mediante una migración Flyway**.

- **Ruta:** `src/main/resources/db/migration/`
- **Convención de Nombres:** `V<Mayor>.<Menor>.<Parche>__<accion>_<descripcion>.sql`
  - Ejemplos reales en PaseaPe:
    - `V2.0.4__crear_tabla_solicitud_paseo_descarte.sql`
    - `V2.0.5__agregar_hora_vencimiento_solicitud_paseo.sql`
- **Inmutabilidad:** Nunca edites ni renombres una migración que ya fue ejecutada en entornos compartidos o `main`. Si se requiere una corrección, crea una nueva migración con el siguiente número secuencial.

---

## No Hacer

```java
// ❌ Omitir extender Auditable en las entidades JPA
public class SolicitudPaseoEntity { ... } // Debe ser: extends Auditable

// ❌ Redeclarar campos de auditoría en la entidad si ya hereda de Auditable
@Column(name = "creado_el")
private LocalDateTime creadoEl; // Ya lo provee Auditable

// ❌ Retornar entidades JPA desde los puertos
SolicitudPaseoEntity findByUuid(String uuid); // en application/repository

// ❌ Retornar Optional desde los puertos
Optional<Paseador> findByUuid(String uuid); // en application/repository

// ❌ Colocar lógica de negocio en el adaptador
if (solicitud.getMontoTotal().compareTo(BigDecimal.ZERO) <= 0) { ... } // en SolicitudPaseoRepository

// ❌ Omitir @SQLRestriction("estado = 1") en tablas transaccionales
@Table(name = "solicitud_paseo")
public class SolicitudPaseoEntity extends Auditable { ... } // Falta @SQLRestriction
```
