# Errores y Logging

## Manejo de Excepciones

Las excepciones de negocio residen en `infrastructure/exception/`.

| Excepción | Cuándo Usar | HTTP / StatusCodes | MessageCodes |
|---|---|---|---|
| `BadRequestException(message)` | Violación de regla de negocio o validación funcional | 400 (`StatusCodes.Code400`) | `ResponseCodeBR01` |
| `BadRequestException(code, message)` | Regla de negocio con código específico que el cliente móvil/web distingue | 400 (`StatusCodes.Code400`) | `code` (ej. `ResponseCodeBR03`) |
| `BadRequestException(statusCode, code, message)` | Error con código de estado HTTP y funcional explícito | `statusCode` | `code` |

### Ejemplo en el Service

```java
SolicitudPaseo solicitud = solicitudPaseoRepository.findByUuid(uuid);
if (solicitud == null) {
    throw new BadRequestException(MessageCodes.ResponseCodeBR01, "No existe una solicitud de paseo con el identificador proporcionado.");
}

if (solicitud.getSolicitudPaseoEstado().getId() != Constant.ID_ESTADO_SOLICITUD_ACEPTADA) {
    throw new BadRequestException(MessageCodes.ResponseCodeBR02, Constant.MSJ_ERROR_SOLICITUD_NO_INICIABLE);
}
```

- **Mensajes en español y orientados al usuario final:** Deben ser claros, concisos y autoexplicativos. Sin exponer nombres de tablas internas, sintaxis SQL, variables Java ni trazas de error.
- **Códigos centralizados:** Usar siempre las constantes de `MessageCodes` y `StatusCodes`. Prohibido inventar códigos numéricos o literales mágicos como `"400"` o `"ERR_01"` directamente en el código.
- **No envolver excepciones de negocio en `RuntimeException`:** Los controladores capturan específicamente `BadRequestException` para retornar el código y mensaje correspondiente.

---

## Logging Estructurado

En PaseaPe el registro de logs se realiza de forma estandarizada:

```java
private final Logger logger = LoggerFactory.getLogger(SolicitudPaseoService.class);

logger.info("Solicitud de paseo {} adjudicada al paseador {}", solicitud.getCodigoReserva(), paseador.getUuid());
```

### Reglas de Logging

1. **Instanciación:** Utilizar siempre `LoggerFactory.getLogger(ClaseActual.class)`. Prohibido mezclar con anotaciones `@Slf4j` o instanciar con una clase equivocada.
2. **Formato Parametrizado:** Usar placeholders `{}` para las variables, nunca concatenación de cadenas (`+`).
3. **Nivel ERROR:** Reservado exclusivamente para el bloque `catch (Exception ex)` de errores no previstos en los controladores o fallos de infraestructura/comunicación externa (como llamadas fallidas a Brevo o ApisPerú). Las excepciones de negocio esperadas (`BadRequestException`) no se registran como error crítico.
4. **Nivel INFO:** Utilizado para hitos operativos de negocio (ej. creación de solicitud, adjudicación de paseo, transición de estado, confirmación de correo).
5. **Nivel DEBUG:** Utilizado para inspección técnica detallada en desarrollo local.
6. **Seguridad y Confidencialidad:** Queda **estrictamente prohibido registrar datos sensibles** en los logs:
   - Tokens JWT completos.
   - Contraseñas o credenciales de usuarios.
   - Claves de API (`api-key` de Brevo u OCI).
   - Datos personales protegidos sin enmascarar.
