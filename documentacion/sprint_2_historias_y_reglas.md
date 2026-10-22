# Especificaciones y Reglas de Negocio - SPRINT 2 (api-paseape)

Este documento detalla los requerimientos funcionales, el modelo de datos, las reglas de negocio y el avance de las Historias de Usuario correspondientes al **SPRINT 2** del backend **api-paseape**.

---

## 1. Alcance del Sprint 2

El Sprint 2 tiene como objetivo habilitar el núcleo operativo y transaccional de la plataforma *PaseaPe*:
- Gestión de disponibilidad semanal de los paseadores (Completada).
- Búsqueda y filtrado dinámico de paseadores por ubicación, horario y cupo disponible por fecha (Completada).
- Validación de identidad y documentos oficiales (DNI con RENIEC/ApisPerú condicional por entorno, Carnet de Extranjería y Pasaporte) (Completada).
- Flujo de creación y contratación de paseos: modalidad inmediata ("Tipo Taxi" - HU15) y modalidad programada para fechas futuras (HU16-HU19).
- Soporte para paseos con múltiples mascotas (relación N:M).
- Ciclo de vida completo del servicio de paseo (Aceptar, Rechazar, Iniciar, Finalizar, Cancelar - HU21).
- Sistema de notificaciones internas modulares con soporte para Short Polling y alertas por correo vía Brevo (HU22, HU23).
- **Identificación pública:** Todos los contratos de entrada (`*ReqDto`) y salida (`*ResDto`) emplean exclusivamente `uuid` en lugar de IDs numéricos internos.

---

## 2. Modelo de Base de Datos (Sincronizado hasta `V2.0.3`)

### 2.1. Modificación a Tablas Existentes
- **`paseadores`**:
  - Incorporación de `max_mascotas_predeterminado TINYINT NOT NULL DEFAULT 3 AFTER tarifa_hora_pen`. Define la capacidad base simultánea que tiene el paseador por bloque horario.

### 2.2. Catálogos del Sprint 2
- **`dia_semana`**:
  - Representación ISO-8601: `1: LUNES`, `2: MARTES`, `3: MIÉRCOLES`, `4: JUEVES`, `5: VIERNES`, `6: SÁBADO`, `7: DOMINGO`.
- **`bloque_horario`**:
  - 17 franjas horarias fijas de 1 hora entre las 06:00 y las 23:00 (`B06_07` a `B22_23`). Cada registro cuenta con campos `hora_inicio` y `hora_fin` (tipo `TIME`).
- **`tipo_duracion_paseo`**:
  - `1`: 30 MINUTOS (Ocupa 1 bloque).
  - `2`: 60 MINUTOS (Ocupa 1 bloque).
  - `3`: 90 MINUTOS (Ocupa 2 bloques contiguos).
  - `4`: 120 MINUTOS (Ocupa 2 bloques contiguos).
- **`tipo_modalidad_paseo`**:
  - `1: PROGRAMADO`
  - `2: INMEDIATO`
- **`tipo_metodo_pago`**:
  - `1: YAPE / PLIN`
  - `2: EFECTIVO`
- **`solicitud_paseo_estado`**:
  - `1: PENDIENTE`
  - `2: ACEPTADA`
  - `3: RECHAZADA`
  - `4: CANCELADA`
  - `5: EN_CURSO`
  - `6: FINALIZADA`
  - `7: VENCIDA`
  - `8: SOLICITADA_BUSCANDO` (exclusivo para modalidad inmediata)
- **`tipo_notificacion`**:
  - `1: SOLICITUD_RECIBIDA`
  - `2: SOLICITUD_ACEPTADA`
  - `3: SOLICITUD_RECHAZADA`
  - `4: PASEO_INICIADO`
  - `5: PASEO_FINALIZADO`
  - `6: SOLICITUD_CANCELADA`

### 2.3. Tablas Transaccionales
1. **`disponibilidad_paseador`**:
   - Matriz atómica con clave única `(paseador_id, dia_semana_id, bloque_horario_id)`.
   - Campos: `max_mascotas` (cupo específico para el bloque), `activo` (booleano), `estado` (borrado lógico).
2. **`solicitud_paseo`**:
   - Cabecera del servicio de paseo.
   - Campos principales: `uuid`, `codigo_reserva`, `tipo_modalidad_paseo_id`, `tipo_metodo_pago_id`, `cliente_id`, `paseador_id` (nullable para modalidad inmediata), `distrito_id`, `direccion_recojo`, `referencia_direccion`, `fecha_programada`, `bloque_horario_id`, `tipo_duracion_paseo_id`, `tarifa_hora_pen_snapshot`, `monto_total`, `solicitud_paseo_estado_id`, `fecha_hora_aceptada`, `fecha_hora_inicio_real`, `fecha_hora_fin_real`, `motivo_rechazo_cancelacion`.
3. **`solicitud_paseo_mascota`**:
   - Tabla asociativa N:M que relaciona `solicitud_paseo_id` con `mascota_id`. Permite que una solicitud atienda una o múltiples mascotas simultáneamente.
4. **`notificaciones`**:
   - Alertas internas para clientes y paseadores.
   - Campos: `uuid`, `remitente_usuario_id` (nullable para eventos del sistema), `destinatario_usuario_id`, `tipo_notificacion_id`, `titulo`, `mensaje`, `referencia_id`, `referencia_tipo`, `leido` (booleano), `fecha_leido`.

---

## 3. Historias de Usuario Desarrolladas y Estado de Avance

### A. HU13 – Gestión de la Disponibilidad del Paseador (COMPLETADA)
* **Objetivo:** Permitir al paseador definir su cuadrícula semanal de atención, capacidad predeterminada y modificar o cancelar bloques específicos.
* **Cómo se desarrolló:**
  * **Controlador:** `DisponibilidadPaseadorController` (`/paseador/disponibilidad`).
  * `GET /api-paseape/paseador/disponibilidad`: Obtiene la disponibilidad activa semanal del paseador en sesión.
  * `PUT /api-paseape/paseador/disponibilidad`: Sincronización masiva de la matriz semanal (`GuardarDisponibilidadReqDto`).
  * `PATCH /api-paseape/paseador/disponibilidad/bloque`: Modificación unitaria de un bloque con 3 acciones exclusivas: `GUARDAR_CUPO`, `REESTABLECER_PREDETERMINADO`, `CANCELAR` (validando que no existan reservas futuras activas).
  * `PATCH /api-paseape/paseador/disponibilidad/capacidad-base`: Actualiza `max_mascotas_predeterminado` en la entidad del paseador.
  * **Capas construidas:** `DisponibilidadPaseadorService`, `DisponibilidadPaseadorValidator`, `IDisponibilidadPaseadorRepository`, `IDisponibilidadPaseadorJpaRepository`, mappers y DTOs correspondientes.
* **Cómo funciona:** La disponibilidad semanal funciona como una plantilla recurrente vinculada a `dia_semana_id` y `bloque_horario_id`, definiendo qué días atiende el paseador y cuál es su tope máximo de mascotas por bloque.

---

### B. HU12 – Búsqueda de Paseadores y Disponibilidad Dinámica con Ocupación de Cupos (COMPLETADA)
* **Objetivo:** Permitir al cliente buscar paseadores cercanos en uno o varios distritos y, al consultar el perfil de un paseador, ver exactamente qué bloques están ocupados o agotados para una fecha específica sin esperar al final del flujo para recibir un error.
* **Cómo se desarrolló:**
  1. **Refactorización de `GET /api-paseape/paseadores` (`obtenerPaseadores`):**
     * Admite búsqueda por múltiples distritos (`@RequestParam List<Integer> distrito_ids`).
     * Soporte de fallback automático: Si el frontend no envía distritos, el backend inspecciona el usuario autenticado (`Principal`), consulta su perfil de `Cliente` y filtra automáticamente por su distrito por defecto.
     * Filtro estricto de seguridad: Solo lista paseadores verificados y aprobados (`paseador_estado_verificacion_id = 3` / `Constant.ID_APROBADO`) con `estado = 1`.
  2. **Refactorización de `GET /api-paseape/paseadores/{uuid}` (`obtenerPerfil`):**
     * Nuevo DTO compuesto de respuesta: `PaseadorDetalleResDto`, que encapsula los datos del paseador (`UsuarioResDto`) y su disponibilidad (`DisponibilidadPaseadorResDto`).
     * Incorporación del parámetro opcional `fecha` (`?fecha=YYYY-MM-DD`).
     * Soporte defensivo para resolver al paseador tanto si se envía su `paseador_uuid` como su `usuario_uuid`.
  3. **Motor de Cálculo Dinámico de Cupos:**
     * En `ISolicitudPaseoJpaRepository`: Nueva consulta agrupada JPQL `countMascotasOcupadasAgrupadoPorBloque(...)` que calcula en **una sola consulta** el total de mascotas reservadas en esa fecha exacta para solicitudes activas (`PENDIENTE [1]`, `ACEPTADA [2]`, `EN_CURSO [5]`).
     * En `DisponibilidadPaseadorService.obtenerDisponibilidadPorPaseadorUuidYFecha(...)`:
       * Cruza el día de la semana de la fecha (`fecha.getDayOfWeek()`) con la plantilla activa del paseador.
       * Calcula `mascotas_ocupadas = COUNT(mascotas)`, `cupos_disponibles = max_mascotas - mascotas_ocupadas`.
       * Determina `esta_lleno = (cupos_disponibles <= 0)`.
       * Normalización temporal: Si la fecha es hoy, valida contra la hora civil de Lima (`America/Lima`); si la hora de inicio del bloque ya transcurrió o la fecha es pasada, fija `disponible = false`.
* **Cómo funciona:**
  * **Listado:** El cliente consulta `/paseadores?distrito_ids=21,31` y recibe todos los paseadores aprobados de esas zonas.
  * **Detalle por fecha:** Al seleccionar un paseador y elegir una fecha en el calendario (`/paseadores/{uuid}?fecha=2026-10-26`), la API responde cada slot con campos calculados:
    ```json
    {
      "bloque_codigo": "B08_09",
      "hora_inicio": "08:00",
      "hora_fin": "09:00",
      "max_mascotas": 3,
      "mascotas_ocupadas": 3,
      "cupos_disponibles": 0,
      "esta_lleno": true,
      "disponible": false
    }
    ```
    El frontend puede pintar en gris o inhabilitar inmediatamente los bloques llenos o vencidos.

---

### C. Módulo Transversal: Validación de Estructura de Documentos y Verificación DNI con RENIEC (COMPLETADA)
* **Objetivo:** Asegurar que los paseadores solo se registren con documentos oficiales válidos, evitando números ficticios o malformados.
* **Cómo se desarrolló:**
  1. **Reglas en `AuthValidator.java`:**
     * **DNI (1):** Prevalida en memoria 8 dígitos numéricos (`^[0-9]{8}$`).
     * **Carnet de Extranjería (2):** Valida 9 dígitos numéricos (`^[0-9]{9}$`) con auto-padding de `'0'` a la izquierda para formatos legacy de 8 dígitos.
     * **Pasaporte (3):** Valida exactamente 9 caracteres alfanuméricos de la carátula (`^[A-Z0-9]{9}$`) normalizado a mayúsculas.
  2. **Nuevo Servicio `ApisPeruService.java`:**
     * Encapsula la consulta HTTP vía `RestClient` hacia `https://dniruc.apisperu.com/api/v1/dni/{dni}` con Bearer token.
     * **Comportamiento condicional por entorno (`app.env`):**
       * En `local` o `dev` (`APP_ENV=local`): Omite la consulta remota a RENIEC para ahorrar cuotas y permitir desarrollo fluido con DNIs de prueba.
       * En producción (`APP_ENV=prod`): Realiza la verificación obligatoria contra RENIEC. Si el DNI no existe o es inválido, rechaza el registro con error 400.
* **Cómo funciona:** Durante `POST /auth/register/local` o `POST /auth/register/google`, el validador revisa la estructura según el tipo de documento y, para DNIs en producción, certifica su existencia real antes de persistir la cuenta.

---

### D. HU15 – Solicitar Paseo Inmediato ("Modalidad Tipo Taxi") (COMPLETADA)
* **Objetivo:** Permitir al dueño de una mascota solicitar un paseo para atención inmediata sin tener que elegir a un paseador específico por adelantado, proponiendo él mismo la tarifa por hora que desea pagar para que cualquier paseador verificado y disponible en su distrito pueda tomar el servicio con rapidez.
* **Cómo se desarrolló:**
    1. **Endpoints Desarrollados:**
        * `POST /api-paseape/cliente/solicitudes/inmediata`: Permite al cliente autenticado registrar una nueva solicitud bajo demanda enviando su oferta de tarifa por hora (`tarifa_hora_pen`).
        * `GET /api-paseape/paseador/solicitudes/disponibles`: Permite al paseador consultar la lista de paseos inmediatos pendientes dentro de su distrito de cobertura con la tarifa propuesta por el cliente.
        * `PATCH /api-paseape/paseador/solicitudes/{uuid}/tomar`: Permite al paseador aceptar y adjudicarse el paseo en tiempo real.
    2. **Validaciones en `SolicitudPaseoValidator`:**
        * Revisa que la lista de mascotas no esté vacía, no contenga duplicados y que cada código de mascota tenga un formato de UUID válido.
        * Valida que el campo `tarifa_hora_pen` sea obligatorio y mayor a 0 (`> 0`).
        * Verifica que los campos de distrito, duración del paseo, método de pago y dirección de recojo sean correctos y obligatorios.
    3. **Lógica de Negocio y Reglas (`SolicitudPaseoService`):**
        * **Comprobación de mascotas:** Confirma que todas las mascotas seleccionadas pertenezcan al cliente en sesión y se encuentren activas en el sistema.
        * **Tarifa propuesta y cálculo total:** El cliente define en el pedido la tarifa por hora que está dispuesto a pagar (`tarifa_hora_pen`), la cual se congela en `tarifa_hora_pen_snapshot` y sirve de base para calcular el costo total multiplicando por los minutos de duración (30, 60, 90 o 120) y la cantidad de mascotas:
          `monto_total = (tarifa_hora_pen / 60) * minutos * total_mascotas`
        * **Código de reserva:** Genera automáticamente un código único reconocible con formato `PAS-INM-YYYYMM-XXXX` (por ejemplo, `PAS-INM-202610-A9F3`).
        * **Estado inicial:** Se registra con fecha del día actual, sin paseador asignado (`paseador_id = null`) y en estado `SOLICITADA_BUSCANDO` (id = 8).
        * **Asignación atómica (sin duplicidad ni choques):** La aceptación del paseo por parte del paseador se ejecuta mediante una actualización directa en la base de datos (`UPDATE ... WHERE paseador IS NULL AND estado = SOLICITADA_BUSCANDO`). De esta forma, si dos paseadores intentan presionar "Aceptar" al mismo tiempo, el sistema se lo entrega únicamente al primero y le muestra un mensaje claro al segundo indicando que el servicio ya fue tomado por otro paseador.
        * **Notificación al cliente:** Al momento de concretarse la adjudicación, se crea una notificación interna en el sistema para que el cliente sepa de inmediato qué paseador atenderá a sus mascotas.
* **Cómo funciona:**
    * **El cliente solicita el paseo:** Entra a la app, elige sus mascotas, la duración del paseo, su método de pago, propone la tarifa por hora que desea pagar y confirma la dirección de recojo.
    * **Los paseadores ven la solicitud:** A todos los paseadores autorizados de ese distrito les aparece la solicitud en su lista de disponibles con la tarifa propuesta por el cliente, el monto total calculado y los detalles del paseo y de las mascotas.
    * **Un paseador toma el servicio:** El primer paseador que pulse en aceptar se adjudica el servicio; el estado cambia a `ACEPTADA` y el cliente recibe una notificación al instante con los datos de su paseador.

---

## 4. Historias de Usuario Pendientes

### HU16 – Programar un Paseo para una Fecha Futura
- **Objetivo:** Como dueño de una mascota, quiero seleccionar una fecha y hora futura para solicitar un paseo, para organizar el cuidado de mi mascota con anticipación.
- **Reglas de Negocio:**
  1. **Validación Temporal:** La fecha y bloque seleccionados no pueden ser anteriores a la fecha y hora civil actual de Lima (`America/Lima`).
  2. **Validación de Cupo:**
     - Debe existir disponibilidad activa del paseador para el `dia_semana` correspondiente a la `fecha_programada` y el `bloque_horario` solicitado.
     - `mascotas_actuales + nuevas_mascotas <= max_mascotas_bloque`.
  3. **Congelamiento de Tarifa:** Se almacena `tarifa_hora_pen_snapshot = paseador.getTarifaHoraPen()`.
  4. **Cálculo de Monto Total:** Basado en tarifa, duración y número de mascotas.
  5. **Estado Inicial:** Registrada en estado `PENDIENTE` (id = 1).
  6. **Notificación:** Se notifica al paseador (`SOLICITUD_RECIBIDA`).

---

### HU17 – Seleccionar Mascotas para el Paseo (Múltiples Mascotas N:M)
- **Objetivo:** Como dueño de mascotas, quiero elegir cuáles de mis mascotas registradas participarán en el paseo.
- **Reglas de Negocio:**
  1. El cliente envía `mascota_uuids: List<String>`.
  2. El backend valida que la lista no esté vacía, no contenga duplicados y que **todas** las mascotas pertenezcan al cliente autenticado y tengan estado activo (`estado = 1`).
  3. Se persiste un registro en `solicitud_paseo_mascota` por cada mascota vinculada a la solicitud.

---

### HU18 – Seleccionar Duración y Bloques Contiguos
- **Objetivo:** Como dueño, quiero elegir la duración del servicio (30, 60, 90 o 120 minutos).
- **Reglas de Negocio:**
  1. Duraciones de 30 y 60 minutos ocupan **1 bloque horario**.
  2. Duraciones de 90 y 120 minutos ocupan **2 bloques horarios consecutivos**:
     - Se valida que el segundo bloque exista (campo `orden` consecutivo en `bloque_horario`).
     - El paseador debe tener disponibilidad activa y cupo suficiente en **ambos bloques horarios**.
  3. **Fórmula Tarifaria:**
     `monto_total = (tarifa_hora_pen_snapshot / 60) * minutos_duracion * total_mascotas`

---

### HU19 – Confirmación de Solicitud y Método de Pago
- **Objetivo:** Como dueño, quiero revisar los detalles antes de enviar y confirmar el método de pago (Yape/Plin o Efectivo).
- **Reglas de Negocio:**
  1. Se valida el método de pago mediante su UUID de catálogo (`tipo_metodo_pago_uuid`).
  2. Se genera un código de reserva único con formato determinista:
     `PAS-YYYYMM-XXXX` (ej. `PAS-202610-A9F3`).
  3. Se confirma la persistencia de la cabecera, los ítems de mascotas y el evento de auditoría/notificación.

---

### HU21 – Consultar y Gestionar Estados de la Solicitud
- **Objetivo:** Como dueño o paseador, quiero consultar el estado de mis servicios y transicionar sus fases según corresponda.
- **Endpoints de Consulta:**
  - `GET /api-paseape/cliente/solicitudes`: Historial y solicitudes activas del cliente.
  - `GET /api-paseape/paseador/solicitudes`: Solicitudes del paseador filtradas por estado (Pendientes, Programadas, En Curso, Historial).
  - Ambos endpoints retornan objetos con el `solicitud_uuid` y los `uuid` de entidades relacionadas.
- **Endpoints de Transición de Estado (Paseador):**
  - `PATCH /api-paseape/paseador/solicitudes/aceptar`: Pasa de `PENDIENTE` a `ACEPTADA`, registra `fecha_hora_aceptada = NOW()`.
  - `PATCH /api-paseape/paseador/solicitudes/rechazar`: Pasa de `PENDIENTE` a `RECHAZADA`, registra `motivo_rechazo_cancelacion`.
  - `PATCH /api-paseape/paseador/solicitudes/iniciar`: Pasa de `ACEPTADA` a `EN_CURSO`, registra `fecha_hora_inicio_real = NOW()`.
  - `PATCH /api-paseape/paseador/solicitudes/finalizar`: Pasa de `EN_CURSO` a `FINALIZADA`, registra `fecha_hora_fin_real = NOW()`.
- **Endpoints de Cancelación (Cliente / Paseador):**
  - `PATCH /api-paseape/solicitudes/cancelar`: Cancela una solicitud en estado `PENDIENTE` o `ACEPTADA` registrando el motivo.

---

### HU22 y HU23 – Notificaciones Transaccionales (Aceptación e Inicio de Paseo)
- **Objetivo:** Notificar al cliente en tiempo real y vía correo cuando el paseador acepte o inicie el paseo.
- **Reglas de Negocio:**
  1. Se inserta un registro en la tabla `notificaciones` con `remitente_usuario_id`, `destinatario_usuario_id`, `tipo_notificacion_id` (`2` para Aceptada, `4` para Iniciado), título y mensaje descriptivo.
  2. Se realiza el envío de correo transaccional mediante el API de Brevo con auditoría en `brevo_email`.
  3. El envío a Brevo no debe comprometer la transacción principal en caso de error externo de red o timeout.

---

## 5. Módulo de Notificaciones y Short Polling

Para garantizar una experiencia reactiva en la app móvil sin sobrecargar el servidor con WebSockets en esta etapa:

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/api-paseape/notificaciones/conteo-no-leidas` | Retorna `{ "total_no_leidas": N }`. Optimizado sobre índice `(destinatario_usuario_id, leido, estado)`. |
| `GET` | `/api-paseape/notificaciones` | Retorna lista paginada de notificaciones del usuario autenticado ordenadas por fecha descendente. Cada ítem incluye su `uuid`. |
| `PATCH` | `/api-paseape/notificaciones/leer` | Recibe `notificacion_uuid` en el request body, marca `leido = true` y fija `fecha_leido = NOW()`. |
| `PATCH` | `/api-paseape/notificaciones/marcar-todas-leidas` | Marca todas las notificaciones pendientes del usuario autenticado como leídas. |

---

## 6. Checklist de Implementación por Capas para Nuevos Módulos

Al codificar cada una de las Historias de Usuario, se debe seguir estrictamente este flujo de construcción:

1. **Dominio:** Crear la entidad pura en `domain.entity` con Lombok (sin anotaciones JPA ni `@SQLRestriction`).
2. **Infraestructura JPA:** Crear `*Entity.java` con `@Entity`, `@Table` y `@SQLRestriction("estado = 1")` (salvo catálogos).
3. **Repositorios:**
   - Crear interfaz en `application.repository.*` con retornos directos `T` o `List<T>` (sin `Optional`).
   - Crear interfaz en `infrastructure.repository.jpa.*JpaRepository`.
   - Crear implementación en `infrastructure.repository.*RepositoryImpl`.
4. **Mappers MapStruct:**
   - `infrastructure.mapper.entity.*DboMapper` (Entity <-> Dominio).
   - `infrastructure.mapper.dto.*DtoMapper` (Dominio <-> DTO).
5. **DTOs:**
   - `*ReqDto` plano sin anotaciones Jakarta + `@JsonNaming(SnakeCaseStrategy)`.
   - `*ResDto` con todos los `uuid` requeridos por el frontend.
6. **Validador:** Crear componente `@Component` en `infrastructure.validator` con comprobaciones de nulidad, rangos y formato UUID.
7. **Servicio:** Implementar caso de uso en `application.service` con `@Transactional(rollbackFor = Exception.class)` o `@Transactional(readOnly = true)`.
8. **Controlador:** Exponer endpoint en `infrastructure.controller` inyectando el validador y respondiendo `ResponseDto<T>`.
