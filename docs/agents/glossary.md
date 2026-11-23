# Glosario de Dominio - PaseaPe

Términos del negocio de **PaseaPe** (plataforma de paseos, cuidado y bienestar de mascotas en Lima Metropolitana) y su correspondencia exacta con el código fuente.

Rutas relativas a `src/main/java/com/paseape/apipaseape/`. Constantes centralizadas en `infrastructure/constant/Constant.java`, `StatusCodes.java` y `MessageCodes.java`.

---

## 1. Actores y Perfiles

| Término | Definición de Negocio | En el Código | No Confundir Con |
|---|---|---|---|
| **Cliente** | Dueño o apoderado de una o varias mascotas registradas que solicita y paga los servicios de paseo. | Entidad `Cliente`, tabla `clientes`, `IClienteRepository`. Rol `Constant.ROL_CLIENTE` ("CLIENTE", id 1). | `Usuario` (la cuenta base de autenticación). |
| **Paseador** | Proveedor de servicios calificado y verificado que atiende paseos en su distrito de cobertura. | Entidad `Paseador`, tabla `paseadores`, `IPaseadorRepository`. Rol `Constant.ROL_PASEADOR` ("PASEADOR", id 2). | `Cliente`. |
| **Administrador** | Usuario con permisos de auditoría, gestión de parámetros del sistema y revisión de paseadores. | Entidad `Administrador`, `IAdministradorRepository`. Rol `Constant.ROL_ADMINISTRADOR` ("ADMINISTRADOR", id 3). | `Usuario`. |
| **Usuario** | Cuenta transversal del sistema para autenticación local (BCrypt) o Google Identity Services. | Entidad `Usuario`, tabla `usuarios`, `IUsuarioRepository`. Estados en `usuario_estado` (`ACTIVO` [1], `INACTIVO` [2], `BLOQUEADO` [5]). | Perfil específico de rol (`Cliente`, `Paseador`). |
| **Estado de Verificación de Paseador** | Fases de auditoría de documentos y antecedentes para habilitar a un paseador a operar en la plataforma. | `PaseadorEstadoVerificacion`, tabla `paseador_estado_verificacion`: `PENDIENTE` (1), `EN_REVISION` (2), `APROBADO` (3 / `Constant.ID_APROBADO`), `RECHAZADO` (4), `OBSERVADO` (5). | `UsuarioEstado` (estado de la cuenta de login). |
| **Validación de Identidad / RENIEC** | Comprobación de existencia del documento oficial del paseador. En producción (`APP_ENV=prod`) valida con RENIEC vía ApisPerú; en local omite la llamada remota. | `ApisPeruService`, `AuthValidator`, tabla `tipo_documento`: `DNI` (1, 8 dígitos), `CARNET_EXTRANJERIA` (2, 9 dígitos), `PASAPORTE` (3, 9 caracteres). | Validación sintáctica en memoria. |

---

## 2. Mascotas y Catálogos Fisiológicos

| Término | Definición de Negocio | En el Código | No Confundir Con |
|---|---|---|---|
| **Mascota** | Animal de compañía registrado por un cliente (canino o felino). Identificado públicamente por su `uuid`. | Entidad `Mascota`, tabla `mascotas`, `IMascotaRepository`. | `SolicitudPaseoMascota` (relación transaccional con un paseo). |
| **Tipo de Mascota** | Clasificación biológica base del animal. | `TipoMascota`, tabla `tipo_mascota`: `PERRO` (`ID_PERRO` = 1), `GATO` (`ID_GATO` = 2). | `TipoRaza`. |
| **Tamaño de Mascota** | Escala de contextura física que impacta en la compatibilidad de paseos grupales. | `TipoTamanoMascota`, tabla `tipo_tamano_mascota`: `TOY` (1), `PEQUENO` (2), `MEDIANO` (3), `GRANDE` (4), `GIGANTE` (5). | `TipoNivelEnergia`. |
| **Nivel de Energía** | Comportamiento cinético y dinamismo de la mascota en exteriores. | `TipoNivelEnergia`, tabla `tipo_nivel_energia`: `BAJO` (1), `MODERADO` (2), `ALTO` (3), `MUY_ALTO` (4). | `TipoTamanoMascota`. |
| **Raza** | Variedad zoológica oficial o mestiza. Catálogo estructurado para caninos (1–29) y felinos (30–40). | `TipoRaza`, tabla `tipo_raza`. Ej: `ID_LABRADOR_RETRIEVER` (3), `ID_GOLDEN_RETRIEVER` (4), `ID_CANINO_MESTIZO` (1). | `TipoMascota`. |
| **Distrito de Cobertura** | Distrito oficial de Lima Metropolitana donde el paseador opera y ofrece sus servicios. | `DistritoLima`, tabla `distrito_lima`: 13 distritos clave configurados (Miraflores [21], San Isidro [31], Surco [40], etc.). | Dirección física de recojo. |

---

## 3. Disponibilidad y Horarios del Paseador

| Término | Definición de Negocio | En el Código | No Confundir Con |
|---|---|---|---|
| **Bloque Horario** | Franja fija de 60 minutos entre las 06:00 y las 23:00 (17 franjas: `B06_07` a `B22_23`). Cada registro tiene `hora_inicio`, `hora_fin` y `orden` (1 a 17). | Entidad `BloqueHorario`, tabla `bloque_horario`, `IBloqueHorarioRepository`. | Duración del paseo (`TipoDuracionPaseo`). |
| **Día de la Semana** | Jornada semanal indexada bajo el estándar ISO-8601 (1 = Lunes hasta 7 = Domingo). | Entidad `DiaSemana`, tabla `dia_semana`, `IDiaSemanaRepository`. | Fecha civil (`LocalDate`). |
| **Disponibilidad del Paseador** | Plantilla atómica recurrente por `(paseador_id, dia_semana_id, bloque_horario_id)` que define si el paseador atiende y cuál es su capacidad máxima en esa franja. | Entidad `DisponibilidadPaseador`, tabla `disponibilidad_paseador`, `IDisponibilidadPaseadorRepository`. | Disponibilidad dinámica por fecha específica. |
| **Capacidad Máxima / Cupo** | Cantidad máxima de mascotas simultáneas que el paseador puede pasear en un bloque horario (predeterminado 3, rango válido de 1 a 6). | Columna `max_mascotas` en `disponibilidad_paseador` y `max_mascotas_predeterminado` en `paseadores`. | Mascotas ocupadas en una fecha. |
| **Ocupación Dinámica por Fecha** | Cálculo dinámico en tiempo real que cruza la plantilla del paseador con las solicitudes activas (`PENDIENTE`, `ACEPTADA`, `EN_CURSO`) para una fecha civil determinada. | `PaseadorService.obtenerDisponibilidadPorFecha`, `ISolicitudPaseoJpaRepository.countMascotasOcupadasAgrupadoPorBloque`. | Matriz estática de disponibilidad. |
| **Cupos Disponibles** | Margen libre en un bloque para aceptar nuevas mascotas: `max(0, max_mascotas - mascotas_ocupadas)`. Si llega a 0, se marca `esta_lleno = true`. | Campo calculado `cupos_disponibles` en `DisponibilidadBloqueFechaResDto`. | Capacidad base predeterminada. |

---

## 4. Solicitudes y Ciclo de Vida del Paseo

| Término | Definición de Negocio | En el Código | No Confundir Con |
|---|---|---|---|
| **Solicitud de Paseo** | Registro cabecera que representa la orden de servicio de paseo contratada o en proceso de asignación. | Entidad `SolicitudPaseo`, tabla `solicitud_paseo`, `ISolicitudPaseoRepository`. | `SolicitudPaseoMascota` (detalle de mascotas asociadas). |
| **Código de Reserva** | Identificador de negocio legible expuesto al usuario. Prefijos: `PAS-INM-YYYYMM-XXXX` (inmediato) y `PAS-PROG-YYYYMM-XXXX` (programado). | Campo `codigo_reserva`, generado por `generarCodigoReserva` en `SolicitudPaseoService`. | `uuid` (identificador canónico de 36 caracteres). |
| **Paseo Inmediato ("Tipo Taxi" - HU15)** | Solicitud bajo demanda creada por el cliente proponiendo la tarifa por hora que desea pagar. Nace sin paseador asignado y en búsqueda abierta en su distrito. | `tipo_modalidad_paseo_id = 2` (`MODALIDAD_INMEDIATO`), estado `SOLICITADA_BUSCANDO` (8). | Paseo Programado. |
| **Paseo Programado (HU16)** | Solicitud agendada para una fecha y bloque(s) horario(s) futuro(s) con un paseador previamente seleccionado por el cliente. | `tipo_modalidad_paseo_id = 1` (`MODALIDAD_PROGRAMADO`), estado `PENDIENTE` (1). | Paseo Inmediato. |
| **Adjudicación Atómica (Tomar Paseo)** | Transición concurrente en tiempo real mediante `UPDATE ... WHERE paseador_id IS NULL AND estado = SOLICITADA_BUSCANDO`. Evita colisiones si varios paseadores intentan tomar el paseo a la vez. | `ISolicitudPaseoJpaRepository.adjudicarSolicitudInmediata`, endpoint `PATCH /paseador/solicitudes/{uuid}/tomar`. | Aceptar paseo programado. |
| **Descarte de Inmediatas (HU20)** | Acción mediante la cual un paseador decide no atender una solicitud inmediata disponible. La solicitud desaparece de su bandeja sin perjudicar a los demás paseadores del distrito. | Tabla `solicitud_paseo_descarte`, entidad `SolicitudPaseoDescarte`, endpoint `PATCH /paseador/solicitudes/{uuid}/descartar`. | Rechazar paseo programado. |
| **Expiración Automática** | Cancelación automática de solicitudes inmediatas no adjudicadas tras transcurrir 5 minutos desde su creación (`hora_vencimiento <= NOW()`). | Tarea programada en `AppTaskScheduler`, transición a estado `VENCIDA` (7). | Cancelación manual por el cliente. |
| **Duración del Paseo** | Tiempo de recorrido contratado: 30 o 60 min (requiere 1 bloque) / 90 o 120 min (requiere 2 bloques horarios consecutivos). | `TipoDuracionPaseo`, tabla `tipo_duracion_paseo`: `30 MINUTOS` (1), `60 MINUTOS` (2), `90 MINUTOS` (3), `120 MINUTOS` (4). | Bloque Horario. |
| **Bloques Contiguos (HU18)** | Regla de reserva para paseos de 90 o 120 minutos: exige validar y ocupar cupo en el bloque inicial y en el bloque inmediato siguiente (`orden + 1`). | Validación `ordenSiguiente = bloqueInicial.getOrden() + 1` en `SolicitudPaseoService`. | Dos paseos independientes. |
| **Paseo con Múltiples Mascotas (HU17)** | Servicio contratado para pasear 2 o más mascotas del mismo cliente en un único recorrido conjunto. | Relación N:M en `solicitud_paseo_mascota`, entidad `SolicitudPaseoMascota`. | Capacidad del paseador. |

---

## 5. Estados del Paseo (`solicitud_paseo_estado`)

| ID | Estado | Descripción Funcional | Transición Siguiente Permitida |
|---|---|---|---|
| **1** | `PENDIENTE` | Solicitud programada recibida, a la espera de confirmación del paseador. | `ACEPTADA` (2), `RECHAZADA` (3), `CANCELADA` (4). |
| **2** | `ACEPTADA` | Solicitud confirmada por el paseador (o inmediata adjudicada). Cupo reservado firmemente. | `EN_CURSO` (5), `CANCELADA` (4). |
| **3** | `RECHAZADA` | Paseador declinó la solicitud programada (motivo opcional). Cupo liberado de inmediato. | Estado terminal. |
| **4** | `CANCELADA` | Solicitud cancelada por el cliente o paseador antes de iniciar (motivo obligatorio). Cupo liberado. | Estado terminal. |
| **5** | `EN_CURSO` | Paseo en ejecución física en tiempo real (`fecha_hora_inicio_real = NOW()`). | `FINALIZADA` (6). |
| **6** | `FINALIZADA` | Servicio concluido exitosamente (`fecha_hora_fin_real = NOW()`). | Estado terminal (habilita calificación). |
| **7** | `VENCIDA` | Solicitud inmediata que no fue adjudicada dentro de los 5 minutos reglamentarios. | Estado terminal. |
| **8** | `SOLICITADA_BUSCANDO` | Solicitud inmediata publicada a la espera de que el primer paseador disponible la tome. | `ACEPTADA` (2), `VENCIDA` (7), `CANCELADA` (4). |

---

## 6. Tarifas, Pagos y Notificaciones

| Término | Definición de Negocio | En el Código | No Confundir Con |
|---|---|---|---|
| **Tarifa Hora Snapshot** | Copia inmutable de la tarifa por hora en Soles (PEN) congelada al momento exacto de crear la solicitud. Garantiza que futuros cambios de tarifa del paseador no alteren órdenes ya registradas. | Campo `tarifa_hora_pen_snapshot` en `solicitud_paseo`. | `tarifa_hora_pen` actual del perfil del paseador. |
| **Monto Total del Paseo** | Importe final calculado con fórmula exacta: `(tarifa_hora_snapshot / 60) * minutos * cantidad_mascotas`, con redondeo bancario a 2 decimales (`HALF_UP`). | Campo `monto_total` en `solicitud_paseo`. | Tarifa por hora base. |
| **Método de Pago** | Canal financiero acordado para el cobro del paseo. | `TipoMetodoPago`, tabla `tipo_metodo_pago`: `YAPE / PLIN` (1), `EFECTIVO` (2). | Pasarela de tarjetas. |
| **Notificación Interna** | Alerta persistida en la plataforma para actualizar al usuario sobre eventos transaccionales de sus paseos. Consultada vía Short Polling. | Entidad `Notificacion`, tabla `notificaciones`, `INotificacionRepository`. | Correo transaccional de Brevo. |
| **Tipo de Notificación** | Clasificador funcional del evento notificado. | `TipoNotificacion`: `SOLICITUD_RECIBIDA` (1), `SOLICITUD_ACEPTADA` (2), `SOLICITUD_RECHAZADA` (3), `PASEO_INICIADO` (4), `PASEO_FINALIZADO` (5), `SOLICITUD_CANCELADA` (6). | Correo de recuperación de contraseña. |
| **Brevo REST API v3** | Servicio externo HTTPS para envío de correos transaccionales (recuperación de contraseña con tokens de 15 minutos). Registra auditoría en base de datos. | `BrevoEmailService`, tabla de auditoría `brevo_email`, header `api-key`. | Notificaciones internas de la app. |
| **Zona Horaria Civil** | Referencia temporal `America/Lima` (UTC-5) utilizada obligatoriamente para evaluar franjas horarias, expiración de bloques del día actual y validación de fechas pasadas. | `Constant.ZONE_ID_LIMA`, `ZoneId.of("America/Lima")`. | Hora UTC de la base de datos y JVM. |

---

## 7. Trampas Frecuentes de Dominio

1. **`paseador_id` puede ser `null` en `solicitud_paseo`:** En solicitudes inmediatas (`SOLICITADA_BUSCANDO`), el campo `paseador_id` es nulo hasta que un paseador la toma mediante `adjudicarSolicitudInmediata`. En solicitudes programadas, el paseador es obligatorio desde el momento de la creación.
2. **`hora_vencimiento` solo aplica para solicitudes inmediatas:** Se inicializa en `NOW() + 5 minutos`. En solicitudes programadas permanece como `null`.
3. **Descartar vs. Rechazar:** `Descartar` aplica solo a solicitudes inmediatas y es un registro local del paseador en `solicitud_paseo_descarte` (no altera el estado de la solicitud). `Rechazar` aplica a solicitudes programadas en estado `PENDIENTE` y cambia el estado global de la solicitud a `RECHAZADA` (3), liberando el cupo reservado.
4. **Cupos vs. Mascotas:** El cupo de un paseador se mide en **cantidad de mascotas simultáneas**, no en cantidad de paseos o clientes. Un solo cliente que solicita un paseo para 2 mascotas ocupa 2 cupos de la capacidad del paseador en ese bloque.
5. **Duración de 90 y 120 minutos:** Siempre requiere 2 bloques horarios continuos. Si el paseador no tiene disponibilidad o cupo suficiente en el segundo bloque, la solicitud debe ser rechazada de inmediato.
6. **Ofuscación de IDs:** Los clientes y paseadores interactúan exclusivamente con `uuid` en los DTOs y URLs. Las claves foráneas numéricas autoincrementales son estrictamente para el motor de base de datos relacional y nunca deben exponerse en endpoints públicos.
