# Arquitectura y Reglas del Backend - api-paseape

Este documento recopila el contexto técnico, la arquitectura de software, los estándares de diseño y las 11 reglas no negociables que rigen el desarrollo del backend del proyecto **api-paseape**.

---

## 1. Stack Tecnológico y Configuración del Entorno

| Componente | Tecnología / Versión | Detalle de Configuración |
| :--- | :--- | :--- |
| **Lenguaje** | Java 21 LTS | Compatible con características modernas de lenguaje, salvo restricciones explícitas del proyecto. |
| **Framework Base** | Spring Boot 3.x | Arquitectura modular basada en Spring Data JPA, Spring Security y Spring Web. |
| **Gestor de Construcción** | Gradle | Compilación mediante `./gradlew clean bootJar -x jar`. |
| **Persistencia** | Spring Data JPA / Hibernate 6 | Mapeo objeto-relacional optimizado, consultas JPQL y nativas según caso. |
| **Motor de Base de Datos** | MySQL 8 | Aiven Cloud Managed en producción; XAMPP/Docker en desarrollo local. |
| **Control de Versiones BD** | Flyway | Migraciones deterministas en `src/main/resources/db/migration/V<SemVer>__descripcion.sql`. |
| **Seguridad** | Spring Security (Stateless) + JJWT 0.12.x | Autenticación dual (Google Identity Services OAuth 2.0 y Local con BCrypt). |
| **Context Path Global** | `/api-paseape` | Configurado en `server.servlet.context-path=/api-paseape`. |
| **Estandarización Temporal** | UTC (JVM y BD) / America/Lima (Negocio) | Persistencia y JVM en UTC (`-Duser.timezone=UTC`, `serverTimezone=UTC`). Negocio civil en `America/Lima` (UTC-5). |
| **Integraciones Externas** | Brevo REST API v3 / Google Auth Verifier | Envío de correos transaccionales por HTTPS (puerto 443) con auditoría en `brevo_email`. |

---

## 2. Arquitectura de Software (Domain-Driven Design - DDD)

El proyecto implementa una arquitectura por capas desacoplada bajo los principios de DDD y Clean Architecture:

```
com.paseape.apipaseape
├── domain/                      # CAPA DE DOMINIO (Reglas de negocio puras)
│   ├── entity/                  # Modelos y entidades de dominio independientes de frameworks
│   └── (value objects)
├── application/                 # CAPA DE APLICACIÓN (Casos de uso y orquestación)
│   ├── service/                 # Servicios de negocio transaccionales
│   └── repository/              # Contratos/interfaces de repositorios puros (sin acoplamiento a JPA)
└── infrastructure/              # CAPA DE INFRAESTRUCTURA (Detalles técnicos y adaptadores)
    ├── controller/              # Endpoints REST (heredan de BaseController)
    ├── validator/               # Validadores dedicados (@Component) por módulo
    ├── dto/
    │   ├── request/             # DTOs de entrada (*ReqDto)
    │   └── response/            # DTOs de salida (*ResDto)
    ├── mapper/
    │   ├── entity/              # Mappers MapStruct (JPA Entity <-> Dominio)
    │   └── dto/                 # Mappers MapStruct (Dominio <-> DTO)
    ├── repository/              # Implementaciones de repositorios de dominio
    │   └── jpa/                 # Interfaces de Spring Data JPA (*JpaRepository)
    ├── entity/                  # Entidades JPA (@Entity, @Table, @SQLRestriction)
    ├── security/                # Filtros JWT, proveedor de tokens, seguridad web
    ├── constant/                # Constantes del sistema (Constant, MessageCodes, StatusCodes)
    └── exception/               # Manejador global (GlobalExceptionHandler) y excepciones de negocio
```

---

## 3. Las 11 Reglas Arquitectónicas y de Estilo (No Negociables)

### Regla 1: Prohibido el Uso de `record`
- Todas las clases, DTOs y entidades se deben modelar usando clases estándar (`public class ...`) con anotaciones de Lombok:
  - `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`.
- Esto garantiza uniformidad en la serialización, compatibilidad con mappers MapStruct y consistencia con las herramientas de introspección.

### Regla 2: Prohibido el Uso de `Optional` en Repositorios
- Los contratos de interfaces en `application.repository` retornan directamente la entidad `T` o `null` si no existe coincidencia, y colecciones `List<T>` (lista vacía si no hay resultados, nunca `null`).
- Queda prohibido retornar `Optional<T>`.
- Las comprobaciones de existencia o nulidad se realizan explícitamente en los Servicios:
  ```java
  Mascota mascota = mascotaRepository.findByUuid(mascotaUuid);
  if (mascota == null) {
      throw new BadRequestException("No existe una mascota registrada con el UUID: " + mascotaUuid);
  }
  ```

### Regla 3: Borrado Lógico Automático con `@SQLRestriction`
- Todas las entidades JPA de negocio y transaccionales (`*Entity.java`) deben declarar a nivel de clase:
  ```java
  @SQLRestriction("estado = 1")
  ```
- **Excepción:** Las entidades de catálogo / lookup tables (`TipoUsuario`, `DistritoLima`, `DiaSemana`, etc.) **NO** llevan `@SQLRestriction`, permitiendo su lectura integral sin filtros de estado de registro.

### Regla 4: Constructor Mono-Argumento en Entidades de Catálogo (Lookup Tables)
- Toda entidad de dominio correspondiente a un catálogo debe incluir un constructor que reciba únicamente su clave primaria `id`:
  ```java
  public TipoModalidadPaseo(Integer id) {
      this.id = id;
  }
  ```
- Al relacionar entidades en los servicios, si ya se dispone del ID o del catálogo predeterminado, no se consulta la base de datos; se instancia directamente:
  ```java
  solicitud.setTipoModalidadPaseo(new TipoModalidadPaseo(dto.getTipoModalidadPaseoId()));
  ```

### Regla 5: DTOs de Entrada (`*ReqDto`) Planos
- Los DTOs de entrada actúan exclusivamente como contenedores planos de datos.
- Anotaciones requeridas: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder` y `@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)`.
- **Prohibido colocar anotaciones de validación de Jakarta** (`@NotNull`, `@NotBlank`, `@Size`, `@Pattern`, `@Email`, etc.) en los `*ReqDto`.

### Regla 6: Clases Validadoras Dedicadas (`*Validator`)
- Toda validación de parámetros (nulidad, vacíos, formatos, rangos y comprobaciones cruzadas) reside en componentes `@Component` dentro de `infrastructure.validator`.
- El controlador inyecta el validador y lo ejecuta al inicio del endpoint:
  ```java
  ErrorDetailDto error = solicitudPaseoValidator.validarCreacion(reqDto);
  if (error != null) {
      return ResponseEntity.status(StatusCodes.BAD_REQUEST)
          .body(ResponseDto.error(StatusCodes.BAD_REQUEST, MessageCodes.ERROR_VALIDACION, error.getMensaje(), header));
  }
  ```
- Si hay inconsistencias, retorna inmediatamente la respuesta de error sin invocar la capa de servicio.

### Regla 7: Estructura y Nomenclatura Estándar de Respuesta HTTP (`ResponseDto<T>`)
Toda petición HTTP responde con la envoltura estándar:
- `statusCode`: Código numérico HTTP (200, 201, 400, 404, 500) usando `StatusCodes`.
- `code`: Código funcional de respuesta usando `MessageCodes` (ej. `"00"` para éxito, `"01"` para error de validación).
- `message`: Descripción legible de la operación.
- `header`: Metadatos de auditoría temporal generados con `RequestService.getResponseHeader(startDatetime, endDatetime)`.
- `response`: Payload DTO de datos (`*ResDto`) o `null` en caso de error.

### Regla 8: Cero Código Duro (Hardcoding)
- Prohibido el uso de literales mágicos, textos repetidos, códigos HTTP manuales o nombres de claims en el código.
- Todo valor debe provenir de:
  - `Constant.java`
  - `StatusCodes.java`
  - `MessageCodes.java`

### Regla 9: Transaccionalidad Explícita y Control de Rollback
- En la capa de Servicio (`application.service`):
  - **Operaciones de mutación/escritura:** `@Transactional(rollbackFor = Exception.class)`.
  - **Operaciones de solo lectura:** `@Transactional(readOnly = true)`.

### Regla 10: Mapeo Desacoplado con MapStruct
- Conversiones entre Entidades JPA y Dominio: interfaces en `infrastructure.mapper.entity.*DboMapper`.
- Conversiones entre Dominio y DTOs: interfaces en `infrastructure.mapper.dto.*DtoMapper`.
- Mantener mappings explícitos y desacoplados para evitar fugas de infraestructura hacia el dominio.

### Regla 11: Identificación Pública Estricta por UUID (Ofuscación de Claves Primarias)
- **DTOs de Respuesta (`*ResDto`):** Toda entidad expuesta hacia el frontend debe incluir obligatoriamente su campo `uuid` (ej. `uuid`, `mascota_uuid`, `paseador_uuid`, `solicitud_uuid`). Queda **prohibido exponer el `id` numérico autoincremental** de la base de datos en los contratos públicos.
- **DTOs de Entrada (`*ReqDto`) y Parámetros:** Cada vez que el frontend consulte el detalle de una entidad o solicite actualizar algún campo, deberá enviar el `uuid` correspondiente.
- **Repositorios y Servicios:** Las búsquedas de negocio externas se realizan exclusivamente con `findByUuid(String uuid)`. Si la entidad no existe, el servicio lanza de inmediato `BadRequestException`. Los `id` autoincrementales numéricos de MySQL quedan estrictamente reservados para el motor de base de datos y relaciones internas de clave foránea.
- **Validadores:** Cada validador comprueba que el `uuid` cumpla con el formato válido canónico de 36 caracteres y no sea nulo ni vacío.

---

## 4. Auditoría y Manejo de Errores

- **Excepciones de Negocio:** Se lanzan mediante `BadRequestException` gestionada por `GlobalExceptionHandler`.
- **Tiempos de Ejecución:** Cada petición registra `startDatetime` al inicio del método del controlador y `endDatetime` al finalizar, calculando `durationMs` en el `header` de `ResponseDto`.
- **Logs:** Trazabilidad clara de peticiones sin exponer datos sensibles ni credenciales en consola.
