# Manual de Integración y Pruebas - HU26 y HU24 (api-paseape)

Este manual documenta el funcionamiento técnico, especificación de contratos HTTP, guías de prueba con cURL/Postman e implementación en aplicaciones cliente (Android/Kotlin) para las Historias de Usuario desarrolladas en el **Sprint 3**:
* **HU26:** Telemetría GPS Acumulativa por Lotes (Paseador).
* **HU24:** Supervisión en Tiempo Real (Short Polling) y Consulta de Recorrido Consolidado (Cliente / Multi-rol).

---

## 1. Arquitectura y Funcionamiento General

El sistema implementa el **Modelo Acumulativo Directo ($O(1)$)**, diseñado para operar eficientemente sobre **Aiven MySQL Cloud** y clientes móviles de alto rendimiento sin sobrecargar la red ni la base de datos:

```
                            FLUJO DURANTE EL PASEO (EN_CURSO)
                            
   [App Paseador] ──(Batch cada 20-30s)──► POST /paseador/solicitudes/{uuid}/ubicaciones
                                                          │
                                                          ▼ (Acumulación directa O(1))
                                        ┌──────────────────────────────────────┐
                                        │  solicitud_paseo_ubicacion_en_vivo   │
                                        │  - 1 fila por paseo activo           │
                                        │  - Distancia acumulada (Haversine)   │
                                        │  - Polilínea delta codificada        │
                                        └──────────────────────────────────────┘
                                                          │
   [App Dueño] ◄──(Polling cada 6-8s)─── GET /cliente/solicitudes/{uuid}/ubicacion-actual
                                           - Retorna última posición y rumbo
                                           - Flag 'senal_degradada' si demora > 45s
                                           - Maneja espera de 1ra señal GPS


                          FLUJO POST-SERVICIO (FINALIZADA)
                          
   [App Dueño / Paseador] ─────────────► GET /solicitudes/{uuid}/recorrido
                                           - Consulta inmutable O(1) de histórico
                                           - Polilínea Google Encoded lista para pintar
                                           - Métricas: distancia, duración, vel. promedio
```

### Principios Operativos Clave
1. **Buffer en Caliente ($O(1)$):** La tabla `solicitud_paseo_ubicacion_en_vivo` conserva una única tupla activa por servicio en curso.
2. **Filtro de Jitter / Ruido GPS ($\Delta d \ge 3.0\text{ m}$):** Si un punto dentro del lote se desplazó menos de $3\text{ m}$ respecto al anterior, solo refresca la última coordenada en vivo, pero no se anexa a la polilínea para evitar ruido estático.
3. **Compresión Polyline Delta:** La ruta se codifica incrementalmente en backend usando el algoritmo estándar de Google Maps (`PolylineDeltaEncoder`), concatenando deltas sin reprocesar puntos históricos.
4. **Costo Cero ($0.00 USD) en Google Cloud Platform:** El renderizado y la decodificación se ejecutan localmente en la CPU del dispositivo móvil mediante `android-maps-utils`.

---

## 2. Catálogo de Endpoints y Contratos de Datos

Todos los endpoints operan bajo el context path `/api-paseape` y retornan el sobre canónico `ResponseDto<T>`.

---

### Endpoint 1: Ingesta de Telemetría GPS en Lote (HU26)

Permite al paseador enviar lotes de coordenadas capturadas por el sensor GPS mientras la solicitud está activa.

* **Método:** `POST`
* **Ruta:** `/api-paseape/paseador/solicitudes/{uuid}/ubicaciones`
* **Rol requerido:** `PASEADOR`
* **Headers:**
  * `Authorization: Bearer <TOKEN_PASEADOR>`
  * `Content-Type: application/json`

#### Estructura del Request Body (`RegistrarUbicacionesBatchReqDto`)
```json
{
  "ubicaciones": [
    {
      "latitud": -12.09675000,
      "longitud": -76.99342000,
      "precision_m": 4.50,
      "velocidad_ms": 1.25,
      "rumbo_grados": 45.00,
      "fecha_hora_gps": "2026-11-10T15:30:00"
    },
    {
      "latitud": -12.09689000,
      "longitud": -76.99321000,
      "precision_m": 4.10,
      "velocidad_ms": 1.30,
      "rumbo_grados": 48.50,
      "fecha_hora_gps": "2026-11-10T15:30:15"
    }
  ]
}
```

#### Parámetros del Payload
| Campo | Tipo | Requerido | Descripción / Validación |
|---|---|---|---|
| `ubicaciones` | `List` | Sí | Lista no vacía (1 a 50 puntos por lote). |
| `latitud` | `BigDecimal` | Sí | Rango válido: $-90.00000000$ a $90.00000000$. |
| `longitud` | `BigDecimal` | Sí | Rango válido: $-180.00000000$ a $180.00000000$. |
| `precision_m` | `BigDecimal` | No | Precisión estimada en metros reportada por el sensor. |
| `velocidad_ms` | `BigDecimal` | No | Velocidad instantánea en metros por segundo. |
| `rumbo_grados` | `BigDecimal` | No | Orientación / dirección de movimiento ($0.0^\circ$ a $360.0^\circ$). |
| `fecha_hora_gps` | `LocalDateTime` | Sí | Marca de tiempo del punto (anti-spoofing: $\le \text{UTC} + 60\text{ s}$). |

#### Respuestas HTTP
* **`200 OK` (Procesamiento Exitoso):**
```json
{
  "codigo": 200,
  "mensaje": "Ubicaciones procesadas exitosamente",
  "response": {
    "puntos_procesados": 2,
    "paseo_activo": true
  }
}
```
* **`400 Bad Request` (Paseo no activo o culminado):**
```json
{
  "codigo": 400,
  "mensaje": "La solicitud no se encuentra en curso para registrar ubicaciones en vivo",
  "response": null
}
```

---

### Endpoint 2: Monitoreo en Tiempo Real - Short Polling (HU24)

Permite al dueño de la mascota consultar la posición en vivo del paseador, rumbo de desplazamiento y estado de la señal GPS.

* **Método:** `GET`
* **Ruta:** `/api-paseape/cliente/solicitudes/{uuid}/ubicacion-actual`
* **Rol requerido:** `CLIENTE`
* **Headers:**
  * `Authorization: Bearer <TOKEN_CLIENTE>`

#### Respuestas HTTP

* **`200 OK` (Posición disponible en tiempo real):**
```json
{
  "codigo": 200,
  "mensaje": "Operación realizada con éxito",
  "response": {
    "uuid": "7b82f091-a182-45e1-88f5-cd2789642a10",
    "solicitud_paseo_uuid": "e839e55a-4b91-4c60-84c4-7ff536093370",
    "latitud": -12.09689000,
    "longitud": -76.99321000,
    "precision_m": 4.10,
    "velocidad_ms": 1.30,
    "rumbo_grados": 48.50,
    "fecha_hora_gps": "2026-11-10T15:30:15",
    "senal_degradada": false
  }
}
```

* **`200 OK` (Paseo en curso pero a la espera de la primera señal GPS):**
  * Ocurre si el paseador acaba de presionar "Iniciar Paseo", pero su servicio móvil en segundo plano aún no envía el primer lote:
```json
{
  "codigo": 200,
  "mensaje": "A la espera de la primera señal GPS del paseador",
  "response": null
}
```

* **`200 OK` con Señal Degradada (`senal_degradada = true`):**
  * Ocurre cuando el paseador no ha emitido coordenadas en más de 45 segundos (púneles, fallas de señal móvil, batería baja):
```json
{
  "codigo": 200,
  "mensaje": "Operación realizada con éxito",
  "response": {
    "uuid": "7b82f091-a182-45e1-88f5-cd2789642a10",
    "solicitud_paseo_uuid": "e839e55a-4b91-4c60-84c4-7ff536093370",
    "latitud": -12.09689000,
    "longitud": -76.99321000,
    "precision_m": 4.10,
    "velocidad_ms": 1.30,
    "rumbo_grados": 48.50,
    "fecha_hora_gps": "2026-11-10T15:20:00",
    "senal_degradada": true
  }
}
```

* **`400 Bad Request` (Solicitud no autorizada o no activa):**
```json
{
  "codigo": 400,
  "mensaje": "La solicitud indicada no pertenece al cliente autenticado o no se encuentra activa",
  "response": null
}
```

---

### Endpoint 3: Consulta de Recorrido Consolidado Post-Paseo (HU24)

Permite al cliente, al paseador o al administrador consultar el recorrido histórico inmutable (estilo Strava) cuando el servicio ya ha finalizado.

* **Método:** `GET`
* **Ruta:** `/api-paseape/solicitudes/{uuid}/recorrido`
* **Roles autorizados:** `CLIENTE`, `PASEADOR`, `ADMIN`
* **Headers:**
  * `Authorization: Bearer <TOKEN>`

#### Respuestas HTTP

* **`200 OK` (Recorrido histórico disponible):**
```json
{
  "codigo": 200,
  "mensaje": "Operación realizada con éxito",
  "response": {
    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "solicitud_paseo_uuid": "e839e55a-4b91-4c60-84c4-7ff536093370",
    "distancia_metros": 1845.50,
    "duracion_segundos": 3600,
    "velocidad_promedio_kmh": 1.85,
    "coordenadas_codificadas": "_p~iF~ps|U_ulLnnqC_mqNvxq`@",
    "coordenadas_total_puntos": 142,
    "tipo_origen_traza_descripcion": "GPS_TIEMPO_REAL",
    "fecha_hora_inicio": "2026-11-10T15:00:00",
    "fecha_hora_fin": "2026-11-10T16:00:00"
  }
}
```

* **`400 Bad Request` (Solicitud aún en curso o sin recorrido consolidado):**
```json
{
  "codigo": 400,
  "mensaje": "La solicitud indicada no se encuentra finalizada o no cuenta con recorrido consolidado",
  "response": null
}
```

---

## 3. Guía de Pruebas con cURL y Postman

A continuación se presentan los comandos cURL completos listos para ser importados en Postman (`File -> Import -> Raw text`). Se utilizan coordenadas representativas de **San Borja, Lima (alrededores del Pentagonito)**.

### Variables recomendadas para Postman Environment
* `base_url`: `http://localhost:7001/api-paseape`
* `token_paseador`: `<JWT_CON_ROL_PASEADOR>`
* `token_cliente`: `<JWT_CON_ROL_CLIENTE>`
* `solicitud_uuid`: `<UUID_DE_LA_SOLICITUD>`

---

### cURL 1: Iniciar Paseo (Precondición para activar `EN_CURSO` - HU27)
```bash
curl --location --request PATCH 'http://localhost:7001/api-paseape/paseador/solicitudes/e839e55a-4b91-4c60-84c4-7ff536093370/iniciar' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...' \
--header 'Content-Type: application/json'
```

---

### cURL 2: Envío de Lote GPS por el Paseador (HU26 - San Borja / Pentagonito)
Envía un lote de 3 puntos simulando el inicio del recorrido por el circuito de trote del Pentagonito:
```bash
curl --location --request POST 'http://localhost:7001/api-paseape/paseador/solicitudes/e839e55a-4b91-4c60-84c4-7ff536093370/ubicaciones' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...' \
--header 'Content-Type: application/json' \
--data-raw '{
  "ubicaciones": [
    {
      "latitud": -12.09675000,
      "longitud": -76.99342000,
      "precision_m": 4.50,
      "velocidad_ms": 1.25,
      "rumbo_grados": 45.00,
      "fecha_hora_gps": "2026-11-10T15:30:00"
    },
    {
      "latitud": -12.09689000,
      "longitud": -76.99321000,
      "precision_m": 4.10,
      "velocidad_ms": 1.30,
      "rumbo_grados": 48.50,
      "fecha_hora_gps": "2026-11-10T15:30:15"
    },
    {
      "latitud": -12.09705000,
      "longitud": -76.99300000,
      "precision_m": 3.80,
      "velocidad_ms": 1.35,
      "rumbo_grados": 50.00,
      "fecha_hora_gps": "2026-11-10T15:30:30"
    }
  ]
}'
```

---

### cURL 3: Consulta de Ubicación en Tiempo Real (HU24 - Cliente Dueño)
El cliente ejecuta esta petición en intervalos de short polling (cada 6-8 segundos):
```bash
curl --location --request GET 'http://localhost:7001/api-paseape/cliente/solicitudes/e839e55a-4b91-4c60-84c4-7ff536093370/ubicacion-actual' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'
```

---

### cURL 4: Consulta de Recorrido Consolidado (HU24 - Post-Paseo)
Disponible cuando la solicitud transiciona al estado `FINALIZADA` (6):
```bash
curl --location --request GET 'http://localhost:7001/api-paseape/solicitudes/e839e55a-4b91-4c60-84c4-7ff536093370/recorrido' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'
```

---

## 4. Guía de Implementación en Frontend (Android / Kotlin)

### 4.1. Módulo Paseador: Captura GPS y Despacho en Lote (HU26)

Para sortear las restricciones del **Doze Mode** y el ahorro de batería en Android 8+, la captura debe gestionarse dentro de un **`ForegroundService`** con notificación fija en la barra de estado.

#### A. Dependencias (`build.gradle.kts`)
```kotlin
dependencies {
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
}
```

#### B. Servicio de Captura en Primer Plano (`PaseoTrackingService.kt`)
```kotlin
class PaseoTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val bufferUbicaciones = Collections.synchronizedList(mutableListOf<UbicacionPuntoReqDto>())
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var paseoUuid: String = ""

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        // Iniciar servicio en primer plano con notificación
        startForeground(NOTIFICATION_ID, crearNotificacionServicio())
        configurarCapturaGps()
        iniciarDespachoPeriodico()
    }

    private fun configurarCapturaGps() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 
            12_000 // Intervalo deseado: 12 segundos
        ).apply {
            setMinUpdateIntervalMillis(10_000) // Mínimo 10 segundos
            setMinUpdateDistanceMeters(2.0f)   // Desplazamiento mínimo del sensor
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    val punto = UbicacionPuntoReqDto(
                        latitud = BigDecimal.valueOf(loc.latitude),
                        longitud = BigDecimal.valueOf(loc.longitude),
                        precisionM = if (loc.hasAccuracy()) BigDecimal.valueOf(loc.accuracy.toDouble()) else null,
                        velocidadMs = if (loc.hasSpeed()) BigDecimal.valueOf(loc.speed.toDouble()) else null,
                        rumboGrados = if (loc.hasBearing()) BigDecimal.valueOf(loc.bearing.toDouble()) else null,
                        fechaHoraGps = LocalDateTime.now(ZoneOffset.UTC).toString()
                    )
                    bufferUbicaciones.add(punto)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    // Despacho cada 25 segundos enviando el lote acumulado
    private fun iniciarDespachoPeriodico() {
        serviceScope.launch {
            while (isActive) {
                delay(25_000) // Despacho batch cada 25 segundos
                if (bufferUbicaciones.isNotEmpty() && paseoUuid.isNotBlank()) {
                    val batchAEnviar = ArrayList(bufferUbicaciones)
                    try {
                        val response = paseaPeApi.registrarUbicaciones(paseoUuid, RegistrarUbicacionesBatchReqDto(batchAEnviar))
                        if (response.isSuccessful && response.body()?.codigo == 200) {
                            bufferUbicaciones.removeAll(batchAEnviar)
                        }
                    } catch (e: Exception) {
                        Log.e("Tracking", "Fallo al despachar batch de telemetría, se reintentará en el siguiente ciclo", e)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
    }
}
```

---

### 4.2. Módulo Cliente: Monitoreo en Vivo por Short Polling (HU24)

El cliente realiza lecturas indexadas $O(1)$ cada 6 a 8 segundos. Se debe detener el polling en el ciclo de vida Android (`onPause()` o `onStop()`) para evitar consumo innecesario de batería y datos cuando la pantalla esté apagada o fuera de la vista de mapa.

#### A. ViewModel de Monitoreo (`MonitoreoPaseoViewModel.kt`)
```kotlin
class MonitoreoPaseoViewModel(
    private val api: PaseaPeApi,
    private val solicitudUuid: String
) : ViewModel() {

    private val _estadoUbicacion = MutableStateFlow<UbicacionUiState>(UbicacionUiState.Cargando)
    val estadoUbicacion: StateFlow<UbicacionUiState> = _estadoUbicacion.asStateFlow()

    private var pollingJob: Job? = null

    fun iniciarMonitoreo() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val response = api.obtenerUbicacionActual(solicitudUuid)
                    if (response.isSuccessful) {
                        val body = response.body()
                        val data = body?.response
                        if (data == null) {
                            // Caso: "A la espera de la primera señal GPS del paseador"
                            _estadoUbicacion.value = UbicacionUiState.EsperandoPrimeraSenal(body?.mensaje ?: "")
                        } else {
                            _estadoUbicacion.value = UbicacionUiState.UbicacionDisponible(data)
                        }
                    } else {
                        _estadoUbicacion.value = UbicacionUiState.Error("Error en servidor: ${response.code()}")
                    }
                } catch (e: Exception) {
                    _estadoUbicacion.value = UbicacionUiState.ErrorConexion(e.localizedMessage ?: "Fallo de conexión")
                }
                delay(7_000) // Short polling cada 7 segundos
            }
        }
    }

    fun detenerMonitoreo() {
        pollingJob?.cancel()
        pollingJob = null
    }
}
```

#### B. Renderizado en Google Maps con Rotación y Señal Degradada (`MonitoreoFragment.kt`)
```kotlin
class MonitoreoFragment : Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private var markerPaseador: Marker? = null

    override fun onResume() {
        super.onResume()
        viewModel.iniciarMonitoreo() // Reanuda polling al volver al mapa
    }

    override fun onPause() {
        super.onPause()
        viewModel.detenerMonitoreo() // Suspende polling si se minimiza la app
    }

    private fun observarEstado() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.estadoUbicacion.collect { state ->
                when (state) {
                    is UbicacionUiState.EsperandoPrimeraSenal -> {
                        mostrarBannerInformativo(state.mensaje) // "A la espera de la primera señal..."
                    }
                    is UbicacionUiState.UbicacionDisponible -> {
                        ocultarBannerInformativo()
                        actualizarPosicionEnMapa(state.data)
                    }
                    is UbicacionUiState.Error -> mostrarToast(state.mensaje)
                    else -> Unit
                }
            }
        }
    }

    private fun actualizarPosicionEnMapa(data: UbicacionEnVivoResDto) {
        val nuevaPosicion = LatLng(data.latitud.toDouble(), data.longitud.toDouble())

        if (markerPaseador == null) {
            val markerOptions = MarkerOptions()
                .position(nuevaPosicion)
                .title("Paseador")
                .anchor(0.5f, 0.5f)
                .flat(true) // Permite rotación suave según el rumbo
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_paseador))

            markerPaseador = googleMap?.addMarker(markerOptions)
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(nuevaPosicion, 17f))
        } else {
            // Animar movimiento suave del marcador
            markerPaseador?.position = nuevaPosicion
            data.rumboGrados?.let { markerPaseador?.rotation = it.toFloat() }
        }

        // Diagnóstico visual de señal degradada (retraso > 45s)
        if (data.senalDegradada) {
            markerPaseador?.alpha = 0.5f // Atenuar opacidad
            mostrarBadgeAlerta("Señal GPS débil o desactualizada")
        } else {
            markerPaseador?.alpha = 1.0f // Opacidad completa
            ocultarBadgeAlerta()
        }
    }
}
```

---

### 4.3. Módulo Historial: Decodificación y Trazado de Recorrido Post-Paseo (HU24)

Al consultar un paseo finalizado, se obtiene la ruta completa comprimida en `coordenadas_codificadas`.

#### A. Dependencia para Decodificación Local ($0 GCP)
```kotlin
dependencies {
    implementation("com.google.maps.android:android-maps-utils:3.8.2")
}
```

#### B. Trazado de la Polilínea y Auto-Zoom (`DetalleRecorridoActivity.kt`)
```kotlin
class DetalleRecorridoActivity : AppCompatActivity(), OnMapReadyCallback {

    private fun renderizarRecorrido(recorrido: SolicitudPaseoRecorridoResDto, map: GoogleMap) {
        // 1. Decodificar la polilínea localmente en la CPU del dispositivo (0 llamadas externas)
        val latLngList: List<LatLng> = PolylineEncoding.decode(recorrido.coordenadasCodificadas)

        if (latLngList.isEmpty()) return

        // 2. Trazar la polilínea en el mapa
        val polylineOptions = PolylineOptions()
            .addAll(latLngList)
            .color(ContextCompat.getColor(this, R.color.brand_primary))
            .width(12f)
            .geodesic(true)
            .jointType(JointType.ROUND)
            .startCap(RoundCap())
            .endCap(RoundCap())

        map.addPolyline(polylineOptions)

        // 3. Agregar marcador en el punto de inicio y punto de fin
        map.addMarker(MarkerOptions().position(latLngList.first()).title("Punto de inicio"))
        map.addMarker(MarkerOptions().position(latLngList.last()).title("Punto de fin"))

        // 4. Ajustar la cámara para encuadrar todo el recorrido con margen
        val boundsBuilder = LatLngBounds.Builder()
        latLngList.forEach { boundsBuilder.include(it) }
        val bounds = boundsBuilder.build()
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))

        // 5. Presentar métricas estilo Strava en la interfaz
        binding.tvDistanciaTotal.text = "${(recorrido.distanciaMetros / 1000.0).format(2)} km"
        binding.tvDuracionTotal.text = formatearSegundos(recorrido.duracionSegundos)
        binding.tvVelocidadPromedio.text = "${recorrido.velocidadPromedioKmh} km/h"
    }

    private fun formatearSegundos(segundos: Int): String {
        val h = segundos / 3600
        val m = (segundos % 3600) / 60
        val s = segundos % 60
        return if (h > 0) String.format("%02d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
    }
}
```

---

## 5. Resumen de Estados de Implementación

| Historia de Usuario | Funcionalidad | Componente Backend | Componente Frontend | Estado |
|---|---|---|---|---|
| **HU27** | Iniciar Paseo | `PATCH /paseador/solicitudes/{uuid}/iniciar` | Botón "Iniciar Paseo" en bandeja operativa | **IMPLEMENTADO** |
| **HU23** | Notificación Inicio | Persistencia en `notificaciones` y Brevo REST | Notificación Push / Bandeja de alertas | **IMPLEMENTADO** |
| **HU26** | Telemetría GPS en Lote | `POST /paseador/solicitudes/{uuid}/ubicaciones` | `ForegroundService` + `FusedLocationProviderClient` | **IMPLEMENTADO** |
| **HU24** | Monitoreo en Vivo | `GET /cliente/solicitudes/{uuid}/ubicacion-actual` | Short polling (6-8s) con rotación de marcador | **IMPLEMENTADO** |
| **HU24** | Recorrido Consolidado | `GET /solicitudes/{uuid}/recorrido` | Decodificación con `android-maps-utils` (Strava-like) | **IMPLEMENTADO** |
| **Cierre Paseo** | Finalización del Paseo | `PATCH /paseador/solicitudes/{uuid}/finalizar` | Botón "Finalizar Paseo" + Resumen post-paseo | **IMPLEMENTADO** |
