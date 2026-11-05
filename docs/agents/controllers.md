# Controllers

El controller es el borde HTTP de la aplicación: recibe el DTO o parámetros de la petición, llama al validator, llama **un solo método** del service y arma la respuesta envuelta en `ResponseDto<T>`. No contiene lógica de negocio ni inyecta repositorios.

**Arquitectura estándar (estilo Mortgage para nuevos desarrollos):**
En desarrollos futuros los controllers **no deben extender de `BaseController` ni de ninguna clase base**. Son clases independientes anotadas con `@RestController` y `@RequiredArgsConstructor`.

## Estructura de Clase

```java
@RestController
@RequestMapping(path = "cliente/solicitudes", produces = "application/json")
@RequiredArgsConstructor
public class SolicitudPaseoClienteController {

    private final RequestService requestService;
    private final SolicitudPaseoService solicitudPaseoService;
    private final SolicitudPaseoValidator solicitudPaseoValidator;

    private final Logger logger = LoggerFactory.getLogger(SolicitudPaseoClienteController.class);
```

- `@RequestMapping` en kebab-case o paths jerárquicos limpios (`cliente/solicitudes`, `paseador/disponibilidad`), sin barra diagonal inicial en las subrutas.
- `@Operation(summary = "...")` en español en cada endpoint para OpenAPI / Swagger.
- El logger debe declararse obligatoriamente con la **clase propia**: `LoggerFactory.getLogger(XController.class)`.

---

## Endpoint Estándar (con Validator y Control de Errores)

```java
@Operation(summary = "Crear nueva solicitud de paseo inmediato")
@PostMapping("inmediata")
public ResponseEntity<ResponseDto<SolicitudPaseoResDto>> crearSolicitudInmediata(
        Principal principal,
        @RequestBody CrearSolicitudInmediataReqDto reqDto
) {
    var response = new ResponseDto<SolicitudPaseoResDto>();
    try {
        Date startDatetime = Calendar.getInstance().getTime();

        // 1. Invocación del validador dedicado (estilo Mortgage)
        ResponseDto<?> someError = solicitudPaseoValidator.validateCrearInmediata(reqDto);
        if (someError != null) {
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(someError.getStatusCode());
            response.setCode(someError.getCode());
            response.setMessage(someError.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(response.getStatusCode()).body(response);
        }

        // 2. Invocación del servicio
        String userEmail = principal.getName();
        SolicitudPaseoResDto resData = solicitudPaseoService.crearSolicitudInmediata(userEmail, reqDto);

        // 3. Ensamblado de respuesta exitosa
        Date endDatetime = Calendar.getInstance().getTime();
        HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

        response.setStatusCode(StatusCodes.Code201);
        response.setCode(MessageCodes.ResponseCodeS00);
        response.setMessage(Constant.MSJ_SOLICITUD_CREADA_EXITO);
        response.setHeader(headerDto);
        response.setResponse(resData);
        return ResponseEntity.status(response.getStatusCode()).body(response);

    } catch (BadRequestException brex) {
        // Excepción de negocio esperada: se devuelve con su código funcional, sin registrar como error crítico
        Date endDatetime = Calendar.getInstance().getTime();
        response.setStatusCode(brex.statusCode != null ? brex.statusCode : StatusCodes.Code400);
        response.setCode(brex.code != null ? brex.code : MessageCodes.ResponseCodeBR01);
        response.setMessage(brex.getMessage());
        response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
        return ResponseEntity.status(response.getStatusCode()).body(response);

    } catch (Exception ex) {
        // Error no previsto (500): se loguea método, payload y traza
        logger.error("Error no controlado en crearSolicitudInmediata | Payload: {} | Causa: {}", reqDto, ex.getMessage(), ex);
        Date endDatetime = Calendar.getInstance().getTime();
        response.setStatusCode(StatusCodes.Code500);
        response.setCode(MessageCodes.ResponseCodeE99);
        response.setMessage("Ocurrió un error interno al procesar la solicitud");
        response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
        return ResponseEntity.status(StatusCodes.Code500).body(response);
    }
}
```

---

## Reglas Estrictas

1. **Un try/catch por endpoint:** Captura específica para cada excepción propia que el servicio pueda lanzar (`BadRequestException`, `NotFoundRequestException`), y `catch (Exception ex)` al final para errores no previstos.
2. **Las excepciones de negocio no se loguean como error del sistema:** Representan casos funcionales inválidos provocados por el usuario o cliente.
3. **Códigos HTTP y Mensajes:**
   - Creación: `StatusCodes.Code201` (`201`).
   - Lecturas, actualizaciones o eliminaciones: `StatusCodes.Code200` (`200`).
   - Éxito funcional: `MessageCodes.ResponseCodeS00` (`"00"`).
   - Error 500: Siempre retorna `response.setResponse(null)` y un mensaje genérico al usuario; nunca expone detalles de base de datos ni el stack trace.
4. **Usuario autenticado:** Se obtiene como `Principal principal` (o `Authentication authentication`) en los argumentos del método y se pasa el email al servicio (`principal.getName()`). Queda prohibido leer el contexto de seguridad estáticamente mediante `SecurityContextHolder.getContext()`.
5. **Un endpoint, una sola llamada al servicio:** Si se requiere orquestar múltiples pasos de negocio, se encapsulan dentro de un método en la capa de Servicio.
6. **Auditoría temporal obligatoria:** Captura `startDatetime` antes de validar y genera el `HeaderDto` con `requestService.getResponseHeader(startDatetime, endDatetime)`.

---

## No Hacer

```java
// ❌ Heredar de BaseController en nuevos controladores
public class NuevoController extends BaseController { ... }

// ❌ Lógica de negocio dentro del controller
if (solicitud.getEstadoId() == Constant.ID_ESTADO_SOLICITUD_PENDIENTE) { ... }

// ❌ Repositorio o Mapper inyectado en el controller
private final ISolicitudPaseoRepository solicitudPaseoRepository;
private final ISolicitudPaseoDtoMapper mapper;

// ❌ Captura genérica que expone el mensaje técnico interno
} catch (Exception ex) {
    response.setMessage(ex.getMessage());
}

// ❌ Uso de @Valid de Jakarta en el RequestBody
public ResponseEntity<?> save(@Valid @RequestBody CrearSolicitudReqDto dto)
```

---

## Checklist para Controllers

- [ ] La clase es independiente y **no extiende `BaseController`**.
- [ ] Logger declarado con la clase propia: `LoggerFactory.getLogger(XController.class)`.
- [ ] Validador dedicado ejecutado al inicio antes de llamar al servicio.
- [ ] `HeaderDto` obtenido mediante `requestService.getResponseHeader(start, end)`.
- [ ] Un solo método del servicio invocado por endpoint.
- [ ] Captura explícita de `BadRequestException` devolviendo su código funcional.
- [ ] Catch final de `Exception` con log estructurado y retorno 500 neutro.
