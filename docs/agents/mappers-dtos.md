# Mappers y DTOs

## Mappers (MapStruct)

En PaseaPe existen dos mappers por cada agregado, ambos definidos como interfaces MapStruct:

| Mapper | Carpeta | Convierte | Lo Usa |
|---|---|---|---|
| `IXDboMapper` | `infrastructure/mapper/entity/` | Dominio ↔ `XEntity` (`toDbo`, `toDomain`) | Solo el adaptador `XRepository` |
| `IXDtoMapper` | `infrastructure/mapper/dto/` | Dominio → `XResDto` (`toResDto`) | La capa de Servicio |

```java
package com.paseape.apipaseape.infrastructure.mapper.entity;

import com.paseape.apipaseape.domain.entity.SolicitudPaseo;
import com.paseape.apipaseape.infrastructure.entity.SolicitudPaseoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ISolicitudPaseoDboMapper {
    SolicitudPaseoEntity toDbo(SolicitudPaseo domain);
    SolicitudPaseo toDomain(SolicitudPaseoEntity entity);
    List<SolicitudPaseo> toDomain(List<SolicitudPaseoEntity> entityList);
}
```

```java
package com.paseape.apipaseape.infrastructure.mapper.dto;

import com.paseape.apipaseape.domain.entity.SolicitudPaseo;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudPaseoResDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ISolicitudPaseoDtoMapper {

    @Mapping(target = "clienteUuid", source = "cliente.uuid")
    @Mapping(target = "paseadorUuid", source = "paseador.uuid")
    @Mapping(target = "mascotas", ignore = true)
    SolicitudPaseoResDto toResDto(SolicitudPaseo domain);

    List<SolicitudPaseoResDto> toResDtoList(List<SolicitudPaseo> domainList);
}
```

- `@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)` se declara siempre para permitir inyección por constructor.
- **Cero lógica dentro del mapper:** Únicamente declaraciones `@Mapping` para transformaciones de propiedades con nombres dispares e `ignore = true` cuando el servicio ensambla colecciones adicionales o relaciones N:M. Prohibido colocar métodos `@AfterMapping` con reglas de negocio o consultas a repositorios.

---

## DTOs (Data Transfer Objects)

- **Prohibido el uso de `record`:** Todos los DTOs se construyen como clases estándar con Lombok:
  - `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.
- **Estrategia de Nombres JSON:** Contratos públicos en `snake_case` mediante:
  ```java
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  ```
  Los atributos en Java se escriben en `camelCase` estándar y Spring los serializa/deserializa automáticamente a `snake_case` (ej. `codigoReserva` ↔ `codigo_reserva`, `tarifaHoraPen` ↔ `tarifa_hora_pen`).
- **DTOs de Entrada (`*ReqDto`) Planos (Regla 5):**
  - Queda **prohibido colocar anotaciones de validación de Jakarta** (`@NotNull`, `@NotBlank`, `@Size`, `@Pattern`, `@Email`, etc.) en los `*ReqDto`.
  - Actúan como contenedores puros de datos deserializados. Toda validación de estructura, formatos y obligatoriedad reside en el `*Validator`.
- **Identificación Pública Exclusiva por UUID (Regla 11):**
  - Todo `*ResDto` y `*ReqDto` expone e intercambia `uuid` (String canónico de 36 caracteres).
  - Queda **estrictamente prohibido exponer IDs numéricos autoincrementales** de base de datos hacia el frontend o la aplicación móvil (la única excepción son los IDs enteros de tablas de catálogo lookup fijas como `distrito_id`, `tipo_metodo_pago_id`, `tipo_duracion_paseo_id`).
- **Envoltura Estándar:** Toda respuesta hacia el cliente HTTP se envuelve en `ResponseDto<T>`:
  - `statusCode`: Código HTTP numérico (200, 201, 400, 404, 500) usando `StatusCodes`.
  - `code`: Código funcional de respuesta ("00", "01", etc.) usando `MessageCodes`.
  - `message`: Mensaje descriptivo en español legible por el usuario final.
  - `header`: Metadatos de auditoría temporal (`HeaderDto` con `startDatetime`, `endDatetime`, `durationMs`).
  - `response`: Payload del DTO de datos (`*ResDto`) o `null`.

---

## Entidades de Dominio

- Modeladas como POJOs puros con Lombok en `com.paseape.apipaseape.domain.entity`.
- **Totalmente independientes de frameworks:** No llevan anotaciones `@Entity`, `@Table`, `@Column`, ni importan nada de JPA, Hibernate o Spring.
- Los catálogos o entidades lookup tables incluyen un constructor mono-argumento que recibe el `id`:
  ```java
  public TipoModalidadPaseo(Integer id) {
      this.id = id;
  }
  ```
