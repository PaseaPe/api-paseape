# Arquitectura de api-paseape

Este documento establece las reglas y la estructura arquitectónica para el backend de **PaseaPe**. Rutas relativas a `src/main/java/com/paseape/apipaseape/`.

El repositorio está organizado **por capas** bajo los principios de Clean Architecture y Domain-Driven Design (DDD). Se mantiene esta distribución estricta: no muevas clases a estructuras por paquete de funcionalidad o módulos planos.

## Flujo Canónico (Estándar por Defecto)

```
XController → XValidator → XService → IXRepository → XRepository → IXJpaRepository + IXDboMapper
                                   ↘ IXDtoMapper (dominio → XResDto)
```

| Pieza | Carpeta | Referencia / Ejemplo |
|---|---|---|
| Controller | `infrastructure/controller/` | `infrastructure/controller/SolicitudPaseoClienteController.java` |
| Validator | `infrastructure/validator/` | `infrastructure/validator/SolicitudPaseoValidator.java` |
| Service | `application/service/` | `application/service/SolicitudPaseoService.java` |
| Puerto | `application/repository/` | `application/repository/ISolicitudPaseoRepository.java` |
| Adaptador | `infrastructure/repository/` | `infrastructure/repository/SolicitudPaseoRepository.java` |
| JPA | `infrastructure/repository/jpa/` | `infrastructure/repository/jpa/ISolicitudPaseoJpaRepository.java` |
| Mappers | `infrastructure/mapper/entity/` y `mapper/dto/` | `mapper/entity/ISolicitudPaseoDboMapper.java`, `mapper/dto/ISolicitudPaseoDtoMapper.java` |
| Entidad JPA | `infrastructure/entity/` | `infrastructure/entity/SolicitudPaseoEntity.java` |
| Dominio | `domain/entity/` | `domain/entity/SolicitudPaseo.java` |
| DTOs Request | `infrastructure/dto/request/` | `infrastructure/dto/request/CrearSolicitudInmediataReqDto.java` |
| DTOs Response | `infrastructure/dto/response/` | `infrastructure/dto/response/SolicitudPaseoResDto.java` |
| Constantes | `infrastructure/constant/` | `Constant.java`, `StatusCodes.java`, `MessageCodes.java` |
| Excepciones | `infrastructure/exception/` | `BadRequestException.java`, `GlobalExceptionHandler.java` |

---

## Qué Puede Importar Cada Capa

| Capa | Puede Usar | No Debe |
|---|---|---|
| `infrastructure/controller` | Validators dedicados, Services, DTOs, `RequestService`, constantes (`StatusCodes`, `MessageCodes`, `Constant`) | Inyectar `IXRepository`, `IXJpaRepository` ni mappers; contener reglas de negocio ni heredar de clases base en código nuevo |
| `infrastructure/validator` | Puertos `IXRepository`, DTOs de request, constantes de validación | Inyectar `IXJpaRepository`, Services ni otros validators; escribir en base de datos ni modificar el request |
| `application/service` | Puertos `IXRepository`, otros Services, `IXDtoMapper`, DTOs, dominio | Inyectar `IXJpaRepository`, clases concretas de `infrastructure/repository`, `XEntity`, `EntityManager` |
| `application/repository` | Dominio (`domain/entity`) | Exponer `XEntity` de JPA ni retornar `Optional<T>` (retorna `T` o `null`, `List<T>`) |
| `infrastructure/repository` | `IXJpaRepository`, `IXDboMapper`, `XEntity` | Contener lógica de negocio o llamar a Services |
| `domain` | Java estándar y Lombok (`@Getter`, `@Setter`, etc.) | Depender de Spring, JPA, Hibernate o DTOs de `infrastructure` |

---

## Transacciones

La frontera transaccional reside **exclusivamente en los métodos públicos de la capa de Servicio (`application/service`)**:
- **Operaciones de mutación/escritura:** `@Transactional(rollbackFor = Exception.class)`.
- **Operaciones de solo lectura:** `@Transactional(readOnly = true)`.

La anotación transaccional se coloca en el método público invocado por el controller, nunca en métodos privados ni en controladores.

---

## Identificación Pública Estricta por UUID (Regla 11)

- **DTOs de Entrada (`*ReqDto`) y Parámetros:** Toda consulta puntual o mutación hace referencia a las entidades de negocio mediante su `uuid` canónico (String de 36 caracteres: `mascota_uuid`, `paseador_uuid`, `solicitud_uuid`).
- **DTOs de Respuesta (`*ResDto`):** Queda estrictamente prohibido exponer las claves primarias autoincrementales numéricas de la base de datos hacia el cliente externo (salvo tablas catálogo / lookup tables con IDs fijos documentados).
- **Consultas de Negocio:** Se ejecutan mediante `findByUuid(String uuid)`. Si el registro no existe, el servicio lanza de inmediato `BadRequestException`.

---

## Nombres y Sufijos de Clases

| Pieza | Patrón | Ejemplo en PaseaPe |
|---|---|---|
| Controller | `XController` | `SolicitudPaseoClienteController`, `PaseadorController` |
| Validator | `XValidator`, método `validateY` | `SolicitudPaseoValidator.validateCrearInmediata` |
| Service | `XService` concreto, sin interfaz ni `Impl` | `SolicitudPaseoService`, `DisponibilidadPaseadorService` |
| Puerto / Adaptador / JPA | `IXRepository` / `XRepository` / `IXJpaRepository` | `ISolicitudPaseoRepository` / `SolicitudPaseoRepository` / `ISolicitudPaseoJpaRepository` |
| Mappers | `IXDboMapper` (`toDbo`/`toDomain`), `IXDtoMapper` (`toResDto`) | `ISolicitudPaseoDboMapper`, `ISolicitudPaseoDtoMapper` |
| Entidad JPA | `XEntity` | `SolicitudPaseoEntity`, `MascotaEntity` |
| Dominio | `X` (sustantivo singular) | `SolicitudPaseo`, `Mascota`, `Paseador` |
| DTOs | `XReqDto` / `XResDto` | `CrearSolicitudInmediataReqDto`, `SolicitudPaseoResDto` |
