# Especificaciones y Reglas de Negocio - SPRINT 3 (api-paseape)

Este documento detalla los requerimientos funcionales, el modelo físico de persistencia, las reglas de negocio y la arquitectura técnica para las Historias de Usuario del **SPRINT 3** del backend **api-paseape**, enfocado en el inicio del servicio, la telemetría GPS acumulativa en tiempo real, la supervisión en vivo por el cliente y la consolidación histórica del recorrido (HU27, HU23, HU26, HU24, HU28, HU29, HU30, HU31).

---

## 1. Resumen Ejecutivo y Arquitectura de Telemetría GPS Acumulativa

Para erradicar cuellos de botella por inserciones concurrentes masivas de coordenadas cada pocos segundos en **Aiven MySQL Cloud**, el Sprint 3 adopta el **Modelo Acumulativo Directo**:

1. **Eliminación Total de la Bitácora Intermedia Fila por Fila:** No se crean tablas temporales ni se persiste cada ping GPS como un registro independiente. Se evita la creación de cientos de miles de filas efímeras diarias en la base de datos relacional.
2. **Buffer de Estado Único en Caliente ($O(1)$):** La tabla `solicitud_paseo_ubicacion_en_vivo` conserva una **única tupla activa por cada servicio en ejecución** (`EN_CURSO` [5]). Dicha fila abastece las lecturas de monitoreo en vivo del cliente y acumula en caliente la distancia ortodrómica recorrida (Haversine) y la concatenación delta de la polilínea comprimida (`coordenadas_codificadas`).
3. **Consolidación Instantánea Post-Paseo ($O(1)$):** Al transicionar a `FINALIZADA` (6), no se procesan consultas masivas de coordenadas ni serializaciones pesadas en memoria; la tupla acumulada se transfiere directamente a `solicitud_paseo_recorrido` y se elimina el registro transitorio de la tabla en vivo.
4. **Identificadores Canónicos UUID v4:** Todas las tablas y contratos públicos (`*ReqDto`, `*ResDto` y rutas HTTP) operan mediante `uuid` (`CHAR(36)`), protegiendo las claves primarias numéricas autoincrementales internas (`BIGINT`).
5. **Costo Cero ($0 USD) en Google Cloud Platform:** Renderizado vectorial nativo con Google Maps SDK for Android, decodificación local en dispositivo móvil con `android-maps-utils` y captura hardware eficiente mediante `FusedLocationProviderClient`.

```
                           DURANTE EL PASEO (EN_CURSO)
                               
   [App Paseador] ──(batch de puntos)──► POST /paseador/solicitudes/{uuid}/ubicaciones
                                               │
                                               ▼
                             ┌────────────────────────────────────┐
                             │ solicitud_paseo_ubicacion_en_vivo  │
                             │ (1 FILA ÚNICA POR PASEO EN CURSO)  │
                             │  - uuid CHAR(36)                   │
                             └────────────────────────────────────┘
                                │                               │
     (Dueño consulta en vivo)   │                               │ (Actualiza en caliente)
                                ▼                               ▼
                     latitud, longitud, rumbo       coordenadas_codificadas (POLYLINE DELTA)
                                                    distancia_metros_acumulada
                                                    total_puntos
                                                                │
                                                                │
                                   AL FINALIZAR EL PASEO        │
                                                                ▼
                             ┌────────────────────────────────────┐
                             │     solicitud_paseo_recorrido      │
                             │   (SE MUEVE LA FILA YA ARMADA)     │
                             │  - uuid CHAR(36)                   │
                             └────────────────────────────────────┘
                                - INSERT directo en histórico post-paseo
                                - DELETE del registro transitorio en vivo
```

---

## 2. Comparativa de Rendimiento y Almacenamiento

| Métrica | Enfoque con Bitácora Fila por Fila | Modelo Acumulativo Directo (Adoptado) |
|---|---|---|
| **Tablas creadas** | 3 tablas (`en_vivo`, `bitacora`, `recorrido`) | **2 tablas operativas** (`en_vivo`, `recorrido`) + 1 catálogo (`tipo_origen_traza`) |
| **Identificadores** | Auto-incrementales numéricos dispersos | `id BIGINT/INT` + `uuid CHAR(36)` obligatorio en todas las entidades |
| **Filas por paseo (2 horas)** | **720 filas** insertadas | **1 fila** en vivo (actualizada) y luego **1 fila** final consolidada |
| **Carga en Base de Datos** | Escritura masiva constante de `INSERT`s | Actualizaciones puntuales indexadas por clave única ($O(1)$) |
| **Cómputo al finalizar** | Costoso: consulta 720 filas, bucle Haversine y serialización en memoria | **Instantáneo ($O(1)$)**: la polilínea y la métrica acumulada ya están armadas |
| **Riesgo de saturación en Aiven** | Alto (requiere purgas continuas de base de datos) | **Cero**: la tabla en vivo solo contiene tantas filas como paseos activos simultáneos |

---

## 3. Modelo Físico de Base de Datos (DDL Flyway)

Todas las tablas cumplen la directriz de auditoría de `docs/agents/persistence.md` (campos de auditoría gestionados por `Auditable` o migración) y clave pública `uuid CHAR(36) NOT NULL UNIQUE`.

### 3.1. Tabla Catálogo: `tipo_origen_traza`
Clasifica el origen y metodología de captura de la ruta para auditoría del servicio.

```sql
CREATE TABLE IF NOT EXISTS `tipo_origen_traza` (
    `id` INT NOT NULL,
    `uuid` CHAR(36) NOT NULL UNIQUE,
    `descripcion` VARCHAR(50) NOT NULL,
    `detalle` VARCHAR(255) NULL,
    `estado` TINYINT(1) NOT NULL DEFAULT 1,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) NOT NULL,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(100) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tipo_origen_traza_uuid` (`uuid`),
    UNIQUE KEY `uk_tipo_origen_traza_descripcion` (`descripcion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `tipo_origen_traza` (`id`, `uuid`, `descripcion`, `detalle`, `estado`, `created_by`, `updated_by`)
VALUES
    (1, UUID(), 'GPS_TIEMPO_REAL', 'Telemetría GPS en tiempo real capturada en la sesión del paseo', 1, 'SYSTEM', 'SYSTEM'),
    (2, UUID(), 'RUTA_INTERPOLADA', 'Ruta aproximada interpolada ante pérdidas o interrupciones de señal', 1, 'SYSTEM', 'SYSTEM'),
    (3, UUID(), 'CIERRE_ADMINISTRATIVO', 'Cierre manual asistido o regularizado por el equipo de soporte', 1, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE `descripcion` = VALUES(`descripcion`), `detalle` = VALUES(`detalle`);
```

### 3.2. Tabla Acumulativa en Tiempo Real: `solicitud_paseo_ubicacion_en_vivo`
Mantiene la última posición conocida y el acumulador en caliente de la polilínea mientras el servicio permanece en estado `5` (`EN_CURSO`).

```sql
CREATE TABLE IF NOT EXISTS `solicitud_paseo_ubicacion_en_vivo` (
    `id` BIGINT AUTO_INCREMENT NOT NULL,
    `uuid` CHAR(36) NOT NULL UNIQUE,
    `solicitud_paseo_id` BIGINT NOT NULL UNIQUE,
    `latitud_actual` DECIMAL(10, 8) NOT NULL,
    `longitud_actual` DECIMAL(11, 8) NOT NULL,
    `precision_m` DECIMAL(6, 2) NULL,
    `velocidad_ms` DECIMAL(6, 2) NULL,
    `rumbo_grados` DECIMAL(5, 2) NULL,
    `fecha_hora_gps` DATETIME(6) NOT NULL,
    `coordenadas_codificadas` LONGTEXT NOT NULL,
    `distancia_metros_acumulada` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `total_puntos` INT NOT NULL DEFAULT 1,
    `estado` TINYINT(1) NOT NULL DEFAULT 1,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) NOT NULL,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(100) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_spuev_uuid` (`uuid`),
    CONSTRAINT `fk_spuev_solicitud` FOREIGN KEY (`solicitud_paseo_id`) 
        REFERENCES `solicitud_paseo` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_spuev_solicitud_estado` ON `solicitud_paseo_ubicacion_en_vivo` (`solicitud_paseo_id`, `estado`);
```

### 3.3. Tabla de Recorrido Consolidado (Historial Strava-like): `solicitud_paseo_recorrido`
Almacena el resultado inmutable post-paseo para auditoría y renderizado de la ruta final en la aplicación móvil.

```sql
CREATE TABLE IF NOT EXISTS `solicitud_paseo_recorrido` (
    `id` BIGINT AUTO_INCREMENT NOT NULL,
    `uuid` CHAR(36) NOT NULL UNIQUE,
    `solicitud_paseo_id` BIGINT NOT NULL UNIQUE,
    `tipo_origen_traza_id` INT NOT NULL,
    `distancia_metros` DECIMAL(10, 2) NOT NULL,
    `duracion_segundos` INT NOT NULL,
    `velocidad_promedio_kmh` DECIMAL(5, 2) NULL,
    `coordenadas_codificadas` LONGTEXT NOT NULL,
    `coordenadas_total_puntos` INT NOT NULL,
    `estado` TINYINT(1) NOT NULL DEFAULT 1,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) NOT NULL,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(100) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_spr_uuid` (`uuid`),
    CONSTRAINT `fk_spr_solicitud` FOREIGN KEY (`solicitud_paseo_id`) 
        REFERENCES `solicitud_paseo` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_spr_tipo_origen` FOREIGN KEY (`tipo_origen_traza_id`) 
        REFERENCES `tipo_origen_traza` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_spr_solicitud_estado` ON `solicitud_paseo_recorrido` (`solicitud_paseo_id`, `estado`);
```

---

## 4. Cronograma de Intervalos y Cadencia Operativa

| Componente | Intervalo | API / Mecanismo | Justificación Técnica |
|---|---|---|---|
| **Paseador (Captura Móvil)** | **Cada 10 a 15 s** | `FusedLocationProviderClient` (`PRIORITY_HIGH_ACCURACY`, `ForegroundService`) | Captura precisa del sensor GPS sorteando políticas Doze de Android. |
| **Paseador (Despacho Batch)** | **Cada 20 a 30 s** (2 a 3 puntos) | `POST /paseador/solicitudes/{uuid}/ubicaciones` | Minimiza aperturas de conexión TLS/SSL en redes móviles y ahorra batería. |
| **Cliente (Short Polling)** | **Cada 6 a 8 s** | `GET /cliente/solicitudes/{uuid}/ubicacion-actual` | Refresco fluido de posición mediante lectura indexada $O(1)$ sin sobrecargar el servidor. |
| **Cliente (Ciclo de Vida)** | **Inmediato (Pausa/Reanuda)** | Hooks `onPause()` / `onResume()` en Android | Desactiva el short polling si la pantalla se apaga o el usuario sale del mapa. |
| **Filtro de Ruido GPS en JVM** | **En cada punto del batch** | Haversine en backend ($\Delta d \ge 3.0\text{ m}$) | Si el desplazamiento es $< 3\text{ m}$, actualiza la posición en vivo pero no ensucia la polilínea. |
| **Heartbeat / Señal Degradada** | **Umbral de 45 s** | Verificación en `SolicitudPaseoService` | Si `fecha_hora_gps` supera los 45 s respecto a UTC, expone `senal_degradada = true`. |

---

## 5. Historias de Usuario del Sprint 3

### A. HU27 – Iniciar el paseo
- **Identificador:** HU27 | **Rol:** Paseador | **Puntos de historia:** 2
- **Dependencias:** HU20 (Bandeja Operativa), HU26 (Telemetría GPS)
- **Estado:** **IMPLEMENTADO Y VERIFICADO**
- **Descripción:** "Como paseador de mascotas, quiero iniciar formalmente un servicio de paseo confirmado, para registrar la hora de inicio, activar el seguimiento GPS y comunicar al dueño que el servicio ha comenzado."

#### Criterios de Aceptación
1. **Inicio de paseo confirmado:** El paseador con solicitud en `ACEPTADA` (2) selecciona "Iniciar paseo". El sistema valida que la fecha civil coincida con la programada (o sea modalidad inmediato), actualiza el estado a `EN_CURSO` (5) y fija `fecha_hora_inicio_real = UTC_NOW`.
2. **Confirmación y habilitación operativa:** Retorna `SolicitudPaseoResDto` con estado `EN_CURSO` y flags operativos para que la app móvil del paseador active el servicio en primer plano de captura GPS.
3. **Disparo de efectos secundarios:** Dispara la notificación interna para el cliente (HU23) y el envío asíncrono de correo vía Brevo.

#### Detalle de Implementación Técnica Realizada
- **Endpoint:** `PATCH /api-paseape/paseador/solicitudes/{uuid}/iniciar`
- **Controlador:** `SolicitudPaseoPaseadorController.java` (`iniciarPaseo`)
  - Manejo de sesión con `@AuthenticationPrincipal CustomUserDetails` y extracción del correo (`principal.getUsername()`).
  - Validación previa mediante `solicitudPaseoValidator.validateIniciarPaseo(userEmail, uuid)`. Si retorna un error, se devuelve directamente sin entrar al servicio.
  - Invocación a `solicitudPaseoService.iniciarPaseo(userEmail, uuid)`.
  - Retorna `ResponseDto<SolicitudPaseoResDto>` con HTTP 200 y mensaje `Constant.MENSAJE_OK_PASEO_INICIADO`.
- **Validador:** `SolicitudPaseoValidator.java` (`validateIniciarPaseo`)
  - Cumple estrictamente con el estándar Mortgage (`docs/agents/validators.md`).
  - Lanza internamente `BadRequestException` dentro de un bloque `try/catch` y canaliza la falla a través de `BuildErrorForValidation(...)` retornando `ResponseDto<?>`.
  - Valida usuario autenticado con rol `PASEADOR` activo mediante `IUsuarioRepository` e `IPaseadorRepository`.
  - Valida formato canónico UUID v4 (`Constant.PATRON_UUID`).
  - Consulta la solicitud por UUID en base de datos mediante el puerto `ISolicitudPaseoRepository.findByUuid`.
  - Valida titularidad estricta: `solicitud.getPaseador().getUsuario().getCorreo().equals(userEmail)`.
  - Valida estado del servicio: únicamente solicitudes en estado `ACEPTADA` (2) pueden iniciar. Bloquea si ya está en `EN_CURSO` (5) o estados posteriores.
  - Valida fecha civil en zona horaria `America/Lima` (`ZoneId.of(Constant.ZONE_ID_LIMA)`): para solicitudes programadas, la fecha civil actual debe coincidir con la fecha de inicio programada.
- **Lógica de Negocio en Servicio:** `SolicitudPaseoService.java` (`iniciarPaseo`)
  - Transiciona el estado a `EN_CURSO` (`idEstado = 5`).
  - Asigna la marca temporal real en UTC: `solicitud.setFechaHoraInicioReal(LocalDateTime.now(ZoneOffset.UTC))`.
  - Persiste la entidad actualizada mediante `solicitudPaseoRepository.save(solicitud)`.
  - Dispara en caliente los efectos secundarios de la HU23 (notificación interna persistida y correo Brevo asíncrono).

---

### B. HU23 – Recibir notificación cuando comience el paseo
- **Identificador:** HU23 | **Rol:** Cliente (Dueño) | **Puntos de historia:** 2
- **Dependencias:** HU20, HU27
- **Estado:** **IMPLEMENTADO Y VERIFICADO**
- **Descripción:** "Como dueño de una mascota, quiero recibir una notificación cuando el paseador inicie el paseo, para saber que mi mascota ya está siendo atendida y poder comenzar a supervisar el recorrido."

#### Criterios de Aceptación
1. **Notificación interna persistida:** Al transicionar a `EN_CURSO`, se crea un registro no leído (`leido = false`) en `notificaciones` con su propio `uuid`.
2. **Contenido de la alerta:** Título `¡El paseo ha comenzado!` (`Constant.TITULO_NOTIF_PASEO_INICIADO`), detalle con nombre del paseador, mascotas y hora local convertida a `America/Lima`.
3. **Correo transaccional:** Despacho asíncrono (`@Async`) vía Brevo REST API v3 con auditoría en `brevo_email`.

#### Detalle de Implementación Técnica Realizada
- **Punto de Disparo:** Orquestado directamente en `SolicitudPaseoService.iniciarPaseo` tras la persistencia exitosa del cambio de estado a `EN_CURSO`.
- **Notificación Interna en BD:**
  - Se genera una entidad de dominio `Notificacion` con `uuid = UUID.randomUUID().toString()`.
  - Tipo de evento: `SOLICITUD_PASEO_INICIADA`.
  - Título estandarizado: `Constant.TITULO_NOTIF_PASEO_INICIADO` (`"¡El paseo ha comenzado!"`).
  - Mensaje detallado: incluye el nombre de las mascotas asociadas, el nombre del paseador y la hora local formateada en la zona horaria de Lima (`America/Lima`).
  - Destinatario: ID de usuario del cliente (`solicitud.getCliente().getUsuario().getId()`).
  - Estado inicial: `leido = false`.
  - Persistencia mediante el puerto `INotificacionRepository.save(...)`.
- **Notificación Transaccional por Correo:**
  - Ejecución desacoplada y asíncrona mediante `@Async` en `BrevoEmailAdapter`.
  - Comunicación REST contra Brevo API v3.
  - Registro de auditoría persistido en la tabla `brevo_email` para trazabilidad de envíos.

---

### C. HU26 – Telemetría GPS Acumulativa por el Paseador
- **Identificador:** HU26 | **Rol:** Paseador | **Puntos de historia:** 8
- **Dependencias:** HU20, HU27
- **Estado:** **IMPLEMENTADO Y VERIFICADO**
- **Descripción:** "Como paseador de mascotas, quiero compartir mi ubicación GPS en lotes durante un paseo activo, para permitir que el dueño supervise la ubicación y el recorrido de su mascota sin saturar la red ni la base de datos."

#### Criterios de Aceptación
1. **Validación de estado activo:** Solo acepta coordenadas si la solicitud está estrictamente en `EN_CURSO` (5). Si ya finalizó o fue cancelada, responde 400 (`Constant.MSJ_ERROR_PASEO_NO_ACTIVO_GPS`).
2. **Acumulación en caliente:**
   - Si no existe registro en `solicitud_paseo_ubicacion_en_vivo`, crea la fila con `uuid = UUID.randomUUID().toString()`, punto inicial, `distancia_metros_acumulada = 0.00` y polilínea inicial codificada.
   - Para cada punto subsiguiente: calcula distancia con Haversine respecto al anterior. Si es $\ge 3.0	ext{ m}$, suma a `distancia_metros_acumulada` y concatena el delta comprimido a `coordenadas_codificadas`.
   - Actualiza `latitud_actual`, `longitud_actual`, `rumbo_grados`, `velocidad_ms` y `fecha_hora_gps`.

#### Detalle de Implementación Técnica Realizada
- **Endpoint:** `POST /api-paseape/paseador/solicitudes/{uuid}/ubicaciones`
- **Controlador:** `SolicitudPaseoPaseadorController.java` (`registrarUbicacionesTelemetria`)
  - Recibe `@PathVariable String uuid`, `@RequestBody RegistrarUbicacionesBatchReqDto reqDto` y `@AuthenticationPrincipal CustomUserDetails`.
  - Valida la solicitud delegando 100% de las comprobaciones a `solicitudPaseoValidator.validateRegistrarUbicaciones(userEmail, uuid, reqDto)`.
  - Ejecuta `solicitudPaseoService.registrarUbicacionesTelemetria(userEmail, uuid, reqDto)`.
  - Retorna `ResponseDto<BatchIngestaResDto>` con HTTP 200, indicando `puntos_procesados` y `paseo_activo = true`.
- **Validador:** `SolicitudPaseoValidator.java` (`validateRegistrarUbicaciones`)
  - Sigue el estándar `docs/agents/validators.md`: encapsula validaciones en `try/catch`, lanza `BadRequestException` y retorna `BuildErrorForValidation(...)`.
  - Valida que `reqDto` no sea nulo y que la lista `ubicaciones` contenga entre 1 y 50 elementos.
  - Valida coordenadas geográficas: $-90.0 \le 	ext{latitud} \le 90.0$ y $-180.0 \le 	ext{longitud} \le 180.0$.
  - Validación anti-spoofing temporal: `fechaHoraGps` no debe ser posterior a la hora UTC del servidor por más de 60 segundos.
  - Valida existencia y titularidad del paseador sobre la solicitud.
  - Valida estado estricto: la solicitud debe estar en `EN_CURSO` (5). Si se encuentra en cualquier otro estado, lanza `BadRequestException(Constant.MSJ_ERROR_PASEO_NO_ACTIVO_GPS)`.
- **Lógica de Negocio en Servicio:** `SolicitudPaseoService.java` (`registrarUbicacionesTelemetria`)
  - Libre de boilerplate de comprobación de nulos (delegadas completamente al validator).
  - Consulta la tupla en caliente en `solicitud_paseo_ubicacion_en_vivo` a través de `solicitudPaseoUbicacionEnVivoRepository.findBySolicitudPaseoId(solicitud.getId())`.
  - **Inicialización (Primer lote recibido):**
    - Instancia `SolicitudPaseoUbicacionEnVivo` con `uuid = UUID.randomUUID().toString()`.
    - Inicializa `distanciaMetrosAcumulada = BigDecimal.ZERO` y `totalPuntos = 1`.
    - Codifica el primer punto mediante `PolylineDeltaEncoder.codificarPuntoInicial(primerPto.getLatitud(), primerPto.getLongitud())`.
  - **Procesamiento de puntos subsiguientes en el lote:**
    - Itera sobre los puntos restantes calculando la distancia ortodrómica Haversine con el punto inmediatamente precedente usando `GeoDistanceCalculator.calcularDistanciaMetros(...)`.
    - **Filtro de Jitter / Ruido GPS:** Si $\Delta d \ge 3.0	ext{ m}$, suma la distancia a `distanciaMetrosAcumulada`, concatena el fragmento delta a `coordenadasCodificadas` con `PolylineDeltaEncoder.concatenarPuntoDelta(...)` e incrementa `totalPuntos`. Si $\Delta d < 3.0	ext{ m}$, descarta el punto para la polilínea para evitar ensuciar el recorrido con ruido estático.
  - **Actualización de Última Posición Conocida:**
    - Independientemente del filtro de jitter, actualiza en caliente los atributos del último punto: `latitudActual`, `longitudActual`, `precisionM`, `velocidadMs`, `rumboGrados` y `fechaHoraGps`.
  - Persiste la entidad actualizada mediante `solicitudPaseoUbicacionEnVivoRepository.save(...)` en una única operación $O(1)$.

---

### D. HU24 – Supervisión en Tiempo Real y Consulta de Recorrido
- **Identificador:** HU24 | **Rol:** Cliente (Dueño) / Paseador / Admin | **Puntos de historia:** 13
- **Dependencias:** HU23, HU26, HU27
- **Estado:** **IMPLEMENTADO Y VERIFICADO**
- **Descripción:** "Como dueño de una mascota, quiero visualizar en tiempo real la ubicación del paseador durante el paseo activo, y como dueño/paseador/administrador consultar el recorrido consolidado post-servicio."

#### Criterios de Aceptación
1. **Ubicación en tiempo real (Short Polling):** Consulta $O(1)$ a `solicitud_paseo_ubicacion_en_vivo`. Retorna `latitud`, `longitud`, `rumbo_grados`, `velocidad_ms`, `fechaHoraGps` y `senal_degradada` (true si el retraso GPS > 45 s).
2. **Espera de primera señal:** Si el paseo está `EN_CURSO` pero aún no hay pings GPS registrados por el paseador, responde HTTP 200 con `response = null` y mensaje informativo `"A la espera de la primera señal GPS del paseador"` (`Constant.MSJ_ESPERA_PRIMERA_SENAL_GPS`).
3. **Consulta de recorrido consolidado (Strava-like):** Post-paseo (`FINALIZADA`), expone la ruta inmutable desde `solicitud_paseo_recorrido` para decodificación y pintado en el mapa móvil.

#### Detalle de Implementación Técnica Realizada

##### D.1. Monitoreo en Vivo (Short Polling del Cliente)
- **Endpoint:** `GET /api-paseape/cliente/solicitudes/{uuid}/ubicacion-actual`
- **Controlador:** `SolicitudPaseoClienteController.java` (`obtenerUbicacionEnVivo`)
  - Valida mediante `solicitudPaseoValidator.validateObtenerUbicacionEnVivo(userEmail, uuid)`.
  - Invoca `solicitudPaseoService.obtenerUbicacionEnVivo(userEmail, uuid)`.
  - **Manejo de espera de primera señal:** Si el servicio retorna `null` (el paseo inició pero el paseador aún no emite su primer lote de coordenadas), el controlador devuelve `ResponseDto` con payload `null`, código 200 y mensaje `Constant.MSJ_ESPERA_PRIMERA_SENAL_GPS`.
  - Si existen coordenadas, responde con `ResponseDto<UbicacionEnVivoResDto>` con código 200 y mensaje `Constant.MENSAJE_OK`.
- **Validador:** `SolicitudPaseoValidator.java` (`validateObtenerUbicacionEnVivo`)
  - Valida autenticación y rol `CLIENTE`.
  - Valida existencia de la solicitud y titularidad del cliente autenticado.
  - Valida estado operativo: solo permite consulta si la solicitud está en `EN_CURSO` (5) o en transición `ACEPTADA` (2). Bloquea si ya finalizó o fue cancelada.
- **Lógica de Negocio en Servicio:** `SolicitudPaseoService.java` (`obtenerUbicacionEnVivo`)
  - Consulta en memoria/DB $O(1)$ a través de `solicitudPaseoUbicacionEnVivoRepository.findBySolicitudPaseoId(solicitud.getId())`.
  - Si no existe registro, retorna `null`.
  - Si existe: mapea a `UbicacionEnVivoResDto` vía `ISolicitudPaseoUbicacionEnVivoDtoMapper`.
  - **Diagnóstico de Señal Degradada:** Evalúa la antigüedad del último ping GPS recibido contra el reloj UTC del servidor:
    $$\Delta t_{	ext{gps}} = 	ext{Duration.between}(	ext{fechaHoraGps}, 	ext{nowUtc}).	ext{getSeconds}()$$
    Si $\Delta t_{	ext{gps}} > 45	ext{ s}$, fija `resDto.setSenalDegradada(true)`, permitiendo que la interfaz móvil atenúe visualmente el marcador en el mapa por falta de telemetría reciente.

##### D.2. Consulta de Recorrido Consolidado (Historial Strava-like)
- **Endpoint:** `GET /api-paseape/solicitudes/{uuid}/recorrido`
- **Controlador:** `SolicitudPaseoRecorridoController.java` (`obtenerRecorridoConsolidado`)
  - Controlador independiente y limpio según el estándar Mortgage (`docs/agents/`), sin extender de `BaseController`.
  - Aplica `@PreAuthorize("hasAnyRole('CLIENTE', 'PASEADOR', 'ADMIN')")`.
  - Valida mediante `solicitudPaseoValidator.validateObtenerRecorridoConsolidado(userEmail, uuid)`.
  - Invoca `solicitudPaseoService.obtenerRecorridoConsolidado(userEmail, uuid)`.
  - Retorna `ResponseDto<SolicitudPaseoRecorridoResDto>`.
- **Validador:** `SolicitudPaseoValidator.java` (`validateObtenerRecorridoConsolidado`)
  - Valida autenticación del usuario.
  - Valida existencia de la solicitud por UUID.
  - Valida autorización multi-rol: el usuario debe ser el `CLIENTE` dueño de la solicitud, el `PASEADOR` titular asignado o un usuario con rol `ADMIN`.
  - Valida estado inmutable: únicamente disponible cuando la solicitud se encuentra formalmente en estado `FINALIZADA` (6).
- **Lógica de Negocio en Servicio:** `SolicitudPaseoService.java` (`obtenerRecorridoConsolidado`)
  - Consulta inmutable $O(1)$ a `solicitud_paseo_recorrido` mediante `solicitudPaseoRecorridoRepository.findBySolicitudPaseoId(solicitud.getId())`.
  - Si no existe registro de recorrido consolidado, lanza `BadRequestException(Constant.MSJ_ERROR_NO_EXISTE_RECORRIDO)`.
  - Mapea la entidad de dominio a `SolicitudPaseoRecorridoResDto` mediante `ISolicitudPaseoRecorridoDtoMapper`.
  - Enriquece el DTO con `solicitudPaseoUuid`, `fechaHoraInicio` (`solicitud.getFechaHoraInicioReal()`), `fechaHoraFin` (`solicitud.getFechaHoraFinReal()`) y la descripción del origen (`GPS_TIEMPO_REAL`).

---

### E. HU28 – Finalizar el paseo
- **Identificador:** HU28 | **Rol:** Paseador | **Estimación:** 1 día | **Prioridad:** 96 | **Puntos de historia:** 2
- **Dependencias:** HU27 (Iniciar el paseo)
- **Estado:** **IMPLEMENTADO Y VERIFICADO**
- **Descripción:** "Como paseador de mascotas, quiero finalizar formalmente un paseo activo, para registrar el término del servicio, detener el seguimiento GPS y permitir que el dueño consulte el resumen del paseo."

#### Criterios de Aceptación
1. **Escenario 1 – Finalización exitosa:** Dado que el paseo se encuentra en curso (`EN_CURSO` [5]), cuando el paseador seleccione "Finalizar paseo" y confirme la acción, el sistema registra la fecha y hora de término (`fecha_hora_fin_real`) y cambia el estado a `FINALIZADA` (6).
2. **Escenario 2 – Cálculo de duración y métricas:** Dado que el paseo tiene registrada su hora de inicio y de término, el sistema calcula la duración real en segundos, consolida la ruta acumulada en `solicitud_paseo_recorrido`, libera el buffer en vivo e incrementa el contador de paseos completados del paseador.

#### Detalle de Implementación Técnica Realizada
- **Endpoint:** `PATCH /api-paseape/paseador/solicitudes/{uuid}/finalizar`
- **Controlador:** `SolicitudPaseoPaseadorController.java` (`finalizarPaseo`)
  - Extrae `userEmail` mediante `resolverUserEmail(principal, authHeader)`.
  - Valida previamente con `solicitudPaseoValidator.validateFinalizarPaseo(userEmail, uuid)`.
  - Invoca `solicitudPaseoService.finalizarPaseo(userEmail, uuid)`.
  - Retorna `ResponseDto<SolicitudPaseoResDto>` con HTTP 200 y mensaje `Constant.MSJ_PASEO_FINALIZADO_EXITO`.
- **Validador:** `SolicitudPaseoValidator.java` (`validateFinalizarPaseo`)
  - Sigue el estándar Mortgage (`docs/agents/validators.md`): encapsula comprobaciones en `try/catch` con `BadRequestException` y retorna `BuildErrorForValidation(...)`.
  - Verifica sesión autenticada (`userEmail`), formato UUID v4, existencia de usuario y perfil `Paseador` activo.
  - Verifica existencia de la solicitud y titularidad estricta del paseador autenticado.
- **Lógica de Negocio en Servicio:** `SolicitudPaseoService.java` (`finalizarPaseo`)
  - Verifica estado estricto `EN_CURSO` (5); rechaza si ya está en `FINALIZADA` (6) (`Constant.MSJ_ERROR_PASEO_YA_FINALIZADO`) o en cualquier otro estado (`Constant.MSJ_ERROR_SOLICITUD_NO_FINALIZABLE`).
  - Transiciona el estado a `FINALIZADA` (6) y asigna `fechaHoraFinReal = LocalDateTime.now()`.
  - Consulta el buffer en vivo (`solicitudPaseoUbicacionEnVivoRepository.findBySolicitudPaseoId(guardada.getId())`).
  - Calcula `duracionSegundos = ChronoUnit.SECONDS.between(fechaHoraInicioReal, fechaHoraFinReal)` (mínimo 0) y `velocidadPromedioKmh = (distanciaMetros / duracionSegundos) * 3.6` (con tope `999.99`).
  - Persiste el recorrido inmutable en `solicitud_paseo_recorrido` con `TipoOrigenTraza(Constant.ID_ORIGEN_TRAZA_GPS_TIEMPO_REAL)`.
  - Elimina la fila transitoria en caliente mediante `solicitudPaseoUbicacionEnVivoRepository.deleteBySolicitudPaseoId(guardada.getId())` si existía.
  - Incrementa el contador del paseador (`paseador.incrementarPaseosCompletados()`) y guarda la entidad `Paseador`.
  - Dispara el registro de notificación interna de finalización (HU29).

---

### F. HU29 – Recibir una notificación cuando termine el paseo
- **Identificador:** HU29 | **Rol:** Cliente (Dueño) | **Estimación:** 1 día | **Prioridad:** 60 | **Puntos de historia:** 2
- **Dependencias:** HU28 (Finalizar el paseo)
- **Estado:** **IMPLEMENTADO Y VERIFICADO**
- **Descripción:** "Como dueño de una mascota, quiero recibir una notificación cuando el paseador finalice el servicio, para saber que mi mascota terminó su paseo y consultar el resumen del recorrido."

#### Criterios de Aceptación
1. **Escenario 1 – Notificación de finalización:** Dado que el paseo está en curso, cuando el paseador finalice el servicio correctamente, el sistema genera y persiste una notificación interna dirigida al dueño indicando que el paseo ha terminado.
2. **Escenario 2 – Información de la notificación:** La notificación incluye el título estandarizado, el nombre del paseador, el código de reserva del paseo y la referencia directa al servicio para consultar el recorrido y calificarlo.

#### Detalle de Implementación Técnica Realizada
- **Punto de Disparo:** Orquestado directamente dentro de `SolicitudPaseoService.finalizarPaseo` tras consolidar el recorrido e incrementar las métricas del paseador.
- **Notificación Interna en BD (`notificaciones`):**
  - Se construye la entidad de dominio `Notificacion` con `uuid = UUID.randomUUID().toString()`.
  - Tipo de evento: `TipoNotificacion(Constant.ID_TIPO_NOTIFICACION_PASEO_FINALIZADO)` (ID `5`).
  - Título estandarizado: `Constant.TITULO_NOTIF_PASEO_FINALIZADO`.
  - Mensaje: `"El paseador " + paseadorNombre + " ha finalizado el paseo " + guardada.getCodigoReserva() + " de tus mascotas. Ya puedes revisar el recorrido y calificar el servicio."`
  - Referencia cruzada: `referenciaId = guardada.getId()` y `referenciaTipo = Constant.REFERENCIA_TIPO_SOLICITUD_PASEO`.
  - Remitente: `Usuario` del paseador | Destinatario: `Usuario` del cliente (`guardada.getCliente().getUsuario()`).
  - Estado inicial: `leido = false`, `estado = Constant.ESTADO_LOGICO_ACTIVO`.
  - Persistencia mediante `INotificacionRepository.save(notificacion)` (sin despacho de correo externo).

---

### G. HU30 – Consultar el historial de paseos como dueño
- **Identificador:** HU30 | **Rol:** Cliente (Dueño) | **Estimación:** 1 día | **Prioridad:** 60 | **Puntos de historia:** 2
- **Dependencias:** HU04, HU28, HU29
- **Estado:** **IMPLEMENTADO Y VERIFICADO**
- **Descripción:** "Como dueño de una mascota, quiero consultar el historial de los paseos realizados por mis mascotas, para revisar los servicios anteriores, conocer sus detalles y llevar un seguimiento de la actividad de mis mascotas."

#### Criterios de Aceptación
1. **Escenario 1 – Visualización del historial:** Dado que el dueño inició sesión y tiene paseos completados o cerrados, cuando acceda a la sección de historial, el sistema muestra sus servicios ordenados del más reciente al más antiguo de forma paginada.
2. **Escenario 2 – Consulta de detalles de un paseo:** Dado que el dueño visualiza su historial, cuando seleccione un paseo, el sistema muestra los detalles completos disponibles del servicio, las mascotas (con su foto) y el paseador asignado.

#### Detalle de Implementación Técnica Realizada
- **Escenario 1 – Listado Paginado de Historial del Cliente:**
  - **Endpoint:** `GET /api-paseape/cliente/solicitudes?tab=HISTORIAL&page=0&size=10`
  - **Controlador:** `SolicitudPaseoClienteController.java` (`obtenerBandejaCliente`).
  - **Validador:** `SolicitudPaseoValidator.java` (`validateConsultaBandejaCliente`) — valida que `tab` sea `ACTIVOS`, `PENDIENTES` o `HISTORIAL`, y que `page >= 0` y `1 <= size <= Constant.SIZE_MAXIMO`.
  - **Servicio:** `SolicitudPaseoService.java` (`obtenerBandejaCliente`) — para `Constant.TAB_CLIENTE_HISTORIAL`, filtra los estados históricos `RECHAZADA` (3), `CANCELADA` (4), `FINALIZADA` (6) y `VENCIDA` (7) del cliente autenticado mediante `findByClienteIdAndEstadosPaginado`, ordenados descendentemente (`Sort.by(DESC, "actualizadoEl").and(Sort.by(DESC, "creadoEl"))`), y ensambla las mascotas por lote (`findAllBySolicitudPaseoIdInWithDetails`) retornando `PageResponseDto<SolicitudPaseoResDto>`.
- **Escenario 2 – Consulta de Detalle Completo de un Paseo:**
  - **Endpoint:** `GET /api-paseape/solicitudes/{uuid}` (transversal).
  - **Controlador:** `SolicitudPaseoDetalleController.java` (`obtenerDetalleSolicitud`) — controlador independiente sin `BaseController` (estándar Mortgage).
  - **Validador:** `SolicitudPaseoDetalleValidator.java` (`validateObtenerDetalle`) — valida sesión, formato UUID v4, existencia de la solicitud y autorización (solo el cliente dueño, el paseador asignado o un administrador).
  - **Servicio:** `SolicitudPaseoDetalleService.java` (`obtenerDetalle`) — consulta `findByUuidWithDetails` y `findAllBySolicitudPaseoIdWithDetails` (con catálogos y `folderDigitalDocument`), mapea vía `ISolicitudPaseoDetalleDtoMapper.toDetalleResDto(...)` a `SolicitudPaseoDetalleResDto` (incluyendo detalle del paseador con `telefono` desde `Usuario`, fechas reales de inicio/fin, montos y lista completa de mascotas con `foto_mascota` resuelta vía `DigitalDocumentService.descargarDocumento`). Complementado con `GET /solicitudes/{uuid}/recorrido` (HU24) para el mapa del recorrido.

---

### H. HU31 – Consultar el historial de servicios como paseador
- **Identificador:** HU31 | **Rol:** Paseador | **Estimación:** 1 día | **Prioridad:** 40 | **Puntos de historia:** 2
- **Dependencias:** HU07, HU28
- **Estado:** **IMPLEMENTADO Y VERIFICADO**
- **Descripción:** "Como paseador de mascotas, quiero consultar el historial de los paseos que he realizado, para llevar un registro de mis servicios y revisar la información de cada paseo completado."

#### Criterios de Aceptación
1. **Escenario 1 – Visualización del historial:** Dado que el paseador inició sesión y tiene servicios completados o cerrados, cuando acceda a la sección de historial, el sistema muestra sus paseos realizados ordenados del más reciente al más antiguo de forma paginada.
2. **Escenario 2 – Consulta de detalles de un servicio:** Dado que el paseador visualiza su historial, cuando seleccione un paseo, el sistema muestra la información disponible del servicio, incluyendo la(s) mascota(s), la fecha, horarios reales/duración y el estado.

#### Detalle de Implementación Técnica Realizada
- **Escenario 1 – Listado Paginado de Historial del Paseador:**
  - **Endpoint:** `GET /api-paseape/paseador/solicitudes?tab=HISTORIAL&page=0&size=10`
  - **Controlador:** `SolicitudPaseoPaseadorController.java` (`obtenerBandejaPaseador`).
  - **Validador:** `SolicitudPaseoValidator.java` (`validateConsultaBandejaPaseador`) — valida `tab` (`INMEDIATAS`, `MIS_PASEOS`, `EN_CURSO`, `HISTORIAL`) y paginación (`page`, `size`).
  - **Servicio:** `SolicitudPaseoService.java` (`obtenerBandejaPaseador`) — para `Constant.TAB_HISTORIAL`, consulta `findByPaseadorIdAndEstadosPaginado` filtrando los estados `RECHAZADA` (3), `CANCELADA` (4), `FINALIZADA` (6) y `VENCIDA` (7) del paseador autenticado, ordenados por `actualizadoEl DESC, creadoEl DESC`, ensamblando las mascotas asociadas en `PageResponseDto<SolicitudPaseoResDto>`.
- **Escenario 2 – Consulta de Detalle Completo del Servicio:**
  - **Endpoint:** `GET /api-paseape/solicitudes/{uuid}` (transversal, compartido con HU30).
  - **Controlador / Validador / Servicio:** `SolicitudPaseoDetalleController` -> `SolicitudPaseoDetalleValidator` (verifica que el usuario autenticado sea el paseador asignado a la solicitud) -> `SolicitudPaseoDetalleService`, devolviendo `SolicitudPaseoDetalleResDto` con fechas (`fecha_programada`, `fecha_hora_inicio_real`, `fecha_hora_fin_real`), duración (`tipo_duracion_paseo_descripcion`, `duracion_minutos`), estado, datos de las mascotas (con `foto_mascota`) y resumen económico. Complementado con `GET /solicitudes/{uuid}/recorrido` (HU24) para visualizar el trazado histórico.

---

## 6. Matriz de Endpoints del Sprint 3

Todas las rutas operan bajo el context path `/api-paseape` y retornan `ResponseDto<T>`:

| Método | Endpoint | Rol Requerido | Descripción | Estado |
|---|---|---|---|---|
| `PATCH` | `/paseador/solicitudes/{uuid}/iniciar` | `PASEADOR` | Inicia el servicio confirmado, fija hora real y pasa a `EN_CURSO` (HU27, HU23). | **IMPLEMENTADO** |
| `POST` | `/paseador/solicitudes/{uuid}/ubicaciones` | `PASEADOR` | Ingesta en lote de coordenadas GPS con acumulación directa en caliente (HU26). | **IMPLEMENTADO** |
| `GET` | `/cliente/solicitudes/{uuid}/ubicacion-actual` | `CLIENTE` | Retorna última posición GPS conocida del paseador para monitoreo en vivo (HU24). | **IMPLEMENTADO** |
| `GET` | `/solicitudes/{uuid}/recorrido` | `CLIENTE`, `PASEADOR`, `ADMIN` | Consulta el recorrido consolidado inmutable post-paseo (Strava-like) (HU24). | **IMPLEMENTADO** |
| `PATCH` | `/paseador/solicitudes/{uuid}/finalizar` | `PASEADOR` | Concluye el paseo, consolida el recorrido a histórico, incrementa contador y notifica al dueño (HU28, HU29). | **IMPLEMENTADO** |
| `GET` | `/cliente/solicitudes?tab=HISTORIAL` | `CLIENTE` | Lista paginada del historial de paseos del dueño ordenado del más reciente al más antiguo (HU30). | **IMPLEMENTADO** |
| `GET` | `/paseador/solicitudes?tab=HISTORIAL` | `PASEADOR` | Lista paginada del historial de servicios del paseador ordenado del más reciente al más antiguo (HU31). | **IMPLEMENTADO** |
| `GET` | `/solicitudes/{uuid}` | `CLIENTE`, `PASEADOR`, `ADMIN` | Consulta el detalle completo de la solicitud, mascotas (con foto) y paseador asignado con teléfono (HU30, HU31). | **IMPLEMENTADO** |

---

## 7. Contratos DTO de Entrada y Salida

Conforme a `docs/agents/mappers-dtos.md`: clases estándar con Lombok, sin `record`, con `@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)`, sin anotaciones de validación de Jakarta en `*ReqDto` e identificación pública por `uuid`.

### 7.1. Request DTOs
* **`UbicacionPuntoReqDto`**: `latitud`, `longitud`, `precisionM`, `velocidadMs`, `rumboGrados`, `fechaHoraGps`.
* **`RegistrarUbicacionesBatchReqDto`**: `ubicaciones` (`List<UbicacionPuntoReqDto>`).

### 7.2. Response DTOs
* **`BatchIngestaResDto`**: `puntosProcesados`, `paseoActivo`.
* **`UbicacionEnVivoResDto`**: `uuid`, `solicitudPaseoUuid`, `latitud`, `longitud`, `precisionM`, `velocidadMs`, `rumboGrados`, `fechaHoraGps`, `senalDegradada`.
* **`SolicitudPaseoRecorridoResDto`**: `uuid`, `solicitudPaseoUuid`, `distanciaMetros`, `duracionSegundos`, `velocidadPromedioKmh`, `coordenadasCodificadas`, `coordenadasTotalPuntos`, `tipoOrigenTrazaDescripcion`, `fechaHoraInicio`, `fechaHoraFin`.
* **`SolicitudPaseoDetalleResDto`**: `uuid`, `codigoReserva`, modalidad, estado, método de pago, duración (`tipoDuracionPaseoId`, `tipoDuracionPaseoDescripcion`, `duracionMinutos`), distrito, dirección, `fechaProgramada`, bloque horario, montos, marcas de tiempo reales (`fechaHoraAceptada`, `fechaHoraInicioReal`, `fechaHoraFinReal`), `clienteUuid`, `clienteNombreCompleto`, `paseador` (`SolicitudPaseadorDetalleResDto` con `telefono`, `correo`, `fotoPerfilUrl`, tarifa, calificación y paseos completados) y `mascotas` (`List<SolicitudMascotaDetalleResDto>` con `fotoMascota`).

---

## 8. Algoritmos Geoespaciales en Memoria (Java 21)

### 8.1. Cálculo Ortodrómico: Haversine (`GeoDistanceCalculator`)
Calcula la distancia precisa en metros entre coordenadas con radio terrestre $R = 6\,371\,000\text{ m}$:
$$d = 2R\arcsin\left(\sqrt{\sin^2\left(\frac{\Delta\phi}{2}\right) + \cos(\phi_1)\cos(\phi_2)\sin^2\left(\frac{\Delta\lambda}{2}\right)}\right)$$

### 8.2. Codificación Delta Incremental de Polilíneas (`PolylineDeltaEncoder`)
Implementación del algoritmo oficial de Google Maps basada en diferencias relativas (deltas $p_n - p_{n-1}$). Permite concatenar nuevos puntos directamente al final del string sin reprocesar la ruta previa:
- `codificarPuntoInicial(lat, lon)`: Inicializa la polilínea con el primer punto.
- `concatenarPuntoDelta(latAnt, lngAnt, latNueva, lngNueva)`: Genera los caracteres delta anexables.

---

## 9. Validaciones de Negocio en `SolicitudPaseoValidator`

Siguiendo el estándar Mortgage (`docs/agents/validators.md`), el validador lanza internamente `BadRequestException` dentro de un bloque `try/catch` y retorna `ResponseDto<?>` mediante `BuildErrorForValidation`:
1. **Titularidad del Paseador:** Verifica que el usuario autenticado en sesión corresponda al paseador titular asignado a la solicitud.
2. **Fecha Civil (HU27):** Valida que la fecha actual en `ZoneId.of(Constant.ZONE_ID_LIMA)` coincida con la fecha programada (o sea modalidad inmediata).
3. **Estado Estricto (HU26):** Requiere estado `5` (`EN_CURSO`). Si ya finalizó o se canceló, lanza `BadRequestException(Constant.MSJ_ERROR_PASEO_NO_ACTIVO_GPS)`.
4. **Rango de Coordenadas:** Valida $-90.0 \le \text{latitud} \le 90.0$ y $-180.0 \le \text{longitud} \le 180.0$. La lista no puede estar vacía (máx. 50 puntos por lote).
5. **Anti-Spoofing Temporal:** `fechaHoraGps` no debe adelantar al reloj UTC del servidor por más de 60 segundos.

---

## 10. Guía de Integración Frontend Android (Kotlin) & Costo $0 GCP

* **Captura en Segundo Plano:** `ForegroundService` con `FusedLocationProviderClient` (`PRIORITY_HIGH_ACCURACY`, captura cada 10-15 s, despacho batch cada 20-30 s con 2-3 puntos).
* **Renderizado en Vivo:** `GoogleMap` SDK con marcador rotativo según `rumbo_grados`. Si `senal_degradada == true`, se atenúa la opacidad del marcador (`alpha = 0.5f`).
* **Pintado Post-Paseo:** Decodificación local instantánea mediante `PolylineEncoding.decode(coordenadasCodificadas)` de `android-maps-utils` y trazado con `googleMap.addPolyline(...)` auto-ajustando la cámara con `LatLngBounds`.
* **Costo GCP:** $0.00 USD (renderizado nativo ilimitado y decodificación local en CPU móvil).

---

## 11. Estado de Construcción Backend (DDD)

### 11.1. Componentes e Historias Desarrolladas (100% Implementado y Verificado)
- [x] **Migración Flyway:** `V2.2.0__sprint_3_schema.sql` aplicada con `tipo_origen_traza`, `solicitud_paseo_ubicacion_en_vivo` y `solicitud_paseo_recorrido` con claves foráneas, índices y `uuid`.
- [x] **Entidades de Dominio:** `TipoOrigenTraza`, `SolicitudPaseoUbicacionEnVivo`, `SolicitudPaseoRecorrido`.
- [x] **Entidades JPA (DBO):** Con herencia de `Auditable`, `@SQLRestriction("estado = 1")` e identificador canónico `uuid`.
- [x] **Puertos de Repositorio:** `ITipoOrigenTrazaRepository`, `ISolicitudPaseoUbicacionEnVivoRepository`, `ISolicitudPaseoRecorridoRepository` (sin `Optional<T>` en capa de aplicación).
- [x] **Adaptadores JPA y Mappers:** MapStruct DBO (`*DboMapper`) y DTO (`*DtoMapper`, incluyendo `ISolicitudPaseoDetalleDtoMapper`) en infraestructura.
- [x] **Utilidades Geoespaciales:** `GeoDistanceCalculator` (Haversine $R = 6,371,000\text{ m}$) y `PolylineDeltaEncoder` (Google Encoded Polyline delta compressor).
- [x] **Refactorización de Validadores:** `SolicitudPaseoValidator` y `SolicitudPaseoDetalleValidator` alineados estrictamente a `docs/agents/validators.md` (Mortgage pattern con `try/catch`, `BadRequestException`, `BuildErrorForValidation(...)` retornando `ResponseDto<?>`).
- [x] **HU27 (Iniciar Paseo):** `PATCH /paseador/solicitudes/{uuid}/iniciar` en `SolicitudPaseoPaseadorController` y `SolicitudPaseoService`.
- [x] **HU23 (Notificación de Inicio):** Notificación interna en BD y despacho asíncrono de correo Brevo implementados en `SolicitudPaseoService`.
- [x] **HU26 (Telemetría GPS por Lotes):** `POST /paseador/solicitudes/{uuid}/ubicaciones` con acumulador en caliente $O(1)$ y filtro de ruido GPS $\Delta d \ge 3.0\text{ m}$.
- [x] **HU24 (Monitoreo en Vivo Short Polling):** `GET /cliente/solicitudes/{uuid}/ubicacion-actual` con diagnóstico de señal degradada (> 45 s) y espera de primera señal.
- [x] **HU24 (Recorrido Consolidado Strava-like):** `GET /solicitudes/{uuid}/recorrido` en controlador independiente `SolicitudPaseoRecorridoController` con acceso multi-rol (`CLIENTE`, `PASEADOR`, `ADMIN`).
- [x] **HU28 & HU29 (Finalización del Paseo y Notificación):** `PATCH /paseador/solicitudes/{uuid}/finalizar` (Validador `validateFinalizarPaseo`, lógica en servicio `finalizarPaseo`, consolidación a histórico, incremento de paseos completados y notificación interna al cliente).
- [x] **HU30 & HU31 (Historial Cliente/Paseador y Detalle Transversal):** Bandejas paginadas `tab=HISTORIAL` en `SolicitudPaseoClienteController` y `SolicitudPaseoPaseadorController`, junto al endpoint transversal `GET /solicitudes/{uuid}` en `SolicitudPaseoDetalleController` con detalle de mascotas (y foto) y paseador (con teléfono).
- [x] **Pruebas Unitarias:** Cobertura de validadores, controladores y servicios de las historias implementadas.

### 11.2. Funcionalidades Pendientes de Desarrollo
- *Todas las funcionalidades del Sprint 3 (HU23, HU24, HU26, HU27, HU28, HU29, HU30 y HU31) han sido completadas e integradas.*

---

## 12. Trazabilidad de Clases e Implementación Técnica Realizada

> **Nota de Alcance:** Esta sección registra las clases, métodos y artefactos desarrollados y probados en el repositorio para el Sprint 3 siguiendo la Arquitectura Limpia y DDD (Estándar Mortgage).

### 12.1. Capa de Dominio (`domain`)
* `com.paseape.apipaseape.domain.entity.TipoOrigenTraza`: Modelo del catálogo de orígenes de traza (GPS_TIEMPO_REAL, RUTA_INTERPOLADA, CIERRE_ADMINISTRATIVO).
* `com.paseape.apipaseape.domain.entity.SolicitudPaseoUbicacionEnVivo`: Modelo del buffer de estado único en caliente para la posición actual y la polilínea delta incremental.
* `com.paseape.apipaseape.domain.entity.SolicitudPaseoRecorrido`: Modelo del recorrido histórico consolidado post-paseo inmutable.

### 12.2. Capa de Aplicación (`application`)
* **Puertos de Repositorio:**
  * `com.paseape.apipaseape.application.repository.ITipoOrigenTrazaRepository`
  * `com.paseape.apipaseape.application.repository.ISolicitudPaseoUbicacionEnVivoRepository`: Operaciones `findBySolicitudPaseoId`, `findByUuid`, `save`, `deleteBySolicitudPaseoId`.
  * `com.paseape.apipaseape.application.repository.ISolicitudPaseoRecorridoRepository`: Operaciones `findBySolicitudPaseoId`, `findByUuid`, `save`.
* **Servicios de Aplicación:**
  * `com.paseape.apipaseape.application.service.SolicitudPaseoService`: Métodos `iniciarPaseo`, `registrarUbicacionesTelemetria`, `obtenerUbicacionEnVivo`, `obtenerRecorridoConsolidado`, `finalizarPaseo`, `obtenerBandejaCliente`, `obtenerBandejaPaseador`.
  * `com.paseape.apipaseape.application.service.SolicitudPaseoDetalleService`: Método `obtenerDetalle` con resolución de fotos de mascotas vía `DigitalDocumentService`.

### 12.3. Capa de Infraestructura (`infrastructure`)
* **DTOs de Entrada y Salida (`infrastructure/dto/`):**
  * `UbicacionPuntoReqDto`, `RegistrarUbicacionesBatchReqDto`
  * `BatchIngestaResDto`, `UbicacionEnVivoResDto`, `SolicitudPaseoRecorridoResDto`
  * `SolicitudPaseoDetalleResDto`, `SolicitudPaseadorDetalleResDto`, `SolicitudMascotaDetalleResDto`
* **Mappers de DTO (MapStruct - `infrastructure/mapper/dto/`):**
  * `ISolicitudPaseoUbicacionEnVivoDtoMapper`
  * `ISolicitudPaseoRecorridoDtoMapper`
  * `ISolicitudPaseoDetalleDtoMapper`
* **Persistencia JPA (Adaptadores, Entidades y DBO Mappers):**
  * `TipoOrigenTrazaEntity`, `SolicitudPaseoUbicacionEnVivoEntity`, `SolicitudPaseoRecorridoEntity`
  * `ITipoOrigenTrazaJpaRepository`, `ISolicitudPaseoUbicacionEnVivoJpaRepository`, `ISolicitudPaseoRecorridoJpaRepository`
  * `TipoOrigenTrazaRepository`, `SolicitudPaseoUbicacionEnVivoRepository`, `SolicitudPaseoRecorridoRepository`
  * `ITipoOrigenTrazaDboMapper`, `ISolicitudPaseoUbicacionEnVivoDboMapper`, `ISolicitudPaseoRecorridoDboMapper`
* **Algoritmos y Utilidades Geoespaciales (`infrastructure/util/`):**
  * `GeoDistanceCalculator`: Cálculo ortodrómico Haversine con radio terrestre de 6,371,000 metros.
  * `PolylineDeltaEncoder`: Compresión y concatenación delta de coordenadas Google Encoded Polyline.
* **Validación de Negocio (`infrastructure/validator/`):**
  * `SolicitudPaseoValidator`: Métodos `validateIniciarPaseo`, `validateFinalizarPaseo`, `validateRegistrarUbicaciones`, `validateObtenerUbicacionEnVivo`, `validateObtenerRecorridoConsolidado`, `validateConsultaBandejaCliente`, `validateConsultaBandejaPaseador` retornando `ResponseDto<?>`.
  * `SolicitudPaseoDetalleValidator`: Método `validateObtenerDetalle` retornando `ResponseDto<?>`.
* **Controladores REST (`infrastructure/controller/`):**
  * `SolicitudPaseoPaseadorController`: Endpoints `GET /`, `PATCH /{uuid}/iniciar`, `PATCH /{uuid}/finalizar`, `POST /{uuid}/ubicaciones`.
  * `SolicitudPaseoClienteController`: Endpoints `GET /`, `GET /{uuid}/ubicacion-actual`.
  * `SolicitudPaseoRecorridoController`: Endpoint `GET /{uuid}/recorrido` (controlador independiente sin `BaseController`).
  * `SolicitudPaseoDetalleController`: Endpoint `GET /{uuid}` (controlador independiente sin `BaseController`).
* **Migración de Base de Datos:**
  * `db/migration/V2.2.0__sprint_3_schema.sql`: Script DDL aplicado en Aiven MySQL Cloud.
* **Pruebas Automatizadas:**
  * `SolicitudPaseoValidatorTest`, `SolicitudPaseoDetalleValidatorTest`
  * `SolicitudPaseoDetalleServiceTest`
  * `SolicitudPaseoClienteControllerTest`, `SolicitudPaseoPaseadorControllerTest`, `SolicitudPaseoRecorridoControllerTest`, `SolicitudPaseoDetalleControllerTest`
