# Especificaciones y Reglas de Negocio - SPRINT 2 (api-paseape)

Este documento detalla los requerimientos funcionales, el modelo de datos, las reglas de negocio y el avance de las Historias de Usuario correspondientes al **SPRINT 2** del backend **api-paseape**.

---

## 1. Alcance del Sprint 2

El Sprint 2 tiene como objetivo habilitar el núcleo operativo y transaccional de la plataforma *PaseaPe*:
- Gestión de disponibilidad semanal de los paseadores (Completada).
- Búsqueda y filtrado dinámico de paseadores por ubicación, horario y cupo disponible por fecha (Completada).
- Validación de identidad y documentos oficiales (DNI con RENIEC/ApisPerú condicional por entorno, Carnet de Extranjería y Pasaporte) (Completada).
- Flujo de creación, confirmación y contratación de paseos: modalidad inmediata ("Tipo Taxi" - HU15) y modalidad programada para fechas futuras con validación de bloques contiguos y método de pago (HU16, HU18, HU19) (Completada).
- Soporte para paseos con múltiples mascotas (relación N:M - HU17) (Completada).
- Bandeja operativa del paseador y gestión de estados del servicio de paseo (Aceptar, Rechazar, Descartar, Cancelar y Expiración Automática - HU20, HU21) (Completada).
- Sistema de notificaciones internas modulares con soporte para Short Polling y alertas por correo vía Brevo (HU22, HU23).
- **Identificación pública:** Todos los contratos de entrada (`*ReqDto`) y salida (`*ResDto`) emplean exclusivamente `uuid` en lugar de IDs numéricos internos.

---

## 2. Modelo de Base de Datos (Sincronizado hasta `V2.0.5`)

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
   - Campos principales: `uuid`, `codigo_reserva`, `tipo_modalidad_paseo_id`, `tipo_metodo_pago_id`, `cliente_id`, `paseador_id` (nullable para modalidad inmediata), `distrito_id`, `direccion_recojo`, `referencia_direccion`, `fecha_programada`, `bloque_horario_id`, `tipo_duracion_paseo_id`, `tarifa_hora_pen_snapshot`, `monto_total`, `solicitud_paseo_estado_id`, `fecha_hora_aceptada`, `fecha_hora_inicio_real`, `fecha_hora_fin_real`, `motivo_rechazo_cancelacion`, `hora_vencimiento` (DATETIME nullable, asignado como NOW() + 5 min para solicitudes inmediatas - V2.0.5).
3. **`solicitud_paseo_mascota`**:
   - Tabla asociativa N:M que relaciona `solicitud_paseo_id` con `mascota_id`. Permite que una solicitud atienda una o múltiples mascotas simultáneamente.
4. **`notificaciones`**:
   - Alertas internas para clientes y paseadores.
   - Campos: `uuid`, `remitente_usuario_id` (nullable para eventos del sistema), `destinatario_usuario_id`, `tipo_notificacion_id`, `titulo`, `mensaje`, `referencia_id`, `referencia_tipo`, `leido` (booleano), `fecha_leido`.
5. **`solicitud_paseo_descarte` (V2.0.4)**:
   - Registro de descarte individual de solicitudes inmediatas por parte de los paseadores. Permite que un paseador rechace ver una solicitud abierta sin afectar la disponibilidad de la misma para el resto de paseadores en el distrito.
   - Campos: `uuid`, `solicitud_paseo_id`, `paseador_id`, `motivo_descarte` (nullable), `estado` (borrado lógico).
   - Restricción única: `(solicitud_paseo_id, paseador_id)`.

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
  2. **Consulta de Perfil de Paseador `GET /api-paseape/paseadores/{uuid}` (`obtenerPerfil`):**
     * Retorna exclusivamente el detalle y perfil completo del paseador (`UsuarioResDto`), conteniendo sus datos personales, biografía, tarifa, calificación promedio y distrito de cobertura.
     * Desacoplamiento de disponibilidad: No incluye la matriz de disponibilidad ni recibe parámetro `fecha`, manteniendo el endpoint liviano y enfocado únicamente en la información del perfil del paseador (la disponibilidad de bloques y cupos se delega al endpoint especializado `GET /api-paseape/paseadores/{paseadorUuid}/disponibilidad`).
     * Soporte defensivo para resolver al paseador tanto si se envía su `paseador_uuid` como su `usuario_uuid`.
  3. **Nuevo Endpoint Especializado para la App Móvil: `GET /api-paseape/paseadores/{paseadorUuid}/disponibilidad` (`obtenerDisponibilidadPorFecha`):**
     * Diseñado específicamente para alimentar el carrusel de fechas del perfil del paseador en la app móvil.
     * **Parámetros:**
       * `paseadorUuid`: `@PathVariable` String con validación estricta de no nulidad, no vacío y formato canónico UUID (`Constant.REGEX_UUID`).
       * `fecha`: `@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha`. Si no se envía, toma por defecto la fecha civil actual en Lima (`Constant.ZONE_ID_LIMA`). Si se envía, `PaseadorValidator` verifica que no sea anterior a la fecha actual de Lima.
     * **Contratos DTO de Respuesta (`infrastructure.dto.response`):**
       * `DisponibilidadPaseadorFechaResDto`: `paseador_uuid`, `fecha`, `dia_semana_id`, `dia_semana_descripcion`, `bloques`.
       * `DisponibilidadBloqueFechaResDto`: `bloque_horario_id`, `bloque_codigo`, `hora_inicio`, `hora_fin`, `max_mascotas`, `mascotas_ocupadas`, `cupos_disponibles`, `esta_lleno`, `disponible`, `motivo_no_disponible`.
     * **Lógica de Negocio (`PaseadorService.obtenerDisponibilidadPorFecha`):**
       * Comprueba que el paseador exista, esté activo (`estado = 1`) y cuente con verificación aprobada (`estado_verificacion_id = 3` / `Constant.ID_APROBADO`).
       * Obtiene la plantilla recurrente activa del paseador para ese día (`IDisponibilidadPaseadorRepository.findAllByPaseadorIdAndDiaSemanaId`) ordenada por `bloqueHorario.orden ASC`.
       * Consulta en una única llamada JPQL las mascotas ocupadas por bloque (`solicitudPaseoRepository.countMascotasOcupadasAgrupadoPorBloque`) filtrando solicitudes activas (`PENDIENTE [1]`, `ACEPTADA [2]`, `EN_CURSO [5]`).
       * Evalúa cupos disponibles: `cupos_disponibles = max(0, capacidad_maxima - ocupadas)`. Si se agotan, marca `esta_lleno = true`.
       * Control de expiración horaria: Si la fecha consultada es hoy en Lima, cualquier bloque cuya `hora_inicio` no sea posterior a `LocalTime.now(ZoneId.of(Constant.ZONE_ID_LIMA))` se marca con `disponible = false` y `motivo_no_disponible = "HORARIO_EXPIRADO"`. Si no expiró pero no tiene cupos, asigna `"SIN_CUPOS"`. Si está libre, `motivo_no_disponible = null`.
  4. **Motor de Cálculo Dinámico de Cupos:**
     * En `ISolicitudPaseoJpaRepository`: Nueva consulta agrupada JPQL `countMascotasOcupadasAgrupadoPorBloque(...)` que calcula en **una sola consulta** el total de mascotas reservadas en esa fecha exacta para solicitudes activas (`PENDIENTE [1]`, `ACEPTADA [2]`, `EN_CURSO [5]`).
     * En `DisponibilidadPaseadorService.obtenerDisponibilidadPorPaseadorUuidYFecha(...)`:
       * Cruza el día de la semana de la fecha (`fecha.getDayOfWeek()`) con la plantilla activa del paseador.
       * Calcula `mascotas_ocupadas = COUNT(mascotas)`, `cupos_disponibles = max_mascotas - mascotas_ocupadas`.
       * Determina `esta_lleno = (cupos_disponibles <= 0)`.
       * Normalización temporal: Si la fecha es hoy, valida contra la hora civil de Lima (`America/Lima`); si la hora de inicio del bloque ya transcurrió o la fecha es pasada, fija `disponible = false`.
* **Cómo funciona:**
  * **Listado:** El cliente consulta `/paseadores?distrito_ids=21,31` y recibe todos los paseadores aprobados de esas zonas.
  * **Detalle por fecha (Carrusel Móvil):** Al seleccionar una fecha en el carrusel (`/paseadores/{paseadorUuid}/disponibilidad?fecha=2026-10-26`), la API responde cada slot con campos calculados:
    ```json
    {
      "status_code": 200,
      "code": "00",
      "message": "Disponibilidad del paseador por fecha obtenida exitosamente",
      "response": {
        "paseador_uuid": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
        "fecha": "2026-10-26",
        "dia_semana_id": 1,
        "dia_semana_descripcion": "LUNES",
        "bloques": [
          {
            "bloque_horario_id": 3,
            "bloque_codigo": "B08_09",
            "hora_inicio": "08:00",
            "hora_fin": "09:00",
            "max_mascotas": 3,
            "mascotas_ocupadas": 3,
            "cupos_disponibles": 0,
            "esta_lleno": true,
            "disponible": false,
            "motivo_no_disponible": "SIN_CUPOS"
          }
        ]
      }
    }
    ```

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

### E. HU20 – Bandeja de Solicitudes del Paseador (4 Pestañas Paginadas) y Descarte de Inmediatas (COMPLETADA)
* **Objetivo:** Permitir al paseador gestionar de manera unificada y eficiente todo su flujo de trabajo operativo desde la app móvil a través de 4 pestañas paginadas (`INMEDIATAS`, `MIS_PASEOS`, `EN_CURSO` e `HISTORIAL`), incorporando la capacidad de descartar solicitudes inmediatas sin afectar a otros paseadores y la expiración automática por inactividad.
* **Cómo se desarrolló:**
  1. **Endpoints Desarrollados:**
     * `GET /api-paseape/paseador/solicitudes`: Endpoint unificado paginado con query params:
       * `tab`: (`INMEDIATAS`, `MIS_PASEOS`, `EN_CURSO`, `HISTORIAL`, por defecto `INMEDIATAS`).
       * `page`: Número de página (0-indexed, default `0`).
       * `size`: Tamaño de página (default `10`, máx `50`).
       * Retorna la envoltura `ResponseDto<PageResponseDto<SolicitudPaseoResDto>>`.
     * `PATCH /api-paseape/paseador/solicitudes/{uuid}/descartar`: Permite al paseador en sesión descartar una solicitud inmediata pendiente de atención. Retorna `ResponseDto<Void>`.
     * `PATCH /api-paseape/paseador/solicitudes/{uuid}/aceptar`: Permite al paseador aceptar una solicitud programada en estado `PENDIENTE`. Pasa a estado `ACEPTADA` (2), fija `fecha_hora_aceptada = NOW()` y notifica al cliente (`SOLICITUD_ACEPTADA`).
     * `PATCH /api-paseape/paseador/solicitudes/{uuid}/rechazar`: Permite al paseador declinar una solicitud programada en estado `PENDIENTE` con motivo opcional. Pasa a estado `RECHAZADA` (3), libera el cupo inmediatamente y notifica al cliente (`SOLICITUD_RECHAZADA`).
     * `PATCH /api-paseape/paseador/solicitudes/{uuid}/cancelar`: Permite al paseador cancelar un paseo previamente confirmado (`ACEPTADA`) que no haya iniciado aún, exigiendo un comentario/motivo obligatorio (`motivo` entre 10 y 500 caracteres). Pasa a estado `CANCELADA` (4), libera el cupo de inmediato y notifica al cliente (`SOLICITUD_CANCELADA`).
  2. **Estructura de las 4 Pestañas:**
     * **Pestaña 1 - `INMEDIATAS`:** Muestra solicitudes bajo demanda abiertas (`tipo_modalidad_paseo_id = 2 [INMEDIATO]`, `solicitud_paseo_estado_id = 8 [SOLICITADA_BUSCANDO]`, `paseador_id IS NULL`) ubicadas en el distrito de cobertura del paseador y excluyendo las que el paseador ya haya descartado (`solicitud_paseo_descarte`).
     * **Pestaña 2 - `MIS_PASEOS`:** Agrupa solicitudes asignadas al paseador (`paseador_id = :paseadorId`) en estados activos preparatorios: `PENDIENTE` (1) [solicitudes programadas entrantes] y `ACEPTADA` (2) [tanto programadas aceptadas como inmediatas tomadas]. Ordenadas cronológicamente por `fecha_programada ASC, creado_el DESC`.
     * **Pestaña 3 - `EN_CURSO`:** Paseos activos en ejecución asignados al paseador (`solicitud_paseo_estado_id = 5 [EN_CURSO]`), ordenados por `fecha_hora_inicio_real DESC`.
     * **Pestaña 4 - `HISTORIAL`:** Registro histórico de solicitudes asignadas en estados terminales: `FINALIZADA` (6), `CANCELADA` (4), `RECHAZADA` (3) y `VENCIDA` (7), ordenadas por `actualizado_el DESC`.
  3. **Mecanismo de Descarte Individual:**
     * Al pulsar el botón "Rechazar" en una solicitud inmediata, se registra la tupla `(solicitud_paseo_id, paseador_id)` en `solicitud_paseo_descarte` con `motivo_descarte = null` (o texto descriptivo opcional).
     * La solicitud desaparece inmediatamente de la pestaña `INMEDIATAS` del paseador, permaneciendo visible y adjudicable para los demás paseadores del distrito.
  4. **Job Scheduled de Expiración Automática (5 Minutos):**
     * En `AppTaskScheduler`: Tarea programada periódica cada 60 segundos (`expirarSolicitudesInmediatasVencidas`) que evalúa solicitudes inmediatas con `solicitud_paseo_estado_id = 8 [SOLICITADA_BUSCANDO]` comparando `hora_vencimiento <= LocalDateTime.now()`.
     * Al crearse una solicitud inmediata, el campo `hora_vencimiento` se calcula y persiste como `LocalDateTime.now().plusMinutes(5)` (para solicitudes programadas permanece `null`).
     * Si no han sido tomadas y se alcanza la hora límite, la consulta JPQL actualiza el estado a `VENCIDA` (id = 7) sin manipular manualmente columnas de auditoría, permitiendo que Spring Data JPA gestione los metadatos de auditoría automáticamente.
  5. **DTO Genérico de Paginación:**
     * Se incorporó `PageResponseDto<T>` en `infrastructure.dto.response` con metadatos: `items`, `page`, `size`, `total_elements`, `total_pages`, `is_first`, `is_last`, `has_next` y `has_previous`.

---

### F. HU16 – Programar un Paseo para una Fecha Futura (COMPLETADA)
* **Objetivo:** Permitir al dueño de una mascota seleccionar una fecha y bloque horario futuro para solicitar un paseo con un paseador específico y organizar el cuidado de sus mascotas con anticipación.
* **Cómo se desarrolló:**
  1. **Endpoint Desarrollado:**
     * `POST /api-paseape/cliente/solicitudes/programada` (`SolicitudPaseoClienteController.crearSolicitudProgramada`).
     * **Contrato de Entrada (`CrearSolicitudProgramadaReqDto`):** `paseador_uuid`, `mascota_uuids`, `distrito_id`, `fecha_programada`, `bloque_horario_id`, `tipo_duracion_paseo_id`, `tipo_metodo_pago_id`, `direccion_recojo` y `referencia_direccion`.
  2. **Validaciones en `SolicitudPaseoValidator.validarCrearSolicitudProgramada`:**
     * Comprueba obligatoriedad y formato UUID canónico (`Constant.REGEX_UUID`) de `paseador_uuid` y de cada elemento en `mascota_uuids` (sin duplicados).
     * Verifica presencia de `fecha_programada`, IDs positivos para `distrito_id`, `bloque_horario_id`, `tipo_duracion_paseo_id`, `tipo_metodo_pago_id` y longitudes máximas de `direccion_recojo` y `referencia_direccion` (255 caracteres).
  3. **Lógica de Negocio (`SolicitudPaseoService.crearSolicitudProgramada`):**
     * **Resolución defensiva del paseador:** Busca primero por `paseador_uuid` y, como respaldo, por `usuario_uuid` asociado al perfil del paseador.
     * **Estado y cobertura del paseador:** Verifica que el paseador esté activo (`estado = 1`), cuente con verificación aprobada (`estado_verificacion_id = 3` / `Constant.ID_APROBADO`), tenga tarifa por hora configurada (`> 0`) y que `distrito_id` coincida con su distrito de cobertura.
     * **Validación temporal civil (`America/Lima`):** Verifica que `fecha_programada` no sea anterior a la fecha actual de Lima (`hoyLima`). Si es para el mismo día, exige que `hora_inicio` del bloque inicial sea estrictamente posterior a la hora civil actual (`ahoraLima`).
     * **Validación de disponibilidad y cupo:** Determina el día de la semana (`dia_semana` ISO-8601), comprueba que el paseador tenga disponibilidad activa en el bloque (o bloques contiguos) y valida que `mascotas_ocupadas + nuevas_mascotas <= max_mascotas_bloque` considerando solicitudes en estados `PENDIENTE` (1), `ACEPTADA` (2) y `EN_CURSO` (5).
     * **Congelamiento de tarifa y registro:** Almacena `tarifa_hora_pen_snapshot = paseador.getTarifaHoraPen()`, calcula `monto_total`, genera el código `PAS-PRG-YYYYMM-XXXX` (`Constant.PREFIX_CODIGO_RESERVA_PROGRAMADO`), persiste la solicitud en estado `PENDIENTE` (id = 1) con `hora_vencimiento = null` y registra la notificación interna `SOLICITUD_RECIBIDA` (id = 1) para el paseador.

---

### G. HU17 – Seleccionar Mascotas para el Paseo (Múltiples Mascotas N:M) (COMPLETADA)
* **Objetivo:** Permitir al dueño de mascotas elegir cuáles de sus mascotas registradas participarán en un mismo paseo (tanto inmediato como programado).
* **Cómo se desarrolló:**
  1. **Validación Estructural (`SolicitudPaseoValidator`):**
     * Exige que `mascota_uuids` contenga al menos un elemento, que todos los identificadores cumplan con `Constant.REGEX_UUID` y que no existan UUIDs repetidos dentro de la solicitud (`Set<String>`).
  2. **Validación de Pertenencia y Estado (`SolicitudPaseoService.validarYObtenerMascotas`):**
     * Comprueba en base de datos que cada mascota exista, pertenezca estrictamente al `Cliente` autenticado y tenga estado activo (`estado = 1`).
  3. **Persistencia Relacional N:M (`solicitud_paseo_mascota`):**
     * Registra en lote (`solicitudPaseoMascotaRepository.saveAll`) un vínculo en `solicitud_paseo_mascota` por cada mascota participante y devuelve el detalle completo (`List<SolicitudMascotaResDto>`) en `SolicitudPaseoResDto`.

---

### H. HU18 – Seleccionar Duración y Bloques Contiguos (COMPLETADA)
* **Objetivo:** Permitir al dueño elegir la duración del servicio (`30`, `60`, `90` o `120` minutos) asegurando la reserva y validación de cupo en bloques horarios consecutivos cuando corresponda.
* **Cómo se desarrolló:**
  1. **Evaluación de Bloques Requeridos (`tipo_duracion_paseo`):**
     * Duraciones de `30 MINUTOS` (id = 1) y `60 MINUTOS` (id = 2) requieren **1 bloque horario** (`bloques_requeridos = 1`).
     * Duraciones de `90 MINUTOS` (id = 3) y `120 MINUTOS` (id = 4) requieren **2 bloques horarios consecutivos** (`bloques_requeridos = 2`).
  2. **Validación de Bloques Contiguos (`IBloqueHorarioRepository.findByOrden`):**
     * Cuando `bloques_requeridos == 2`, el servicio calcula `ordenSiguiente = bloqueInicial.getOrden() + 1` (validando que no supere el último bloque del día `orden = 17` / `22:00 - 23:00`).
     * Verifica que el paseador tenga disponibilidad activa y cupo suficiente (`mascotas_ocupadas + nuevas_mascotas <= max_mascotas_bloque`) en **ambos bloques horarios consecutivos**.
  3. **Fórmula Tarifaria Exacta (`calcularMontoTotal`):**
     * `monto_total = (tarifa_hora_pen_snapshot * minutos_duracion * total_mascotas) / 60` con escala de 2 decimales y redondeo `RoundingMode.HALF_UP`.

---

### I. HU19 – Confirmación de Solicitud y Método de Pago (COMPLETADA)
* **Objetivo:** Permitir al dueño revisar todos los datos finales del servicio en una pantalla de resumen previa en el frontend y confirmar el método de pago (`YAPE / PLIN` o `EFECTIVO`) al enviar la solicitud (tanto inmediata como programada) al backend.
* **Cómo se desarrolló:**
  1. **Flujo de Confirmación Final (Frontend ➔ Backend):**
     * Antes de disparar la petición HTTP hacia `POST /api-paseape/cliente/solicitudes/inmediata` o `POST /api-paseape/cliente/solicitudes/programada`, el frontend presenta una pantalla de confirmación con el resumen completo del paseo (mascotas seleccionadas, duración, dirección de recojo, fecha/bloque horario o modalidad inmediata, tarifa por hora, monto total estimado y método de pago).
  2. **Validación de Método de Pago (`SolicitudPaseoValidator`):**
     * Se valida que `tipo_metodo_pago_id` sea obligatorio y positivo (`1: YAPE / PLIN`, `2: EFECTIVO`).
  3. **Generación de Código de Reserva y Persistencia Transaccional (`SolicitudPaseoService`):**
     * Se genera un código de reserva único con formato determinista mediante `generarCodigoReserva`:
       * `PAS-INM-YYYYMM-XXXX` para paseos inmediatos (`Constant.PREFIX_CODIGO_RESERVA_INMEDIATO`).
       * `PAS-PRG-YYYYMM-XXXX` para paseos programados (`Constant.PREFIX_CODIGO_RESERVA_PROGRAMADO`).
     * Dentro de una única transacción (`@Transactional(rollbackFor = Exception.class)`), se persiste la cabecera en `solicitud_paseo`, el detalle de mascotas en `solicitud_paseo_mascota` y la notificación interna correspondiente, retornando el `SolicitudPaseoResDto` completo con todos los datos confirmados.

---

### J. HU21 – Consultar y Gestionar Estados de la Solicitud (COMPLETADA)
* **Objetivo:** Permitir tanto al paseador como al cliente consultar sus solicitudes según su ciclo operativo mediante bandejas unificadas paginadas por pestañas especializadas, y ejecutar las transiciones de estado correspondientes con validaciones rigurosas de negocio, control de motivos condicionales y emisión de notificaciones automáticas.
* **Cómo se desarrolló:**
    1. **Flujo Operativo del Paseador (`SolicitudPaseoPaseadorController`):**
        * **Endpoints de Consulta:**
            * `GET /api-paseape/paseador/solicitudes?tab={INMEDIATAS|MIS_PASEOS|EN_CURSO|HISTORIAL}&page=0&size=10`: Consulta paginada filtrada por pestaña operativa.
            * `GET /api-paseape/paseador/solicitudes/disponibles`: Listado de solicitudes inmediatas en búsqueda activa dentro de su distrito de cobertura.
        * **Endpoints de Transición de Estado:**
            * `PATCH /api-paseape/paseador/solicitudes/{uuid}/tomar`: Adjudicación atómica de solicitud inmediata (`SOLICITADA_BUSCANDO` [8] ➔ `ACEPTADA` [2]), asigna el paseador, fija `fecha_hora_aceptada` y notifica al cliente (`SOLICITUD_ACEPTADA`).
            * `PATCH /api-paseape/paseador/solicitudes/{uuid}/descartar`: Descarte individual de solicitud inmediata en `solicitud_paseo_descarte` sin afectar a otros paseadores.
            * `PATCH /api-paseape/paseador/solicitudes/{uuid}/aceptar`: Aceptación de solicitud programada (`PENDIENTE` [1] ➔ `ACEPTADA` [2]), fija `fecha_hora_aceptada = LocalDateTime.now()` y notifica al cliente (`SOLICITUD_ACEPTADA`).
            * `PATCH /api-paseape/paseador/solicitudes/{uuid}/rechazar`: Rechazo de solicitud programada (`PENDIENTE` [1] ➔ `RECHAZADA` [3]) con motivo opcional (`RechazarSolicitudReqDto`), libera cupos y notifica al cliente (`SOLICITUD_RECHAZADA`).
            * `PATCH /api-paseape/paseador/solicitudes/{uuid}/cancelar`: Cancelación de paseo previamente confirmado (`ACEPTADA` [2] ➔ `CANCELADA` [4]) antes de su inicio, con motivo obligatorio de 10 a 500 caracteres (`CancelarSolicitudReqDto`), liberando cupos y notificando al cliente (`SOLICITUD_CANCELADA`).

    2. **Flujo Operativo del Cliente – Bandeja "Mis Paseos" (`SolicitudPaseoClienteController`):**
        * **Endpoint de Consulta Unificada:**
            * `GET /api-paseape/cliente/solicitudes`: Endpoint paginado con query params:
                * `tab`: (`ACTIVOS`, `PENDIENTES`, `HISTORIAL`, por defecto `PENDIENTES`).
                * `page`: Número de página (0-indexed, default `0`).
                * `size`: Tamaño de página (default `10`, máx `50`).
                * Retorna la envoltura `ResponseDto<PageResponseDto<SolicitudPaseoResDto>>`.
        * **Estructura de las 3 Pestañas del Cliente:**
            * **Pestaña 1 - `ACTIVOS`:** Muestra únicamente los paseos que se encuentran en ejecución física en tiempo real (`solicitud_paseo_estado_id = 5 [EN_CURSO]`), ordenados por `fecha_hora_inicio_real DESC, creado_el DESC`.
            * **Pestaña 2 - `PENDIENTES` (Pestaña por defecto):** Agrupa todas las solicitudes abiertas o pendientes de inicio:
                * Inmediatas bajo demanda en búsqueda activa (`solicitud_paseo_estado_id = 8 [SOLICITADA_BUSCANDO]`).
                * Programadas esperando confirmación del paseador (`solicitud_paseo_estado_id = 1 [PENDIENTE]`).
                * Paseos (inmediatos o programados) ya aceptados por el paseador pendientes de iniciar el recorrido (`solicitud_paseo_estado_id = 2 [ACEPTADA]`).
                * Criterio de ordenamiento: `fecha_programada ASC, creado_el DESC`.
            * **Pestaña 3 - `HISTORIAL`:** Registro histórico consolidado de todas las solicitudes cerradas o terminales:
                * Culminadas satisfactoriamente (`solicitud_paseo_estado_id = 6 [FINALIZADA]`).
                * Canceladas por el cliente o el paseador (`solicitud_paseo_estado_id = 4 [CANCELADA]`).
                * Rechazadas por el paseador (`solicitud_paseo_estado_id = 3 [RECHAZADA]`).
                * Vencidas por inactividad (`solicitud_paseo_estado_id = 7 [VENCIDA]`).
                * Criterio de ordenamiento: `actualizado_el DESC, creado_el DESC`.

    3. **Flujo y Reglas de Negocio de Cancelación por el Cliente:**
        * **Endpoint:** `PATCH /api-paseape/cliente/solicitudes/{uuid}/cancelar` con cuerpo `@RequestBody(required = false) CancelarSolicitudReqDto`.
        * **Aislamiento Multitenant:** Comprueba que la solicitud pertenezca al cliente en sesión autenticada (`solicitud.getCliente().getId().equals(cliente.getId())`).
        * **Regla de Cancelación Condicional según Estado de Aceptación:**
            * **A. Solicitud NO Aceptada por el Paseador (`SOLICITADA_BUSCANDO` [8] o `PENDIENTE` [1]):**
                * El cliente puede desistir libremente del servicio antes de que sea aceptado.
                * El comentario o motivo es **opcional**: se permite enviar cuerpo vacío (`{}`), nulo o texto sin requerir longitud mínima.
                * Transiciona a estado `CANCELADA` (4). Si es inmediata abierta (`SOLICITADA_BUSCANDO`), sale de la bolsa general sin requerir notificación (al no tener paseador asignado). Si es programada (`PENDIENTE`), desvincula la atención solicitada.
            * **B. Paseo YA Aceptado por el Paseador (`ACEPTADA` [2]):**
                * El cliente puede cancelar el servicio siempre que el paseador no haya marcado el inicio del recorrido (`fecha_hora_inicio_real == null`).
                * El comentario o motivo es **estrictamente obligatorio**: debe tener una longitud mínima de 10 caracteres y máxima de 500 caracteres (`Constant.MSJ_ERROR_MOTIVO_CANCELACION_OBLIGATORIO`).
                * Transiciona a estado `CANCELADA` (4), persiste el motivo en `motivo_rechazo_cancelacion`, libera el cupo reservado del bloque horario y emite una notificación interna al paseador asignado (`tipo_notificacion_id = 6 [SOLICITUD_CANCELADA]`, título `"Paseo cancelado por el cliente"`).
            * **C. Restricciones y Bloqueos de Transición:**
                * Si el paseo ya arrancó (`solicitud_paseo_estado_id = 5 [EN_CURSO]` o `fecha_hora_inicio_real != null`), la cancelación se bloquea arrojando error de negocio (`MSJ_ERROR_SOLICITUD_EN_CURSO_NO_CANCELABLE`).
                * Si la solicitud ya se encuentra en un estado terminal (`RECHAZADA` [3], `CANCELADA` [4], `FINALIZADA` [6] o `VENCIDA` [7]), se deniega la operación arrojando `MSJ_ERROR_SOLICITUD_NO_CANCELABLE`.

    4. **Transición Automática por Expiración (`AppTaskScheduler`):**
        * Tarea periódica cada 60 segundos (`expirarSolicitudesInmediatasVencidas`) que evalúa solicitudes inmediatas con `solicitud_paseo_estado_id = 8 [SOLICITADA_BUSCANDO]` y `hora_vencimiento <= LocalDateTime.now()`.
        * Transiciona atómicamente a `VENCIDA` (7) sin alterar metadatos manuales, permitiendo auditoría automática por JPA.

    5. **Componentes y Capas Implementadas:**
        * **Controlador:** `SolicitudPaseoClienteController` (`GET /cliente/solicitudes`, `PATCH /cliente/solicitudes/{uuid}/cancelar`).
        * **Validador:** `SolicitudPaseoValidator` (`validarConsultaBandejaCliente`, `validarCancelarSolicitudCliente`).
        * **Servicio:** `SolicitudPaseoService` (`obtenerBandejaCliente` con `@Transactional(readOnly = true)`, `cancelarSolicitudCliente` con `@Transactional(rollbackFor = Exception.class)`).
        * **Repositorios:** `ISolicitudPaseoJpaRepository` (`findByClienteIdAndEstadosPaginado`), `ISolicitudPaseoRepository`, `SolicitudPaseoRepository`.
        * **Constantes:** `Constant.java` (`TAB_CLIENTE_ACTIVOS`, `TAB_CLIENTE_PENDIENTES`, `TAB_CLIENTE_HISTORIAL`, `MSJ_BANDEJA_CLIENTE_OBTENIDA`, `MSJ_SOLICITUD_CANCELADA_CLIENTE_EXITO`, `MSJ_ERROR_SOLICITUD_EN_CURSO_NO_CANCELABLE`, `TITULO_NOTIF_SOLICITUD_CANCELADA_CLIENTE`).
* **Cómo funciona:**
    * **Consulta de Bandeja ("Mis Paseos"):** El cliente ingresa a la aplicación móvil y navega entre sus 3 pestañas. Por defecto visualiza `PENDIENTES` para revisar sus paseos confirmados próximos a realizarse y las solicitudes que esperan paseador; al momento del paseo conmuta a `ACTIVOS` para monitorear el paseo en curso; y en `HISTORIAL` audita el histórico de todos sus servicios concluidos, cancelados o expirados.
    * **Flujo de Cancelación Ágil:** Si el cliente solicitó un servicio y desiste antes de que un paseador lo acepte, puede cancelar de forma directa sin redactar un motivo. Si el servicio ya contaba con un paseador comprometido (`ACEPTADA`), el sistema exige un motivo justificado (≥ 10 caracteres) antes de liberar el cupo y notificar de inmediato al paseador asignado.
---

## 4. Historias de Usuario Pendientes

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
