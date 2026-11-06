# Services

La capa de servicio (`application/service`) contiene la lógica de negocio pura, la orquestación de casos de uso y la frontera transaccional. Es una clase concreta anotada con `@Service`, sin interfaz previa ni sufijo `Impl`.

Referencia de estructura y tamaño en PaseaPe: `application/service/SolicitudPaseoService.java`.

---

## Estructura Canónica

```java
package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IPaseadorRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoMascotaRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoRepository;
import com.paseape.apipaseape.domain.entity.*;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudInmediataReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudPaseoResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.ISolicitudPaseoDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SolicitudPaseoService {

    private final ISolicitudPaseoRepository solicitudPaseoRepository;
    private final ISolicitudPaseoMascotaRepository solicitudPaseoMascotaRepository;
    private final IClienteRepository clienteRepository;
    private final IPaseadorRepository paseadorRepository;
    private final ISolicitudPaseoDtoMapper solicitudPaseoDtoMapper;

    @Transactional(rollbackFor = Exception.class)
    public SolicitudPaseoResDto crearSolicitudInmediata(String userEmail, CrearSolicitudInmediataReqDto reqDto) throws BadRequestException {
        // 1. Resolver cliente autenticado
        Cliente cliente = clienteRepository.findByCorreo(userEmail);
        if (cliente == null) {
            throw new BadRequestException(MessageCodes.ResponseCodeBR01, "El cliente asociado a la sesión no existe.");
        }

        // 2. Validar que las mascotas existan, estén activas y pertenezcan al cliente
        List<Mascota> mascotas = this.validarYObtenerMascotas(cliente.getId(), reqDto.getMascotaUuids());

        // 3. Cálculo de negocio de tarifa congelada
        BigDecimal tarifaSnapshot = reqDto.getTarifaHoraPen();
        int minutos = 60; // según tipoDuracionPaseoId
        BigDecimal montoTotal = this.calcularMontoTotal(tarifaSnapshot, minutos, mascotas.size());

        // 4. Instanciar entidad con constructor mono-argumento en catálogos
        SolicitudPaseo solicitud = SolicitudPaseo.builder()
                .uuid(UUID.randomUUID().toString())
                .codigoReserva(this.generarCodigoReservaInmediato())
                .cliente(cliente)
                .distrito(new DistritoLima(reqDto.getDistritoId()))
                .tipoModalidadPaseo(new TipoModalidadPaseo(Constant.ID_MODALIDAD_INMEDIATO))
                .tipoDuracionPaseo(new TipoDuracionPaseo(reqDto.getTipoDuracionPaseoId()))
                .tipoMetodoPago(new TipoMetodoPago(reqDto.getTipoMetodoPagoId()))
                .solicitudPaseoEstado(new SolicitudPaseoEstado(Constant.ID_ESTADO_SOLICITUD_BUSCANDO))
                .tarifaHoraPenSnapshot(tarifaSnapshot)
                .montoTotal(montoTotal)
                .horaVencimiento(LocalDateTime.now().plusMinutes(5))
                .direccionRecojo(reqDto.getDireccionRecojo())
                .referenciaDireccion(reqDto.getReferenciaDireccion())
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();

        solicitud = solicitudPaseoRepository.save(solicitud);

        // 5. Persistir detalle relacional N:M
        this.guardarMascotasAsociadas(solicitud, mascotas);

        return solicitudPaseoDtoMapper.toResDto(solicitud);
    }

    private BigDecimal calcularMontoTotal(BigDecimal tarifaHora, int minutos, int totalMascotas) {
        return tarifaHora.divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(minutos))
                .multiply(BigDecimal.valueOf(totalMascotas))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
```

---

## Reglas Obligatorias

1. **Dependencias Autorizadas:**
   - Puertos de repositorio `IXRepository` (de `application/repository`).
   - Mappers MapStruct `IXDtoMapper` (de `infrastructure/mapper/dto`).
   - Otros Services concretos del dominio.
   - **Prohibido inyectar:** interfaces `IXJpaRepository`, clases adaptadoras de `infrastructure/repository`, entidades JPA (`*Entity`) o `EntityManager`.
2. **Prohibido el uso de `Optional`:** Los repositorios devuelven la entidad directa o `null`. Las comprobaciones de nulidad se realizan explícitamente en el servicio:
   ```java
   Paseador paseador = paseadorRepository.findByUuid(uuid);
   if (paseador == null) {
       throw new BadRequestException(MessageCodes.ResponseCodeBR01, "El paseador no existe.");
   }
   ```
3. **Constructor Mono-Argumento para Catálogos:** Al asociar entidades lookup table (`TipoModalidadPaseo`, `DistritoLima`, `TipoMetodoPago`, `BloqueHorario`), se instancia directamente con su ID:
   ```java
   solicitud.setDistrito(new DistritoLima(reqDto.getDistritoId()));
   ```
   No se debe consultar la base de datos si ya se cuenta con la clave primaria.
4. **Transaccionalidad Explícita:**
   - Mutaciones, escrituras y eliminaciones lógicas: `@Transactional(rollbackFor = Exception.class)`.
   - Consultas de solo lectura: `@Transactional(readOnly = true)`.
5. **Cero Código Duro (Hardcoding):**
   - Utilizar las constantes de `Constant.java` para estados (`Constant.ID_ESTADO_SOLICITUD_ACEPTADA`), roles (`Constant.ROL_CLIENTE`) y límites.
   - Códigos de error y respuestas usando `MessageCodes.java`.
6. **Estandarización Temporal:**
   - Cálculos y validaciones de hora civil de negocio (como franjas horarias y comparación con hora actual) se evalúan contra la zona horaria civil de Lima:
     ```java
     LocalDate hoyLima = LocalDate.now(ZoneId.of(Constant.ZONE_ID_LIMA));
     LocalTime ahoraLima = LocalTime.now(ZoneId.of(Constant.ZONE_ID_LIMA));
     ```
   - Las fechas de persistencia y auditoría de la JVM operan en UTC.

---

## No Hacer

```java
// ❌ Crear interfaz de servicio e implementación separada
public interface ISolicitudPaseoService { ... }
public class SolicitudPaseoServiceImpl implements ISolicitudPaseoService { ... }

// ❌ Inyectar repositorios Spring Data JPA directamente
private final ISolicitudPaseoJpaRepository jpaRepository;

// ❌ Números mágicos de estado en lugar de constantes
solicitud.setSolicitudPaseoEstado(new SolicitudPaseoEstado(2));

// ❌ Retornar DTOs JPA o Entidades JPA hacia el exterior
public SolicitudPaseoEntity obtenerPorId(Long id)
```
