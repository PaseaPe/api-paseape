# Validators

El validator rechaza una petición HTTP antes de que alcance la capa de servicio: comprueba presencia de campos obligatorios, formatos, rangos válidos y **existencia previa o estado** de las entidades referenciadas en la base de datos mediante puertos de lectura.

Devuelve un `ResponseDto<?>` con el detalle del error en caso de fallo, o `null` si el request es plenamente válido. No utiliza Bean Validation (`@Valid`).

**Arquitectura estándar (estilo Mortgage):**
El validador utiliza internamente el lanzamiento de `BadRequestException` dentro de un bloque `try/catch`, interceptando el error de validación para construir la respuesta estructurada mediante un método privado `BuildErrorForValidation(...)`.

---

## Qué Puede Inyectar

- **Sí:** Puertos de lectura `IXRepository` de `application/repository` (por ejemplo: `IClienteRepository`, `IPaseadorRepository`, `IDistritoLimaRepository`) para verificar existencia previa o estado activo.
- **No:** `IXJpaRepository`, Services ni otros validators.
- **Nunca escribe en base de datos** ni altera los datos del request (asignar valores por defecto es responsabilidad exclusiva del Service).

---

## Estructura Canónica (Estilo Mortgage)

```java
package com.paseape.apipaseape.infrastructure.validator;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IDistritoLimaRepository;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.DistritoLima;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudInmediataReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Component
public class SolicitudPaseoValidator {

    private final IClienteRepository clienteRepository;
    private final IDistritoLimaRepository distritoLimaRepository;

    private static final Pattern UUID_PATTERN = Pattern.compile(Constant.REGEX_UUID);

    public ResponseDto<?> validateCrearInmediata(CrearSolicitudInmediataReqDto reqDto) {
        try {
            // 1. Nulidad del request
            if (reqDto == null) {
                throw new BadRequestException("El cuerpo de la solicitud no puede estar vacío");
            }

            // 2. Obligatoriedad y formato de listas
            List<String> mascotaUuids = reqDto.getMascotaUuids();
            if (ObjectUtils.isEmpty(mascotaUuids)) {
                throw new BadRequestException("Debe seleccionar al menos una mascota para el paseo");
            }

            Set<String> uniqueUuids = new HashSet<>();
            for (String uuid : mascotaUuids) {
                this.validateUuid(uuid, "mascota");
                if (!uniqueUuids.add(uuid.toLowerCase())) {
                    throw new BadRequestException("La lista de mascotas contiene identificadores duplicados");
                }
            }

            // 3. Montos y valores numéricos
            if (reqDto.getTarifaHoraPen() == null || reqDto.getTarifaHoraPen().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("La tarifa propuesta por hora debe ser mayor a 0");
            }

            // 4. Existencia previa barata mediante puertos IXRepository
            if (reqDto.getDistritoId() == null || reqDto.getDistritoId() <= 0) {
                throw new BadRequestException("El distrito seleccionado es obligatorio y debe ser válido");
            }

            DistritoLima distrito = distritoLimaRepository.findById(reqDto.getDistritoId());
            if (distrito == null) {
                throw new BadRequestException("El distrito seleccionado no existe");
            }

            // 5. Cadenas de texto y longitud
            if (ObjectUtils.isEmpty(reqDto.getDireccionRecojo())) {
                throw new BadRequestException("La dirección de recojo es obligatoria");
            }

            if (reqDto.getDireccionRecojo().trim().length() > 255) {
                throw new BadRequestException("La dirección de recojo no debe superar los 255 caracteres");
            }

        } catch (BadRequestException brex) {
            return BuildErrorForValidation(
                    brex.statusCode != null ? brex.statusCode : StatusCodes.Code400,
                    brex.code != null ? brex.code : MessageCodes.ResponseCodeBR01,
                    brex.getMessage()
            );
        }

        return null;
    }

    // Regla reutilizable que lanza BadRequestException
    private void validateUuid(String uuid, String nombreEntidad) throws BadRequestException {
        if (ObjectUtils.isEmpty(uuid)) {
            throw new BadRequestException("El identificador de " + nombreEntidad + " no debe estar vacío");
        }
        if (!UUID_PATTERN.matcher(uuid.trim()).matches()) {
            throw new BadRequestException("El identificador de " + nombreEntidad + " no tiene un formato UUID válido");
        }
    }

    // Constructor privado estándar del error de validación
    private ResponseDto<?> BuildErrorForValidation(int statusCode, String code, String message) {
        var response = new ResponseDto<>();
        response.setStatusCode(statusCode);
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
}
```

---

## Reglas No Negociables

1. **Un método de validación por endpoint:** Nomenclatura `validate<Accion>` correspondiente al endpoint del controller (`crearInmediata` → `validateCrearInmediata`).
2. **Orden estricto de comprobaciones:**
   - Nulidad general del body.
   - Campos obligatorios ausentes o cadenas vacías.
   - Formatos (UUID canónico, expresiones regulares de DNI/Pasaporte, rangos de fecha).
   - Rangos numéricos y montos mayores a cero.
   - Existencia de claves foráneas mediante puertos (`findById` o `findByUuid`).
3. **Mensajes legibles y claros en español:** Describen con precisión el problema al usuario final sin exponer nombres de tablas, SQL ni detalles técnicos.
4. **Métodos privados reutilizables:** Para validaciones repetidas (como `validateUuid` o validación de documentos oficiales), extraer métodos auxiliares privados que declaren `throws BadRequestException`.
5. **Cero mutaciones:** El validator nunca modifica las propiedades del DTO ni le inyecta valores por omisión.

---

## No Hacer

```java
// ❌ Retornar DTOs intermedios propietarios como ErrorDetailDto en nuevo código
public ErrorDetailDto validarCrearSolicitud(...)

// ❌ Inyectar Services o repositorios JPA
private final SolicitudPaseoService service;
private final ISolicitudPaseoJpaRepository jpaRepository;

// ❌ Realizar cálculos de negocio complejos en el validator
// (El cálculo del costo del paseo o verificación de cupos pertenece al Service)
```
