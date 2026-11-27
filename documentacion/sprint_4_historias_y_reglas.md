# Especificaciones y Reglas de Negocio - SPRINT 4 (api-paseape)

Este documento detalla los requerimientos funcionales, el modelo físico de persistencia (`V2.3.0`), las reglas de negocio y la arquitectura técnica para las Historias de Usuario del **SPRINT 4** del backend **api-paseape**.

---

## 1. Alcance y Estado General del Sprint 4

El Sprint 4 consolida dos frentes principales en la plataforma **PaseaPe**:
1. **Administración de Catálogos del Sistema (Implementado):** CRUD completo, borrado lógico (`estado = 0`) y reactivación (`estado = 1`) de los 6 catálogos maestros administrables por el rol `ADMINISTRADOR`.
2. **Reputación, Moderación, Sanciones y Penalidades Automáticas (Pendiente de Desarrollo):** Calificación con estrellas (HU32), opiniones de servicio (HU33), panel de reputación del paseador (HU34), gestión administrativa de reportes/reclamos (HU37), bloqueo temporal/permanente de cuentas (HU40) y motor automático de penalidades.

| Módulo / Historia | Descripción | Estado |
| :--- | :--- | :--- |
| **HU41 – HU44 (Catálogos)** | Mantenedor administrativo de catálogos (listar, crear, actualizar, desactivar y reactivar) | **IMPLEMENTADO Y VERIFICADO** |
| **HU32** | Calificar al paseador después del servicio (1 a 5 estrellas) | **IMPLEMENTADO Y VERIFICADO** |
| **HU33** | Escribir una opinión sobre el servicio | **IMPLEMENTADO Y VERIFICADO** |
| **HU34** | Consultar el promedio de calificaciones y opiniones como paseador | **IMPLEMENTADO Y VERIFICADO** |
| **HU37** | Revisar y gestionar reportes y reclamos de usuarios (Admin) | **PENDIENTE** |
| **HU40** | Bloquear temporal o permanentemente cuentas que incumplan normas (Admin) | **PENDIENTE** |
| **Motor de Penalidades** | Detección automática de faltas operativas y escalamiento a sanciones | **PENDIENTE** |

---

## 2. Modelo Físico de Base de Datos (`V2.3.0`)

Migración Flyway creada en `src/main/resources/db/migration/V2.3.0__sprint_4_reputacion_reportes_sanciones_schema.sql`. Cumple con `docs/agents/persistence.md`: `id BIGINT/INT`, `uuid CHAR(36) NOT NULL UNIQUE`, auditoría `Auditable` (`creado_el`, `actualizado_el`, `creado_por`, `actualizado_por`), borrado lógico `estado BIT NOT NULL DEFAULT 1` y `@SQLRestriction("estado = 1")` en entidades transaccionales.

### 2.1. Ajustes a Tablas y Catálogos Existentes
* **`paseadores`**: Incorpora `total_calificaciones INT NOT NULL DEFAULT 0` y `suma_calificaciones INT NOT NULL DEFAULT 0` junto a `calificacion_promedio DECIMAL(3,2)` para consultas $O(1)$ en HU34.
* **`digital_document_type`**: Agrega `5: EVIDENCIA_REPORTE` para adjuntar evidencias en reportes (HU37) reutilizando `folder_digital_document`.
* **`tipo_notificacion`**: Agrega `7: CALIFICACION_RECIBIDA`, `8: REPORTE_RECIBIDO`, `9: REPORTE_ACTUALIZADO`, `10: PENALIDAD_APLICADA`, `11: CUENTA_SANCIONADA`.

### 2.2. Nuevas Tablas del Sprint 4
* **Catálogos (`INT`):**
  * `categoria_reporte`: `MALTRATO_O_RIESGO_MASCOTA` (1), `INCUMPLIMIENTO_HORARIO` (2), `PASEO_INCOMPLETO` (3), `COMPORTAMIENTO_INAPROPIADO` (4), `PROBLEMA_PAGO` (5), `OTRO` (6).
  * `estado_reporte`: `PENDIENTE` (1), `EN_REVISION` (2), `RESUELTO` (3, terminal), `DESESTIMADO` (4, terminal).
  * `tipo_penalidad`: Catálogo maestro de reglas de infracción (`codigo`, `rol_aplicable`, `gravedad`, `puntos`, `dias_vigencia_puntos`, `dispara_sancion_inmediata`, `horas_suspension_inmediata`).
* **Transaccionales (`BIGINT`):**
  * `paseo_calificacion`: Relación 1:1 con `solicitud_paseo` (`UNIQUE solicitud_paseo_id`), `puntuacion TINYINT` (`CHECK BETWEEN 1 AND 5`), `comentario TEXT NULL`, `fecha_comentario`, `visible BIT`.
  * `reporte_incidencia`: Reportes con `codigo_reporte`, `solicitud_paseo_id` (nullable), `usuario_reportante_id`, `usuario_reportado_id`, `categoria_reporte_id`, `estado_reporte_id`, `administrador_asignado_id`, `folder_digital_document_id` (evidencias).
  * `reporte_seguimiento`: Bitácora inmutable de cambios de estado y notas internas del administrador.
  * `usuario_sancion`: Historial de bloqueos (`TEMPORAL` | `PERMANENTE`), origen (`MANUAL_ADMIN` | `AUTOMATICO_PENALIDAD`), `fecha_inicio`, `fecha_fin`, `activa BIT` y campos de levantamiento anticipado.
  * `usuario_penalidad`: Ledger de infracciones con `puntos_aplicados`, `fecha_expiracion_puntos`, `detalle_evidencia`, vínculo opcional a `usuario_sancion_id` y soporte de anulación administrativa (`anulada BIT`).

### 2.3. Catálogo de Motivos de Cancelación (`V2.3.1`)
* Migración creada en `src/main/resources/db/migration/V2.3.1__tipo_motivo_cancelacion_schema.sql`.
* **`tipo_motivo_cancelacion` (`INT`):** Catálogo maestro para el combo de cancelación en la app móvil (`id`, `uuid`, `descripcion`, `rol_aplicable` [`CLIENTE` | `PASEADOR` | `AMBOS`], `tipo_penalidad_id` nullable hacia `tipo_penalidad`, `requiere_comentario BIT`).
  * **Semillas Paseador:** `1`: Cliente no entregó la mascota (`NO_SHOW_CLIENTE` [4]), `2`: Emergencia personal (`CANCELACION_TARDIA_PASEADOR` [1]), `3`: Fuerza mayor / clima extremo (`NULL`).
  * **Semillas Cliente:** `4`: Paseador no responde o tiene retraso (`NULL`), `5`: Cambio de planes (`CANCELACION_TARDIA_CLIENTE` [2]), `6`: Error al seleccionar fecha/hora/mascotas (`CANCELACION_TARDIA_CLIENTE` [2]).

---

## 3. Historias de Usuario Implementadas: Mantenedor de Catálogos

> **Estado:** **IMPLEMENTADO Y VERIFICADO**  
> **Alcance:** Gestión administrativa de los 6 catálogos configurables: `distritos` (`DistritoLima`), `tipos-mascota` (`TipoMascota`), `razas` (`TipoRaza`), `tamanos-mascota` (`TipoTamanoMascota`), `niveles-energia` (`TipoNivelEnergia`) y `generos-mascota` (`TipoGeneroMascota`).  
> **Exclusiones:** Se excluyen deliberadamente `TipoDocumento` y `TipoMetodoPago` por ser catálogos estructurales ligados a integraciones y reglas fijas.

### A. HU41 – Consultar catálogos administrables
* **Rol:** Administrador | **Estado:** **IMPLEMENTADO Y VERIFICADO**
* **Descripción:** Como administrador de la plataforma, quiero consultar los ítems de los catálogos maestros filtrando opcionalmente por estado activo o inactivo, para auditar y mantener actualizados los valores operativos del sistema.
* **Reglas de Negocio:**
  * Todas las rutas `/catalogos/**` requieren JWT autenticado (regla general en `SecurityConfig`).
  * `CatalogoValidator.validateConsultaCatalogo` valida que el usuario autenticado esté activo (`Constant.ID_ACTIVO`) y tenga rol `ADMINISTRADOR` (`Constant.ID_ADMINISTRADOR`).
  * El parámetro opcional `?estado=` admite únicamente `0` (inactivo) o `1` (activo). Si se omite, retorna todos los registros.
  * `CatalogoService` convierte `creado_el` y `actualizado_el` de UTC a `America/Lima` mediante `UtilMethods.convertirUtcALima(...)`.

### B. HU42 – Crear nuevos registros en catálogos administrables
* **Rol:** Administrador | **Estado:** **IMPLEMENTADO Y VERIFICADO**
* **Descripción:** Como administrador de la plataforma, quiero registrar nuevos valores en los catálogos maestros (distritos, tipos de mascota, razas, tamaños, niveles de energía y géneros), para ampliar la cobertura y opciones disponibles en la aplicación.
* **Reglas de Negocio:**
  * Valida obligatoriedad y longitud máxima de `descripcion` (30 para géneros, 50 para tipos/tamaños/niveles, 80 para razas, 100 para distritos).
  * Valida unicidad de `descripcion` (y de `codigo_ubigeo` en distritos, máx. 10 caracteres).
  * Para **razas (`TipoRazaReqDto`)**, exige `tipo_mascota_uuid` válido, existente y activo (`estado = 1`), y valida que no exista otra raza con la misma descripción dentro de ese tipo de mascota.
  * Genera `uuid` v4, asigna `estado = 1` (`Constant.ESTADO_LOGICO_ACTIVO`) y retorna HTTP `201`.

### C. HU43 – Actualizar registros de catálogos administrables
* **Rol:** Administrador | **Estado:** **IMPLEMENTADO Y VERIFICADO**
* **Descripción:** Como administrador de la plataforma, quiero editar la descripción y atributos complementarios de un ítem de catálogo existente, para corregir textos o mantener actualizada la información sin alterar su identificador público.
* **Reglas de Negocio:**
  * Identificación obligatoria por `uuid` en la ruta (`PUT /catalogos/{recurso}/{uuid}`).
  * Verifica existencia previa y valida que la nueva `descripcion` (o `codigo_ubigeo` en distritos) no colisione con otro registro distinto al editado (`!duplicado.getId().equals(existente.getId())`).
  * En `tamanos-mascota`, permite actualizar `rango_peso_ref` (máx. 50 caracteres).

### D. HU44 – Desactivar (borrado lógico) y reactivar ítems de catálogos
* **Rol:** Administrador | **Estado:** **IMPLEMENTADO Y VERIFICADO**
* **Descripción:** Como administrador de la plataforma, quiero desactivar lógicamente ítems de catálogo que ya no deban mostrarse y reactivarlos cuando sea necesario, para preservar la integridad referencial histórica de mascotas y solicitudes existentes.
* **Reglas de Negocio:**
  * `DELETE /catalogos/{recurso}/{uuid}` fija `estado = 0` (`Constant.ESTADO_LOGICO_INACTIVO`).
  * `PATCH /catalogos/{recurso}/{uuid}/activar` fija `estado = 1` (`Constant.ESTADO_LOGICO_ACTIVO`).
  * Evita transiciones redundantes: si el ítem ya tiene el estado solicitado, lanza `BadRequestException` (`ResponseCodeBR03`).

### E. Trazabilidad Técnica Implementada (Catálogos)
* **Controlador:** `infrastructure/controller/CatalogoController.java` (`/catalogos/*`, controlador independiente sin `BaseController`, patrón `ejecutarEndpoint`).
* **Validador:** `infrastructure/validator/CatalogoValidator.java` ( `try/catch` + `BuildErrorForValidation`).
* **Servicio:** `application/service/CatalogoService.java`.
* **Mapper:** `infrastructure/mapper/dto/ICatalogoDtoMapper.java`.
* **DTOs:** `CatalogoBaseReqDto`, `DistritoLimaReqDto`, `TipoRazaReqDto`, `TipoTamanoMascotaReqDto` | `CatalogoAdminResDto`, `DistritoLimaAdminResDto`, `TipoRazaAdminResDto`, `TipoTamanoMascotaAdminResDto`.

| Recurso (`/catalogos/...`) | `GET` (Listar) | `POST` (Crear) | `PUT /{uuid}` (Editar) | `DELETE /{uuid}` (Desactivar) | `PATCH /{uuid}/activar` (Reactivar) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| `distritos` | ✓ | ✓ | ✓ | ✓ | ✓ |
| `tipos-mascota` | ✓ | ✓ | ✓ | ✓ | ✓ |
| `razas` | ✓ | ✓ | ✓ | ✓ | ✓ |
| `tamanos-mascota` | ✓ | ✓ | ✓ | ✓ | ✓ |
| `niveles-energia` | ✓ | ✓ | ✓ | ✓ | ✓ |
| `generos-mascota` | ✓ | ✓ | ✓ | ✓ | ✓ |

---

## 4. Historias de Usuario Pendientes de Desarrollo (Sprint 4)

### A. HU32 – Calificar al paseador después del servicio
* **Identificador:** HU32 | **Rol:** Cliente (Dueño) | **Estimación:** 1 día | **Prioridad:** 42 | **Puntos:** 2
* **Dependencias:** HU28 (Finalizar paseo), HU30 (Historial cliente)
* **Estado:** **IMPLEMENTADO Y VERIFICADO**

#### Criterios de Aceptación
1. **Escenario 1 – Calificación de un servicio completado:** Dado que el dueño tiene un paseo en estado `FINALIZADA` (6), cuando envíe una puntuación entera entre `1` y `5`, el sistema guarda la calificación en `paseo_calificacion` asociada al servicio, cliente y paseador, y recalcula el promedio del paseador.
2. **Escenario 2 – Intento de calificar un servicio no completado:** Dado que el paseo está en `PENDIENTE`, `SOLICITADA_BUSCANDO`, `ACEPTADA`, `EN_CURSO`, `CANCELADA`, `RECHAZADA` o `VENCIDA`, el sistema rechaza la operación con `BadRequestException`.

#### Diseño de Implementación Previsto
* **Endpoint:** `POST /api-paseape/calificaciones/solicitudes/{uuid}`
* **DTOs:** `RegistrarCalificacionReqDto` (`puntuacion`, `comentario` opcional) → `CalificacionResDto` (`uuid`, `solicitud_paseo_uuid`, `puntuacion`, `comentario`, `fecha_comentario`, `creado_el`).
* **Validador (`CalificacionValidator.validateRegistrarCalificacion`):**
  * Verifica sesión activa con rol `CLIENTE`, formato UUID v4 de `solicitud_uuid`, `puntuacion` no nula y en rango `[1, 5]`; si se envía `comentario` no vacío tras `trim()`, valida longitud entre **5** y **1000** caracteres.
* **Servicio (`CalificacionService.registrarCalificacion` con `@Transactional(rollbackFor = Exception.class)`):**
  * Verifica existencia de `SolicitudPaseo` y titularidad del cliente autenticado.
  * Exige `solicitudPaseoEstado.id == Constant.ID_ESTADO_SOLICITUD_FINALIZADA` (6).
  * Valida que no exista registro previo en `paseo_calificacion` para esa solicitud (`findBySolicitudPaseoId == null`).
  * Persiste `PaseoCalificacion` con `uuid = UUID.randomUUID().toString()`.
  * Actualiza atómicamente en `Paseador`: `total_calificaciones += 1`, `suma_calificaciones += puntuacion` y `calificacion_promedio = suma / total` (`RoundingMode.HALF_UP`, escala 2).
  * Registra notificación interna `CALIFICACION_RECIBIDA` (ID 7) dirigida al usuario del paseador.

---

### B. HU33 – Escribir una opinión sobre el servicio
* **Identificador:** HU33 | **Rol:** Cliente (Dueño) | **Estimación:** 1 día | **Prioridad:** 39 | **Puntos:** 2
* **Dependencias:** HU28, HU32
* **Estado:** **IMPLEMENTADO Y VERIFICADO**

#### Criterios de Aceptación
1. **Escenario 1 – Registro de una opinión:** Dado que el dueño tiene un paseo `FINALIZADA` (6) y ya registró su calificación en HU32, cuando envíe su comentario, el sistema lo persiste en `paseo_calificacion.comentario` junto a `fecha_comentario = UTC_NOW()`.
2. **Escenario 2 – Opinión vacía:** Dado que el dueño intenta enviar un comentario nulo, vacío o solo espacios en blanco, el sistema rechaza la petición solicitando el contenido requerido.

#### Diseño de Implementación Previsto
* **Endpoint unificado:** `POST /api-paseape/calificaciones/solicitudes/{uuid}` (integrado en HU32).
* **DTOs:** `RegistrarCalificacionReqDto` (`puntuacion`, `comentario` opcional) → `CalificacionResDto`.
* **Validador (`CalificacionValidator.validateRegistrarCalificacion`):**
  * Si se envía `comentario` no vacío tras `trim()`, valida longitud mínima **5** y máxima **1000** caracteres.
* **Servicio (`CalificacionService.registrarCalificacion`):**
  * Si se envía `comentario` válido, persiste `comentario.trim()` junto a `fecha_comentario = LocalDateTime.now(ZoneOffset.UTC)` en el mismo registro de calificación.

---

### C. HU34 – Consultar el promedio de calificaciones como paseador
* **Identificador:** HU34 | **Rol:** Paseador | **Estimación:** 1 día | **Prioridad:** 33 | **Puntos:** 2
* **Dependencias:** HU32, HU33
* **Estado:** **IMPLEMENTADO Y VERIFICADO**

#### Criterios de Aceptación
1. **Escenario 1 – Visualización del promedio:** Dado que el paseador tiene 1 o más calificaciones, cuando consulte su panel de reputación, el sistema muestra `calificacion_promedio`, `total_calificaciones` y la lista paginada de opiniones visibles ordenadas de la más reciente a la más antigua.
2. **Escenario 2 – Paseador sin calificaciones:** Dado que `total_calificaciones == 0`, el sistema retorna `tiene_calificaciones = false`, `calificacion_promedio = 0.00`, lista vacía de opiniones y mensaje informativo indicando que aún no cuenta con puntuaciones.

#### Diseño de Implementación Previsto
* **Endpoints:**
  * `GET /api-paseape/calificaciones/paseador?page=0&size=10` (panel privado del paseador autenticado).
  * `GET /api-paseape/calificaciones/paseadores/{uuid}?page=0&size=10` (consulta pública del perfil del paseador para dueños).
* **DTOs:** `ResumenCalificacionesPaseadorResDto` (`paseador_uuid`, `calificacion_promedio`, `total_calificaciones`, `tiene_calificaciones`, `mensaje_estado`, `opiniones: PageResponseDto<OpinionPaseadorResDto>`).
* **Servicio (`CalificacionService.obtenerCalificacionesPaseador` con `@Transactional(readOnly = true)`):**
  * Lee `calificacion_promedio` y `total_calificaciones` directamente de `Paseador` ($O(1)$).
  * Consulta paginada sobre `paseo_calificacion` filtrando `paseador_id`, `visible = true`, `estado = 1`, ordenado por `creado_el DESC`, convirtiendo fechas a `America/Lima`.

---

### D. HU37 – Revisar reportes y reclamos de usuarios
* **Identificador:** HU37 | **Rol:** Administrador | **Estimación:** 1 día | **Prioridad:** 61 | **Puntos:** 2
* **Dependencias:** HU35, HU36 (creación de reportes por cliente y paseador)
* **Estado:** **PENDIENTE**

#### Criterios de Aceptación
1. **Escenario 1 – Visualización de reportes:** Dado que existen reportes registrados, cuando el administrador acceda a la bandeja, el sistema muestra una lista paginada con `codigo_reporte`, fecha (`creado_el` en Lima), categoría, prioridad sugerida y estado, con filtros por estado, categoría y rango de fechas.
2. **Escenario 2 – Consulta de los detalles de un reporte:** Dado que el administrador selecciona un reporte por `uuid`, cuando tenga autorización de rol `ADMINISTRADOR`, el sistema muestra la descripción completa, el usuario reportante, el usuario reportado, los datos de la `solicitud_paseo` asociada (si aplica), evidencias adjuntas y el historial de `reporte_seguimiento`.

#### Diseño de Implementación Previsto
* **Endpoints:**
  * *Base HU35/HU36 (creación por usuario):* `POST /api-paseape/reportes` (`multipart/form-data`): recibe los datos del reporte (`solicitud_paseo_uuid` opcional, `usuario_reportado_uuid` opcional, `categoria_reporte_id`, `titulo`, `descripcion`) junto con las fotos/capturas de evidencia en la misma petición. Si se adjuntan archivos, crea el `folder_digital_document`, los sube a OCI Object Storage y registra cada `digital_document` con tipo `5: EVIDENCIA_REPORTE`. En el frontend, el combo opcional de paseo relacionado lista todos los paseos del usuario mostrando el formato `codigo_reserva (folio) - fecha - estado`.
  * *Notificación al crear reporte:* Al persistirse el reporte, se registra la notificación interna `REPORTE_RECIBIDO` (ID `8`) **tanto para el usuario reportante** (confirmación de recepción) **como para los administradores activos** (alerta operativa en bandeja).
  * *HU37 (gestión Admin — accesible desde el menú lateral vertical del botón hamburguesa):*
    * `GET /api-paseape/admin/reportes?estado_id=&categoria_id=&page=0&size=10`
    * `GET /api-paseape/admin/reportes/{uuid}`
    * `PATCH /api-paseape/admin/reportes/{uuid}/gestionar` (asignación de administrador, cambio de estado `EN_REVISION` / `RESUELTO` / `DESESTIMADO`, `comentario_interno`, `resolucion_resumen`, `visible_usuario` y opción de disparar penalidad grave).
* **Seguridad y Validación (`ReporteValidator`):**
  * Valida estrictamente que el usuario autenticado sea `ADMINISTRADOR` activo (`Constant.ID_ADMINISTRADOR`), siguiendo el mismo patrón de `CatalogoValidator` (sin abrir `/admin/**` para reportes: usar rutas bajo `/admin/reportes/**` ajustando `SecurityConfig` o validando JWT).
  * **Nota crítica de `SecurityConfig`:** Actualmente `SecurityConfig` tiene `.requestMatchers("/admin/**").permitAll()` para el registro de administradores. Para las rutas administrativas protegidas del Sprint 4 (`/admin/reportes/**`, `/admin/usuarios/**`, `/admin/sanciones/**`), se debe **restringir `SecurityConfig`** para permitir solo `POST /admin` (o `/admin/register`) como `permitAll()` y exigir `.authenticated()` para el resto de `/admin/**`.
* **Servicio (`ReporteService`):**
  * En cada transición en HU37, inserta una fila inmutable en `reporte_seguimiento` y, si `estado_nuevo` es `RESUELTO` o `DESESTIMADO`, fija `fecha_resolucion` y envía notificación interna `REPORTE_ACTUALIZADO` (ID 9) al reportante.

---

### E. HU40 – Bloquear cuentas que incumplan las normas
* **Identificador:** HU40 | **Rol:** Administrador | **Estimación:** 1 día | **Prioridad:** 52 | **Puntos:** 2
* **Dependencias:** HU35, HU37
* **Estado:** **PENDIENTE**

#### Criterios de Aceptación
1. **Escenario 1 – Bloqueo de una cuenta:** Dado que el administrador identificó un incumplimiento, cuando seleccione bloquear una cuenta e indique el motivo (obligatorio), el sistema registra la sanción en `usuario_sancion`, actualiza `usuarios.usuario_estado_id` (`4: SUSPENDIDO` si es temporal o `5: BLOQUEADO` si es permanente) y registra la auditoría administrativa.
2. **Escenario 2 – Bloqueo temporal:** Dado que el administrador aplica un bloqueo temporal indicando la duración (ej. en horas o días, con `fecha_fin > UTC_NOW()`), el sistema restringe el acceso del usuario durante ese periodo y lo rehabilita automáticamente al vencer el plazo.

#### Diseño de Implementación Previsto
* **Endpoints:**
  * *Admin (HU40):*
    * `POST /api-paseape/admin/usuarios/{uuid}/sanciones` (aplicar bloqueo `TEMPORAL` o `PERMANENTE`).
    * `PATCH /api-paseape/admin/sanciones/{uuid}/levantar` (levantamiento anticipado con `motivo_levantamiento`).
    * `GET /api-paseape/admin/usuarios/{uuid}/sanciones` (historial de sanciones y penalidades del usuario).
  * *Usuario autenticado (Cliente y Paseador — Transparencia de Cuenta):*
    * `GET /api-paseape/mi-cuenta/estado-sanciones`: expone los puntos vigentes acumulados, el detalle de penalidades activas con su fecha de vencimiento y la sanción activa (si existe), junto al umbral vigente para alimentar la vista `SCR_S4_08_MiEstadoCuenta_SancionesPuntos` y el modal informativo amigable.
* **Reglas de Negocio (`SancionService` + `SancionValidator`):**
  * Solo `ADMINISTRADOR` activo puede sancionar; no puede sancionarse a sí mismo ni a un `superadmin`.
  * `tipo_sancion = TEMPORAL` exige `duracion_horas > 0` (calculando `fecha_fin = fecha_inicio.plusHours(duracionHoras)` en UTC); `PERMANENTE` fija `fecha_fin = null`.
  * Evita duplicar sanciones activas del mismo tipo; al aplicar una sanción permanente, cierra sanciones temporales previas.
  * **Bloqueo inmediato de sesión:** `JwtAuthenticationFilter` / `CustomUserDetailsService` debe verificar `usuario.getUsuarioEstado().getId() == Constant.ID_ACTIVO` en cada request autenticado (y en login) para rechazar con HTTP 401/403 si la cuenta pasó a `SUSPENDIDO` o `BLOQUEADO`.
  * **Desbloqueo automático (`AppTaskScheduler`):** Tarea programada periódica (cada 5 minutos) que consulta `usuario_sancion` con `tipo_sancion = 'TEMPORAL'`, `activa = 1` y `fecha_fin <= UTC_NOW()`, marca `activa = 0` y restaura `usuarios.usuario_estado_id = 1 (ACTIVO)` si no tiene otras sanciones activas.

---

## 5. Motor de Penalidades Automáticas y Casuísticas (Pendiente)

Implementado de forma desacoplada en `PenalidadService` y disparado desde `SolicitudPaseoService`, `ReporteService` y `AppTaskScheduler`:

| Código (`tipo_penalidad`) | Actor | Disparador y Umbral | Puntos | Vigencia |
| :--- | :--- | :--- | :---: | :---: |
| `CANCELACION_TARDIA_PASEADOR` | Paseador | Cancela `ACEPTADA` [2] con motivo atribuible al paseador y `< 2h` antes de `hora_inicio` (programado) o `> 3 min` tras `fecha_hora_aceptada` (inmediato) | **2** | 30 días |
| `CANCELACION_TARDIA_CLIENTE` | Cliente | Cancela `ACEPTADA` [2] con motivo atribuible al cliente y `< 1h` antes de `hora_inicio` (programado) o `> 3 min` tras `fecha_hora_aceptada` (inmediato) | **1** | 30 días |
| `NO_SHOW_PASEADOR` | Paseador | Job en `AppTaskScheduler`: `ACEPTADA` [2] sin `fecha_hora_inicio_real` tras `+15 min` de `hora_inicio` (programado) o `+25 min` de `fecha_hora_aceptada` (inmediato) | **3** | 30 días |
| `NO_SHOW_CLIENTE` | Cliente | El paseador cancela en `PATCH /paseador/solicitudes/{uuid}/cancelar` seleccionando el motivo de combo correspondiente a ausencia/no entrega de mascota del cliente (+15 min de espera) | **2** | 30 días |
| `FINALIZACION_ANTICIPADA` | Paseador | En `finalizarPaseo` (HU28): `duracion_segundos < 80%` de `tipo_duracion_paseo.minutos * 60` | **2** | 30 días |
| `TELEMETRIA_INSUFICIENTE` | Paseador | En `finalizarPaseo` (HU28): duración `>= 15 min` con `0` puntos GPS o distancia `< 50 m` | **1** | 30 días |
| `REPORTE_GRAVE_CONFIRMADO` | Ambos | Resolución de reporte en HU37 confirmando falta grave (maltrato, suplantación, agresión) | **5** | 60 días |

### Flujo de Cancelación con Motivos Parametrizados (`/application-parameters`)
* **Catálogo para Combo en App:** `GET /api-paseape/application-parameters` expondrá la lista de `tipo_motivo_cancelacion` activos (`id`, `uuid`, `descripcion`, `rol_aplicable`) para poblar el selector desplegable de cancelación tanto del cliente como del paseador.
* **Contrato de Cancelación (`CancelarSolicitudReqDto`):** Los endpoints `PATCH /cliente/solicitudes/{uuid}/cancelar` y `PATCH /paseador/solicitudes/{uuid}/cancelar` reciben `motivo_cancelacion_id` (seleccionado del combo) junto con el campo de texto `motivo` (comentario explicativo).
* **Evaluación de Penalidad por Responsabilidad:** `SolicitudPaseoService` inspecciona el `tipo_motivo_cancelacion` seleccionado para determinar si no aplica penalidad (fuerza mayor/sin culpa), si penaliza al usuario que cancela (`CANCELACION_TARDIA_*`) o si penaliza a la contraparte (ej. `NO_SHOW_CLIENTE` cuando el paseador cancela porque el cliente no entregó la mascota).

### Reglas de Escalamiento Automático
Al registrar una falta en `usuario_penalidad` (con idempotencia garantizada por `UNIQUE(solicitud_paseo_id, usuario_id, tipo_penalidad_id)`), `PenalidadService` suma los `puntos_aplicados` vigentes (`anulada = 0` y `fecha_expiracion_puntos > UTC_NOW()`):
* **4 a 6 puntos:** Crea `usuario_sancion` automática `TEMPORAL` por **24 horas** (`SUSPENDIDO` [4]).
* **7 a 9 puntos:** Crea `usuario_sancion` automática `TEMPORAL` por **7 días** (`SUSPENDIDO` [4]).
* **≥ 10 puntos (o `REPORTE_GRAVE_CONFIRMADO`):** Crea `usuario_sancion` preventiva por **30 días** (`SUSPENDIDO` [4]) y notifica a `ADMINISTRADOR` para evaluar bloqueo permanente (`BLOQUEADO` [5]) en HU40.

---

## 6. Matriz Consolidada de Endpoints del Sprint 4

Todas las rutas operan bajo `/api-paseape` retornando `ResponseDto<T>`:

| Método | Endpoint | Rol | Historia | Estado |
| :--- | :--- | :--- | :--- | :--- |
| `GET/POST/PUT/DELETE/PATCH` | `/catalogos/{distritos\|tipos-mascota\|razas\|tamanos-mascota\|niveles-energia\|generos-mascota}/...` | `ADMINISTRADOR` | HU41–HU44 | **IMPLEMENTADO** |
| `POST` | `/calificaciones/solicitudes/{uuid}` | `CLIENTE` | HU32, HU33 | **IMPLEMENTADO** |
| `GET` | `/calificaciones/paseador` | `PASEADOR` | HU34 | **IMPLEMENTADO** |
| `GET` | `/calificaciones/paseadores/{uuid}` | Autenticado | HU34 | **IMPLEMENTADO** |
| `POST` | `/reportes` | `CLIENTE`, `PASEADOR` | Base HU35/36 | **PENDIENTE** |
| `GET` | `/admin/reportes` | `ADMINISTRADOR` | HU37 | **PENDIENTE** |
| `GET` | `/admin/reportes/{uuid}` | `ADMINISTRADOR` | HU37 | **PENDIENTE** |
| `PATCH` | `/admin/reportes/{uuid}/gestionar` | `ADMINISTRADOR` | HU37 | **PENDIENTE** |
| `POST` | `/admin/usuarios/{uuid}/sanciones` | `ADMINISTRADOR` | HU40 | **PENDIENTE** |
| `PATCH` | `/admin/sanciones/{uuid}/levantar` | `ADMINISTRADOR` | HU40 | **PENDIENTE** |
| `GET` | `/admin/usuarios/{uuid}/sanciones` | `ADMINISTRADOR` | HU40 | **PENDIENTE** |
| `GET` | `/mi-cuenta/estado-sanciones` | `CLIENTE`, `PASEADOR` | Transparencia Puntaje | **PENDIENTE** |

---

## 7. Checklist de Implementación DDD para Historias Pendientes (`docs/agents`)

1. **Dominio (`domain.entity`):** Clases puras con Lombok (sin `@Entity`, sin `@SQLRestriction`, sin `record`).
2. **Infraestructura JPA (`infrastructure.entity`):** Herencia obligatoria de `Auditable` y `@SQLRestriction("estado = 1")` en entidades transaccionales (omitida en catálogos).
3. **Puertos y Adaptadores (`application.repository` / `infrastructure.repository`):** Prohibido retornar `Optional<T>` en puertos; retornar `T` o `null`, y `List<T>` vacía cuando no hay datos.
4. **Mappers MapStruct:** `*DboMapper` (Entity ↔ Dominio) y `*DtoMapper` (Dominio → `*ResDto`).
5. **DTOs:** `*ReqDto` planos sin anotaciones Jakarta y con `@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)`; identificación pública estricta por `uuid`.
6. **Validadores (`infrastructure.validator`):** `try/catch` + `BadRequestException` + `BuildErrorForValidation(...)`.
7. **Servicios (`application.service`):** Clases concretas (sin interfaz/`Impl`), `@Transactional(rollbackFor = Exception.class)` en escrituras y `@Transactional(readOnly = true)` en lecturas.
8. **Controladores (`infrastructure.controller`):** Controladores independientes (sin `BaseController`) con `LoggerFactory` y `RequestService`.

---

## 8. Plan de Desarrollo por Bloques y Estado de Avance

| Bloque | Alcance | Dependencias | Estado |
| :--- | :--- | :--- | :---: |
| **Base BD y Catálogos** | Migraciones `V2.3.0` y `V2.3.1`, mantenedor `HU41`–`HU44` y diseño Figma Sprint 4 | — | **COMPLETADO** |
| **Bloque 0 – Base Común y Seguridad** | Constantes en `Constant.java` y restricción de rutas `/admin/**` en `SecurityConfig` | Base BD | **COMPLETADO** |
| **Bloque 1 – Reputación y Reseñas (`HU32`, `HU33`, `HU34`)** | Calificación 1–5 ★, opinión única, métricas en `Paseador` y paneles público/privado | Bloque 0 | **COMPLETADO** |
| **Bloque 2 – Sanciones (`HU40`), Penalidades y Cancelación** | Motivos en `/application-parameters`, `SancionService`, `PenalidadService`, `mi-cuenta/estado-sanciones`, filtro JWT y jobs | Bloque 0 | **PENDIENTE** |
| **Bloque 3 – Reportes e Incidencias (`HU35/36`, `HU37`)** | `POST /reportes` (`multipart/form-data`), bandeja/gestión Admin, seguimiento y penalidad grave | Bloque 2 | **PENDIENTE** |
| **Bloque 4 – Integración `app-android-paseape`** | Conexión de pantallas Figma (`SCR_S3_05`, `SCR_S4_01`–`08`, modales y drawer Admin) | Bloques 1, 2 y 3 | **PENDIENTE** |

### Detalle de Tareas por Bloque

#### Base BD, Catálogos y Diseño — **COMPLETADO**
- [x] Migración `V2.3.0__sprint_4_reputacion_reportes_sanciones_schema.sql`.
- [x] Migración `V2.3.1__tipo_motivo_cancelacion_schema.sql`.
- [x] Mantenedor administrativo de catálogos (`HU41`–`HU44`).
- [x] Diseño UI/UX en Figma (página `04. Sprint 4` + actualización de `SCR_S3_05`, `SCR_S3_02`, `SCR_C2` y drawer vertical Admin).

#### Bloque 0: Base Común y Seguridad — **COMPLETADO**
- [x] Agregar constantes de `tipo_notificacion` (`7`–`11`), `EVIDENCIA_REPORTE` (`5`), `categoria_reporte`, `estado_reporte` y códigos de `tipo_penalidad` en `Constant.java`.
- [x] Ajustar `SecurityConfig.java` para mantener público solo el registro admin y proteger `/admin/reportes/**`, `/admin/usuarios/**` y `/admin/sanciones/**`.

#### Bloque 1: Reputación y Reseñas (`HU32`, `HU33`, `HU34`) — **COMPLETADO**
- [x] Dominio, entidad JPA (`PaseoCalificacionEntity`), puerto/adaptador (`IPaseoCalificacionRepository`) y mappers (`Dbo` / `Dto`).
- [x] `POST /calificaciones/solicitudes/{uuid}` (`HU32`, `HU33`): recálculo en `Paseador`, notificación `CALIFICACION_RECIBIDA` (`7`) y opinión opcional (`5–1000` chars).
- [x] `GET /calificaciones/paseador` y `GET /calificaciones/paseadores/{uuid}` (`HU34`): resumen $O(1)$ y paginación en hora Lima.
- [x] Pruebas unitarias (`CalificacionServiceTest`, `CalificacionValidatorTest`).

#### Bloque 2: Sanciones (`HU40`), Penalidades y Cancelación — **PENDIENTE**
- [ ] Exponer `tipo_motivo_cancelacion` (`V2.3.1`) en `ApplicationParameterService` / `ApplicationParameterController`.
- [ ] Dominio, entidades JPA, repositorios y mappers para `TipoPenalidad`, `UsuarioPenalidad` y `UsuarioSancion`.
- [ ] `SancionService` + `SancionValidator` + endpoints Admin (`POST /admin/usuarios/{uuid}/sanciones`, `PATCH /admin/sanciones/{uuid}/levantar`, `GET /admin/usuarios/{uuid}/sanciones`).
- [ ] Endpoint `GET /mi-cuenta/estado-sanciones` para Cliente y Paseador.
- [ ] Validación de cuenta `ACTIVO` en `JwtAuthenticationFilter` / `CustomUserDetailsService` y job de desbloqueo automático en `AppTaskScheduler`.
- [ ] `PenalidadService` (acumulador idempotente y escalamiento) + actualización de `CancelarSolicitudReqDto` (`motivo_cancelacion_id` + `motivo`) y hooks en `SolicitudPaseoService` / `AppTaskScheduler`.
- [ ] Pruebas unitarias (`SancionServiceTest`, `PenalidadServiceTest`, `SolicitudPaseoServiceTest`).

#### Bloque 3: Reportes e Incidencias (`HU35/36`, `HU37`) — **PENDIENTE**
- [ ] Dominio, entidades JPA, repositorios y mappers para `CategoriaReporte`, `EstadoReporte`, `ReporteIncidencia` y `ReporteSeguimiento`.
- [ ] `POST /reportes` (`multipart/form-data`): subida de evidencias vía `DigitalDocumentService` (`5: EVIDENCIA_REPORTE`) y notificación `REPORTE_RECIBIDO` (`8`) a reportante y administradores.
- [ ] Endpoints Admin `HU37`: `GET /admin/reportes`, `GET /admin/reportes/{uuid}`, `PATCH /admin/reportes/{uuid}/gestionar` (seguimiento, notificación `9` y disparo opcional de `REPORTE_GRAVE_CONFIRMADO`).
- [ ] Pruebas unitarias (`ReporteServiceTest`, `ReporteValidatorTest`).

#### Bloque 4: Integración en `app-android-paseape` — **PENDIENTE**
- [ ] Flujo de calificación/opinión en 2 pasos (`SCR_S3_05`), `SCR_S4_01` y `SCR_S4_02`.
- [ ] Modal de cancelación parametrizado (`MODAL_S4_CancelarServicio`), `SCR_S4_08` + `MODAL_S4_InfoPuntajeAmigable` y `SCR_S4_07`.
- [ ] Flujo de reportes (`SCR_S4_03`), menú lateral vertical Admin (`OVERLAY_ADM_NavDrawer_Reportes`), `SCR_S4_04`, `SCR_S4_05` y `SCR_S4_06`.
