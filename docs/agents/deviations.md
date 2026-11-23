# Desvíos Existentes: No Imitar

Este documento cataloga prácticas o implementaciones que existen en partes del código actual de **PaseaPe** (como tolerancia o deuda técnica de sprints anteriores) pero que **no son modelo a seguir para futuros desarrollos**.

No copies estas desviaciones como referencia en nuevo código, y no las refactorices fuera del alcance de la tarea asignada.

---

## Tabla de Desvíos y Alternativa Correcta

| Desvío en Código Existente | Dónde se Observa | Qué Hacer en Nuevos Desarrollos |
|---|---|---|
| **Controladores que extienden de `BaseController`** | `SolicitudPaseoClienteController`, `MascotaController`, etc. | **Estándar Mortgage:** Clase independiente sin heredar de ninguna clase base, con logger propio `LoggerFactory.getLogger(XController.class)` y manejo explícito de errores (`controllers.md`). |
| **Validadores que retornan `ErrorDetailDto` con retornos tempranos** | `SolicitudPaseoValidator`, `PaseadorValidator` | **Estándar Mortgage:** Retornar `ResponseDto<?>`, lanzar internamente `BadRequestException` y capturar en bloque `try/catch` delegando a `BuildErrorForValidation` (`validators.md`). |
| **Uso de Java `record`** | Ocasional en DTOs o clases auxiliares | Prohibido. Usar clases estándar (`class`) con Lombok (`@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`). |
| **Retorno de `Optional<T>` en contratos de repositorio** | Algunos repositorios de infraestructura o JPA | Prohibido en `application/repository`. Retornar siempre la entidad directa `T` o `null` si no existe, y colecciones `List<T>`. |
| **Anotaciones Jakarta en `*ReqDto` (`@NotNull`, `@Size`, etc.)** | DTOs de autenticación o primeros sprints | Prohibido en nuevos `*ReqDto`. Los DTOs de entrada son contenedores planos con `@JsonNaming`; toda validación reside en el `*Validator`. |
| **Exposición de claves numéricas primarias (`id`) hacia el frontend** | Vistas administrativas o DTOs legacy | Prohibido. Todo contrato público debe exponer e interactuar mediante `uuid` (Regla 11), reservando los IDs numéricos para la persistencia interna. |
| **Inyección directa de JPA en Services** | Servicios que inyectan `*JpaRepository` | Inyectar únicamente el puerto de dominio `IXRepository` (de `application/repository`). |
| **Lógica de negocio en Mappers MapStruct** | Métodos `@AfterMapping` con reglas o consultas | Mapeos directos campo a campo; si requiere datos de otro agregado, usar `ignore = true` y resolverlo en el Service. |
| **Entidades transaccionales sin `@SQLRestriction`** | Tablas de negocio que gestionan borrado lógico manual | Declarar siempre a nivel de clase `@SQLRestriction("estado = 1")` en entidades JPA de infraestructura (salvo catálogos). |
| **Códigos de error o roles hardcodeados** | Strings directos `"CLIENTE"`, `"400"` o `"00"` en servicios | Centralizar en `Constant.java`, `StatusCodes.java` y `MessageCodes.java`. |
| **Validación de fechas en zona horaria no controlada** | Uso de `LocalDate.now()` sin especificar `ZoneId` | Validar fechas civiles del negocio siempre con `ZoneId.of(Constant.ZONE_ID_LIMA)`. |
