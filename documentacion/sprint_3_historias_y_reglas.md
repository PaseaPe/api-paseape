# Especificaciones y Reglas de Negocio - SPRINT 3 (api-paseape)

Este documento detalla los requerimientos funcionales, el modelo físico de persistencia, las reglas de negocio y la arquitectura técnica para las Historias de Usuario del **SPRINT 3** del backend **api-paseape**, enfocado en el inicio del servicio, la telemetría GPS acumulativa en tiempo real, la supervisión en vivo por el cliente y la consolidación histórica del recorrido (HU27, HU23, HU26, HU24).

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
- **Descripción:** "Como paseador de mascotas, quiero iniciar formalmente un servicio de paseo confirmado, para registrar la hora de inicio, activar el seguimiento GPS y comunicar al dueño que el servicio ha comenzado."

#### Criterios de Aceptación
1. **Inicio de paseo confirmado:** El paseador con solicitud en `ACEPTADA` (2) selecciona "Iniciar paseo". El sistema valida que la fecha civil coincida con la programada (o sea modalidad inmediato), actualiza el estado a `EN_CURSO` (5) y fija `fecha_hora_inicio_real = UTC_NOW`.
2. **Confirmación y habilitación operativa:** Retorna `SolicitudPaseoResDto` con estado `EN_CURSO` y flags operativos para que la app móvil del paseador active el servicio en primer plano de captura GPS.
3. **Disparo de efectos secundarios:** Dispara la notificación interna para el cliente (HU23) y el envío asíncrono de correo vía Brevo.

#### Especificación Técnica
- **Endpoint:** `PATCH /paseador/solicitudes/{uuid}/iniciar`
- **Controlador:** `SolicitudPaseoPaseadorController` (estándar Mortgage, sin heredar de `BaseController`).
- **Validador:** `SolicitudPaseoValidator.validateIniciarPaseo(uuid)`.
- **Reglas del Servicio:**
  - Valida titularidad del paseador autenticado vía `Principal` (`solicitud.getPaseador().getUsuario().getCorreo().equals(userEmail)`).
  - Precondición estricta: únicamente solicitudes en `ACEPTADA` (2) pueden pasar a `EN_CURSO` (5).
  - Bloqueo de duplicidad: si ya está en `EN_CURSO` o `fecha_hora_inicio_real != null`, lanza `BadRequestException`.

---

### B. HU23 – Recibir notificación cuando comience el paseo
- **Identificador:** HU23 | **Rol:** Cliente (Dueño) | **Puntos de historia:** 2
- **Dependencias:** HU20, HU27
- **Descripción:** "Como dueño de una mascota, quiero recibir una notificación cuando el paseador inicie el paseo, para saber que mi mascota ya está siendo atendida y poder comenzar a supervisar el recorrido."

#### Criterios de Aceptación
1. **Notificación interna persistida:** Al transicionar a `EN_CURSO`, se crea un registro no leído (`leido = false`) en `notificaciones` con su propio `uuid`.
2. **Contenido de la alerta:** Título `¡El paseo ha comenzado!` (`Constant.TITULO_NOTIF_PASEO_INICIADO`), detalle con nombre del paseador, mascotas y hora local convertida a `America/Lima`.
3. **Correo transaccional:** Despacho asíncrono (`@Async`) vía Brevo REST API v3 con auditoría en `brevo_email`.

---

### C. HU26 – Telemetría GPS Acumulativa por el Paseador
- **Identificador:** HU26 | **Rol:** Paseador | **Puntos de historia:** 8
- **Dependencias:** HU20, HU27
- **Descripción:** "Como paseador de mascotas, quiero compartir mi ubicación GPS en lotes durante un paseo activo, para permitir que el dueño supervise la ubicación y el recorrido de su mascota sin saturar la red ni la base de datos."

#### Criterios de Aceptación
1. **Validación de estado activo:** Solo acepta coordenadas si la solicitud está estrictamente en `EN_CURSO` (5). Si ya finalizó o fue cancelada, responde 400 (`Constant.MSJ_ERROR_PASEO_NO_ACTIVO_GPS`).
2. **Acumulación en caliente:**
   - Si no existe registro en `solicitud_paseo_ubicacion_en_vivo`, crea la fila con `uuid = UUID.randomUUID().toString()`, punto inicial, `distancia_metros_acumulada = 0.00` y polilínea inicial codificada.
   - Para cada punto subsiguiente: calcula distancia con Haversine respecto al anterior. Si es $\ge 3.0\text{ m}$, suma a `distancia_metros_acumulada` y concatena el delta comprimido a `coordenadas_codificadas`.
   - Actualiza `latitud_actual`, `longitud_actual`, `rumbo_grados`, `velocidad_ms` y `fecha_hora_gps`.

#### Especificación Técnica
- **Endpoint:** `POST /paseador/solicitudes/{uuid}/ubicaciones`
- **Payload:** `RegistrarUbicacionesBatchReqDto` conteniendo `List<UbicacionPuntoReqDto>`.
- **Respuesta:** `BatchIngestaResDto` (`puntos_procesados`, `paseo_activo`).

---

### D. HU24 – Supervisión en Tiempo Real y Consulta de Recorrido
- **Identificador:** HU24 | **Rol:** Cliente (Dueño) | **Puntos de historia:** 13
- **Dependencias:** HU23, HU26, HU27
- **Descripción:** "Como dueño de una mascota, quiero visualizar en tiempo real la ubicación del paseador y consultar el recorrido consolidado post-servicio."

#### Criterios de Aceptación
1. **Ubicación en tiempo real (Short Polling):** Consulta $O(1)$ a `solicitud_paseo_ubicacion_en_vivo`. Retorna `latitud`, `longitud`, `rumbo_grados`, `velocidad_ms`, `fecha_hora_gps` y `senal_degradada` (true si el retraso GPS > 45 s).
2. **Espera de primera señal:** Si el paseo está `EN_CURSO` pero aún no hay pings GPS, retorna `response = null` con mensaje informativo `"A la espera de la primera señal GPS del paseador"`.
3. **Consulta de recorrido consolidado (Strava-like):** Post-paseo (`FINALIZADA`), expone la ruta inmutable desde `solicitud_paseo_recorrido` para decodificación y pintado en el mapa.

#### Especificación Técnica
- **Endpoint Monitoreo en Vivo:** `GET /cliente/solicitudes/{uuid}/ubicacion-actual`
- **Endpoint Recorrido Consolidado:** `GET /solicitudes/{uuid}/recorrido`

---

### E. Finalización del Paseo y Consolidación a Histórico
- **Rol:** Paseador
- **Endpoint:** `PATCH /paseador/solicitudes/{uuid}/finalizar`
- **Lógica de Consolidación:**
  1. Valida que la solicitud pertenezca al paseador y esté en `EN_CURSO` (5).
  2. Lee la fila única de `solicitud_paseo_ubicacion_en_vivo`.
  3. Calcula `duracion_segundos` entre `fecha_hora_inicio_real` y el instante actual UTC.
  4. Calcula `velocidad_promedio_kmh = (distancia_metros_acumulada / duracion_segundos) * 3.6`.
  5. Inserta directamente en `solicitud_paseo_recorrido` (`uuid = UUID.randomUUID().toString()`, polilínea delta ya ensamblada, total de puntos, tipo de origen 1).
  6. Elimina la fila de `solicitud_paseo_ubicacion_en_vivo`.
  7. Actualiza `solicitud_paseo` a `FINALIZADA` (6) con `fecha_hora_fin_real = UTC_NOW`.
  8. Emite notificación interna y correo transaccional de fin de servicio.

---

## 6. Matriz de Endpoints del Sprint 3

Todas las rutas operan bajo el context path `/api-paseape` y retornan `ResponseDto<T>`:

| Método | Endpoint | Rol Requerido | Descripción |
|---|---|---|---|
| `PATCH` | `/paseador/solicitudes/{uuid}/iniciar` | `PASEADOR` | Inicia el servicio confirmado, fija hora real y pasa a `EN_CURSO` (HU27, HU23). |
| `POST` | `/paseador/solicitudes/{uuid}/ubicaciones` | `PASEADOR` | Ingesta en lote de coordenadas GPS con acumulación directa en caliente (HU26). |
| `GET` | `/cliente/solicitudes/{uuid}/ubicacion-actual` | `CLIENTE` | Retorna última posición GPS conocida del paseador para monitoreo en vivo (HU24). |
| `PATCH` | `/paseador/solicitudes/{uuid}/finalizar` | `PASEADOR` | Concluye el paseo, consolida el recorrido a histórico y libera el buffer en vivo. |
| `GET` | `/solicitudes/{uuid}/recorrido` | `CLIENTE`, `PASEADOR`, `ADMIN` | Consulta el recorrido consolidado inmutable post-paseo (Strava-like). |

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

## 11. Checklist de Construcción Backend (DDD)

- [ ] **Migración Flyway:** `V2.2.0` creando `tipo_origen_traza`, `solicitud_paseo_ubicacion_en_vivo` y `solicitud_paseo_recorrido` con claves foráneas, índices y `uuid`.
- [ ] **Entidades de Dominio:** `TipoOrigenTraza`, `SolicitudPaseoUbicacionEnVivo`, `SolicitudPaseoRecorrido`.
- [ ] **Entidades JPA:** Con herencia de `Auditable` y `@SQLRestriction("estado = 1")` (salvo catálogo).
- [ ] **Puertos de Repositorio:** Retorno de entidad o `null`, cero `Optional` en `application/repository`.
- [ ] **Adaptadores JPA y Mappers:** MapStruct DBO y DTO.
- [ ] **Utilidades Geoespaciales:** `GeoDistanceCalculator` y `PolylineDeltaEncoder`.
- [ ] **Servicios y Transacciones:** `@Transactional(rollbackFor = Exception.class)` para mutaciones y `@Transactional(readOnly = true)` para lecturas.
- [ ] **Controladores:** Estándar Mortgage, sin heredar de `BaseController`, logger de clase propia y captura explícita de `BadRequestException`.
- [ ] **Pruebas Unitarias:** JUnit 5 + Mockito cubriendo caminos felices y excepciones de validación en `SolicitudPaseoServiceTest`.
