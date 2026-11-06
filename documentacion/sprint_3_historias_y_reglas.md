# Especificaciones y Reglas de Negocio - SPRINT 3 (api-paseape)

Este documento detalla los requerimientos funcionales, el modelo preliminar de datos, las reglas de negocio y la arquitectura técnica para las Historias de Usuario correspondientes al **SPRINT 3** del backend **api-paseape**, enfocado en la ejecución física del paseo, tracking GPS en tiempo real y supervisión activa del servicio.

---

## 1. Alcance del Sprint 3

El Sprint 3 tiene como objetivo primordial habilitar el ciclo de vida en ejecución de la plataforma *PaseaPe*:
- **Inicio formal del servicio de paseo (HU27):** Transición de estado `ACEPTADA` (2) a `EN_CURSO` (5), fijando la marca temporal real de inicio y habilitando el contexto de tracking.
- **Notificación transaccional de inicio de servicio (HU23):** Emisión modular de alerta push/in-app y correo electrónico vía Brevo hacia el dueño con los datos del paseador, mascotas participantes y hora de inicio.
- **Transmisión y telemetría de coordenadas GPS por el paseador (HU26):** Ingesta periódica de telemetría geoespacial (latitud, longitud, altitud, precisión, velocidad y timestamp UTC) durante paseos con estado `EN_CURSO`.
- **Supervisión y visualización en tiempo real para el cliente (HU24):** Consulta de última posición conocida (*current location*) e historial del trayecto recorrido (*breadcrumbs/route trace*) para renderizado cartográfico en la app móvil del dueño.
- **Identificación Pública Estándar:** Todos los contratos de entrada (`*ReqDto`) y salida (`*ResDto`) emplean identificadores canónicos `uuid` (`CHAR(36)`), manteniendo ocultas las claves primarias numéricas internas (`BIGINT`).
- **Control de Acceso y Aislamiento Multitenant:** Verificación estricta de identidad para asegurar que solo el paseador asignado transmita coordenadas y que únicamente el dueño propietario de las mascotas asociadas a la reserva acceda a la telemetría.

---

## 2. Modelo de Base de Datos Propuesto (Sprint 3 - Migración V2.2.0)

Para dar soporte a la telemetría geoespacial y trazabilidad del recorrido sin sobrecargar la tabla de cabecera `solicitud_paseo`, se plantea la creación de una tabla especializada de auditoría y trayectorias GPS.

```
 [ solicitud_paseo ]
  │ id (BIGINT PK)
  │ uuid (CHAR 36)
  │ solicitud_paseo_estado_id (FK -> 5: EN_CURSO)
  │ fecha_hora_inicio_real (DATETIME UTC)
  │
  │ 1
  │
  │ N
  └───► [ solicitud_paseo_ubicacion ] (Nuevo en V2.2.0)
         │ id (BIGINT PK AUTO_INCREMENT)
         │ uuid (CHAR 36 UNIQUE)
         │ solicitud_paseo_id (BIGINT FK)
         │ latitud (DECIMAL 10,8)
         │ longitud (DECIMAL 11,8)
         │ altitud (DECIMAL 7,2 NULLABLE)
         │ precision_metros (DECIMAL 6,2 NULLABLE)
         │ velocidad_ms (DECIMAL 6,2 NULLABLE)
         │ rumbo_grados (DECIMAL 5,2 NULLABLE)
         │ fecha_hora_gps (DATETIME UTC)
         │ creado_el (DATETIME UTC)
         │ creado_por (VARCHAR 100)
         │ estado (BIT DEFAULT 1)
         │
         │ INDEX: (solicitud_paseo_id, fecha_hora_gps DESC)
```

### 2.1. Entidad Propuesta: `solicitud_paseo_ubicacion`
- **`id`**: `BIGINT AUTO_INCREMENT PRIMARY KEY`.
- **`uuid`**: `CHAR(36) NOT NULL UNIQUE`.
- **`solicitud_paseo_id`**: `BIGINT NOT NULL`. Clave foránea referenciando a `solicitud_paseo(id)`.
- **`latitud`**: `DECIMAL(10, 8) NOT NULL`. Rango $[-90.0, +90.0]$.
- **`longitud`**: `DECIMAL(11, 8) NOT NULL`. Rango $[-180.0, +180.0]$.
- **`altitud`**: `DECIMAL(7, 2) NULL`. Altitud sobre el nivel del mar en metros.
- **`precision_metros`**: `DECIMAL(6, 2) NULL`. Margen de error reportado por el chip GPS del dispositivo móvil (en metros).
- **`velocidad_ms`**: `DECIMAL(6, 2) NULL`. Velocidad instantánea en metros por segundo.
- **`rumbo_grados`**: `DECIMAL(5, 2) NULL`. Orientación geográfica ($0.0^\circ$ a $360.0^\circ$).
- **`fecha_hora_gps`**: `DATETIME NOT NULL`. Marca de tiempo capturada por el sensor del dispositivo móvil (normalizada a UTC).
- **Auditoría estándar**: `creado_el` (DATETIME), `creado_por` (VARCHAR 100), `estado` (`BIT` DEFAULT 1).
- **Índices estratégicos**:
    - `idx_sp_ubicacion_paseo_fecha (solicitud_paseo_id ASC, fecha_hora_gps DESC)`: Vital para consultas ultrarrápidas de la última ubicación conocida.
    - `idx_sp_ubicacion_recorrido (solicitud_paseo_id ASC, creado_el ASC)`: Para la reconstrucción cronológica de la ruta.

---

## 3. Historias de Usuario del Sprint 3 (En Proceso de Especificación)

### A. HU27 – Iniciar el paseo
- **Identificador:** HU27
- **Rol:** Paseador de mascotas
- **Estimación:** 1 día | **Prioridad:** 96 | **Puntos de historia:** 2
- **Dependencias:** HU20 (Bandeja Operativa), HU26 (Telemetría GPS)
- **Descripción:** "Como paseador de mascotas, quiero iniciar formalmente un servicio de paseo confirmado, para registrar la hora de inicio, activar el seguimiento GPS y comunicar al dueño que el servicio ha comenzado."

#### Criterios de Aceptación
1. **Escenario 1 – Inicio de un paseo confirmado:**
    - Dado que el paseador tiene un servicio en estado `ACEPTADA` (id = 2) asignado a su cuenta.
    - Cuando seleccione "Iniciar paseo" en la aplicación móvil.
    - Entonces el sistema valida que la fecha civil coincida con la programada (o sea un paseo inmediato confirmado), actualiza el estado a `EN_CURSO` (id = 5) y registra `fecha_hora_inicio_real = UTC_TIMESTAMP`.
2. **Escenario 2 – Confirmación del inicio:**
    - Dado que el servicio inició satisfactoriamente.
    - Cuando el backend persista la mutación transaccional.
    - Entonces devuelve el `SolicitudPaseoResDto` con el nuevo estado y habilita en la respuesta los flags operativos necesarios para que la app del paseador inicie el worker de localización en segundo plano.

#### Especificación Técnica Prevista
- **Endpoint:** `PATCH /api-paseape/paseador/solicitudes/{uuid}/iniciar`
- **Controlador:** `SolicitudPaseoPaseadorController`
- **Validador:** `SolicitudPaseoValidator.validarIniciarPaseo(uuid)`
- **Reglas del Servicio (`SolicitudPaseoService`):**
    - Valida pertenencia del paseo (`solicitud.getPaseador().getUsuario().getId().equals(usuarioId)`).
    - Valida precondición de estado: únicamente solicitudes en `ACEPTADA` (2) pueden transicionar a `EN_CURSO` (5).
    - Bloqueo de duplicidad: si `fecha_hora_inicio_real != null` o el estado actual ya es `EN_CURSO`, deniega con `BadRequestException`.
    - Dispara evento de dominio de notificación: `PaseoIniciadoEvent`.

---

### B. HU23 – Recibir una notificación cuando comience el paseo
- **Identificador:** HU23
- **Rol:** Dueño de mascota (Cliente)
- **Estimación:** 1 día | **Prioridad:** 63 | **Puntos de historia:** 2
- **Dependencias:** HU20, HU27
- **Descripción:** "Como dueño de una mascota, quiero recibir una notificación cuando el paseador inicie el paseo, para saber que mi mascota ya está siendo atendida y poder comenzar a supervisar el recorrido."

#### Criterios de Aceptación
1. **Escenario 1 – Inicio del paseo:**
    - Dado que existe un servicio en estado `ACEPTADA` que transiciona a `EN_CURSO`.
    - Cuando el paseador confirma el inicio.
    - Entonces el sistema genera de forma automática una notificación interna activa (`leido = false`) para el dueño en la tabla `notificaciones`.
2. **Escenario 2 – Contenido de la notificación:**
    - Dado que el paseo acaba de comenzar.
    - Cuando el dueño reciba o consulte sus notificaciones.
    - Entonces visualiza:
        - Título: `¡El paseo ha comenzado!` (`Constant.TITULO_NOTIF_PASEO_INICIADO`).
        - Mensaje descriptivo con el nombre del paseador, la lista de nombres de mascotas participantes y la hora local de inicio (convertida a `America/Lima`).
        - Referencia contextual: `referencia_id = solicitud.getId()`, `referencia_tipo = "SOLICITUD_PASEO"`.
        - Despacho de correo electrónico transaccional vía Brevo REST API v3 con plantilla de inicio de paseo.

#### Especificación Técnica Prevista
- **Disparador:** Invocado atómicamente o vía `ApplicationEventPublisher` desde `SolicitudPaseoService.iniciarPaseo`.
- **Tipo de Notificación:** `tipo_notificacion_id = 4 [PASEO_INICIADO]`.
- **Destinatario:** `solicitud.getCliente().getUsuario().getId()`.
- **Remitente:** `solicitud.getPaseador().getUsuario().getId()`.

---

### C. HU26 – Compartir la ubicación GPS como paseador
- **Identificador:** HU26
- **Rol:** Paseador de mascotas
- **Estimación:** 4 días | **Prioridad:** 94 | **Puntos de historia:** 8
- **Dependencias:** HU20, HU27
- **Descripción:** "Como paseador de mascotas, quiero compartir mi ubicación GPS durante un paseo activo, para permitir que el dueño supervise la ubicación y el recorrido de su mascota mientras realizo el servicio."

#### Criterios de Aceptación
1. **Escenario 1 – Activación del seguimiento GPS:**
    - Dado que el paseador tiene el servicio en estado `EN_CURSO`.
    - Cuando inicie el paseo y el dispositivo móvil recolecte las primeras coordenadas.
    - Entonces el backend valida que el paseo esté activo y pertenezca al paseador autenticado antes de aceptar puntos de telemetría.
2. **Escenario 2 – Envío periódico de coordenadas (Telemetría / Batching):**
    - Dado que el servicio está en progreso.
    - Cuando el cliente móvil despache las coordenadas (ya sea individualmente o en paquetes/batch para optimizar batería y conexiones de red).
    - Entonces el servidor persiste los puntos en `solicitud_paseo_ubicacion` con sus marcas temporales UTC y confirma la recepción.

#### Especificación Técnica Prevista
- **Endpoint Principal:** `POST /api-paseape/paseador/solicitudes/{uuid}/ubicacion`
- **Endpoint Alternativo (Batching de coordenadas):** `POST /api-paseape/paseador/solicitudes/{uuid}/ubicaciones/batch`
- **Contratos DTO Previstos (`infrastructure.dto.request`):**
    - `RegistrarUbicacionPaseoReqDto`:
        - `latitud` (`Double` obligatorio, $-90.0 \le lat \le 90.0$).
        - `longitud` (`Double` obligatorio, $-180.0 \le lon \le 180.0$).
        - `altitud` (`Double` opcional).
        - `precision_metros` (`Double` opcional).
        - `velocidad_ms` (`Double` opcional).
        - `rumbo_grados` (`Double` opcional).
        - `fecha_hora_gps` (`LocalDateTime` opcional, default `UTC_NOW`).
    - `RegistrarUbicacionesBatchReqDto`:
        - `puntos` (`List<RegistrarUbicacionPaseoReqDto>` no vacía, máx. 50 puntos por ráfaga).
- **Validador:** `UbicacionPaseoValidator`
- **Reglas del Servicio:**
    - Requiere que la solicitud exista y esté estrictamente en `solicitud_paseo_estado_id = 5` (`EN_CURSO`).
    - Si la solicitud ya finalizó o fue cancelada, se rechaza la ingesta con código 400 (`Constant.MSJ_ERROR_PASEO_NO_ACTIVO_GPS`).

---

### D. HU24 – Visualizar la ubicación del paseador durante el paseo
- **Identificador:** HU24
- **Rol:** Dueño de mascota (Cliente)
- **Estimación:** 4 días | **Prioridad:** 88 | **Puntos de historia:** 13
- **Dependencias:** HU23, HU26, HU27
- **Descripción:** "Como dueño de una mascota, quiero visualizar en tiempo real la ubicación del paseador durante el servicio, para supervisar el recorrido de mi mascota y conocer su ubicación mientras se realiza el paseo."

#### Criterios de Aceptación
1. **Escenario 1 – Visualización de ubicación en tiempo real:**
    - Dado que el paseo se encuentra en estado `EN_CURSO` y el paseador ha enviado telemetría.
    - Cuando el dueño acceda a la pantalla de seguimiento en vivo.
    - Entonces el sistema responde con la última posición conocida del paseador (`latitud`, `longitud`, `fecha_hora_gps`, `velocidad_ms`, etc.).
2. **Escenario 2 – Actualización y trazado del recorrido:**
    - Dado que el dueño mantiene abierta la vista del mapa con short polling.
    - Cuando consulte la ruta o nuevos puntos desde una marca temporal previa (`desde_timestamp` opcional).
    - Entonces el sistema entrega la lista de coordenadas registradas para dibujar el polyline continuo en el mapa.

#### Especificación Técnica Prevista
- **Endpoint Última Ubicación:** `GET /api-paseape/cliente/solicitudes/{uuid}/ubicacion-actual`
- **Endpoint Recorrido Histórico / Breadcrumbs:** `GET /api-paseape/cliente/solicitudes/{uuid}/ruta?desde={iso_utc_datetime}`
- **Contratos DTO Previstos (`infrastructure.dto.response`):**
    - `UbicacionPaseoResDto`:
        - `uuid`
        - `latitud`
        - `longitud`
        - `precision_metros`
        - `velocidad_ms`
        - `rumbo_grados`
        - `fecha_hora_gps`
        - `hace_cuantos_segundos` (cálculo de frescura respecto al instante actual del servidor)
    - `RutaPaseoResDto`:
        - `solicitud_uuid`
        - `estado_paseo_id`
        - `estado_paseo_descripcion`
        - `ultima_ubicacion` (`UbicacionPaseoResDto`)
        - `puntos_recorrido` (`List<UbicacionPaseoResDto>`)
- **Reglas del Servicio:**
    - Aislamiento Multitenant: La solicitud debe pertenecer estrictamente al cliente en sesión.
    - Si el paseo está `EN_CURSO` pero el paseador aún no ha transmitido coordenadas GPS, responde exitosamente con `ultima_ubicacion = null` y mensaje informativo `"A la espera de la primera señal GPS del paseador"`.

---

## 4. Matriz de Endpoints del Sprint 3

| Método | Endpoint | Rol Requerido | Descripción |
| :---: | :--- | :---: | :--- |
| `PATCH` | `/api-paseape/paseador/solicitudes/{uuid}/iniciar` | `PASEADOR` | Inicia el servicio confirmado, fija hora real y pasa a `EN_CURSO` (HU27, HU23). |
| `POST` | `/api-paseape/paseador/solicitudes/{uuid}/ubicacion` | `PASEADOR` | Registra una coordenada GPS individual del recorrido activo (HU26). |
| `POST` | `/api-paseape/paseador/solicitudes/{uuid}/ubicaciones/batch` | `PASEADOR` | Ingesta en lote de telemetría geoespacial en segundo plano (HU26). |
| `GET` | `/api-paseape/cliente/solicitudes/{uuid}/ubicacion-actual` | `CLIENTE` | Retorna la última posición GPS conocida del paseador para el cliente (HU24). |
| `GET` | `/api-paseape/cliente/solicitudes/{uuid}/ruta` | `CLIENTE` | Retorna el historial de coordenadas para pintar el polyline en el mapa (HU24). |

---

## 5. Estrategia de Polling y Trazabilidad Geoespacial

Para conservar la arquitectura sin estado (Stateless) y evitar la complejidad de infraestructura de WebSockets dedicados en esta etapa del proyecto:
1. **Transmisión del Paseador (Push de Datos):**
    - La app del paseador ejecuta un worker en segundo plano con intervalos de captura cada 10 a 15 segundos.
    - Envía paquetes de telemetría hacia `POST /paseador/solicitudes/{uuid}/ubicacion`.
2. **Supervisión del Dueño (Short Polling del Cliente):**
    - Mientras la pantalla de mapa esté visible en la app del cliente, se ejecuta un polling liviano cada 8 a 10 segundos hacia `GET /cliente/solicitudes/{uuid}/ubicacion-actual`.
    - Para la ruta completa, solicita `GET /cliente/solicitudes/{uuid}/ruta` al abrir la vista y luego actualiza diferencialmente con el último punto devuelto.

---

## 6. Checklist de Construcción por Capas (DDD)

1. **Base de Datos (Flyway):**
    - Diseñar y aplicar migración `V2.2.0__create_solicitud_paseo_ubicacion_schema.sql`.
2. **Capa Dominio (`domain.entity`):**
    - Crear entidad pura `SolicitudPaseoUbicacion.java` con Lombok, `@Builder` y constructor por defecto.
3. **Capa Infraestructura JPA (`infrastructure.entity`):**
    - Crear `SolicitudPaseoUbicacionEntity.java` con `@Entity`, `@Table` y `@SQLRestriction("estado = 1")`.
4. **Capa de Repositorios:**
    - Contrato puro: `ISolicitudPaseoUbicacionRepository.java` (retorno directo `T` o `null`, cero `Optional`).
    - JPA: `ISolicitudPaseoUbicacionJpaRepository.java` con `@Query` optimizada para el top 1 ordenado por `fechaHoraGps DESC`.
    - Implementación: `SolicitudPaseoUbicacionRepository.java`.
5. **Mappers MapStruct:**
    - `ISolicitudPaseoUbicacionDboMapper.java` (Entity <-> Dominio).
    - `ISolicitudPaseoUbicacionDtoMapper.java` (Dominio <-> DTO).
6. **Capa DTO (`infrastructure.dto`):**
    - Request: `RegistrarUbicacionPaseoReqDto`, `RegistrarUbicacionesBatchReqDto`.
    - Response: `UbicacionPaseoResDto`, `RutaPaseoResDto`.
7. **Capa Validadora (`infrastructure.validator`):**
    - Crear `UbicacionPaseoValidator.java` validando rangos geoespaciales y no vacíos.
8. **Capa de Negocio (`application.service`):**
    - Extender `SolicitudPaseoService` o crear `UbicacionPaseoService` con transaccionalidad `@Transactional(rollbackFor = Exception.class)`.
9. **Capa Controladora (`infrastructure.controller`):**
    - Integrar endpoints en `SolicitudPaseoPaseadorController` y `SolicitudPaseoClienteController` retornando la envoltura estándar `ResponseDto<T>`.