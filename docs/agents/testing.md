# Testing

En PaseaPe, las pruebas unitarias se implementan con **JUnit 5 + Mockito**, sin levantar contexto de Spring (`@SpringBootTest`) para garantizar velocidad y aislamiento estricto de la lógica de negocio.

Referencia real en el proyecto: `src/test/java/com/paseape/apipaseape/application/service/SolicitudPaseoServiceTest.java`.

---

## Qué Se Testea

- **Todo Service nuevo o método modificado:** Debe contar con pruebas para cada una de sus ramas lógicas:
  - Camino feliz (éxito en la operación y retorno del DTO correspondiente).
  - Recurso inexistente o inactivo (lanzamiento de `BadRequestException`).
  - Cada validación de regla de negocio que interrumpe el flujo.
  - Verificación de que **no** se ejecuta persistencia cuando una regla falla:
    ```java
    verify(solicitudPaseoRepository, never()).save(any());
    ```
- **Validators dedicados:** Pruebas unitarias de formatos, listas vacías, duplicados y comprobaciones previas.
- **Evitar pruebas innecesarias:** No escribir tests unitarios triviales para adaptadores o getters/setters que solo delegan en Spring Data JPA o MapStruct.

---

## Ubicación y Nomenclatura

- **Ubicación:** Misma estructura de paquetes bajo el directorio de pruebas:
  `src/test/java/com/paseape/apipaseape/application/service/<XServiceTest.java>`
  `src/test/java/com/paseape/apipaseape/infrastructure/validator/<XValidatorTest.java>`
- **Nombre de la clase:** Sufijo `Test` (ej. `SolicitudPaseoServiceTest`, `PaseadorServiceTest`).
- **Nomenclatura de Métodos:** `test<Metodo>_<Escenario>` o `<metodo>_<escenario_resultado>`:
  - `testCrearSolicitudInmediata_Exito`
  - `testCrearSolicitudInmediata_MascotaAjena`
  - `testTomarSolicitudInmediata_RaceConditionYaTomada`

---

## Estructura Canónica del Test

```java
package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IMascotaRepository;
import com.paseape.apipaseape.application.repository.ISolicitudPaseoRepository;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.Mascota;
import com.paseape.apipaseape.domain.entity.SolicitudPaseo;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CrearSolicitudInmediataReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.SolicitudPaseoResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.mapper.dto.ISolicitudPaseoDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudPaseoServiceTest {

    @Mock
    private ISolicitudPaseoRepository solicitudPaseoRepository;
    @Mock
    private IClienteRepository clienteRepository;
    @Mock
    private IMascotaRepository mascotaRepository;

    private ISolicitudPaseoDtoMapper mapper;
    private SolicitudPaseoService service;

    private Cliente cliente;
    private Mascota mascota;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ISolicitudPaseoDtoMapper.class);
        // Instanciación del servicio inyectando los mocks
        service = new SolicitudPaseoService(
                solicitudPaseoRepository,
                clienteRepository,
                mascotaRepository,
                mapper
        );

        cliente = Cliente.builder()
                .id(1L)
                .uuid(UUID.randomUUID().toString())
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();

        mascota = Mascota.builder()
                .id(10L)
                .uuid(UUID.randomUUID().toString())
                .cliente(cliente)
                .estado(Constant.ESTADO_LOGICO_ACTIVO)
                .build();
    }

    @Test
    @DisplayName("crearSolicitudInmediata - Éxito al registrar solicitud")
    void testCrearSolicitudInmediata_Exito() throws BadRequestException {
        // given
        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(mascota.getUuid()))
                .distritoId(21)
                .tarifaHoraPen(new BigDecimal("25.00"))
                .direccionRecojo("Av. Primavera 123")
                .build();

        when(clienteRepository.findByCorreo("cliente@paseape.com")).thenReturn(cliente);
        when(mascotaRepository.findByUuid(mascota.getUuid())).thenReturn(mascota);
        when(solicitudPaseoRepository.save(any(SolicitudPaseo.class))).thenAnswer(i -> i.getArgument(0));

        // when
        SolicitudPaseoResDto result = service.crearSolicitudInmediata("cliente@paseape.com", req);

        // then
        assertNotNull(result);
        assertEquals(Constant.ID_ESTADO_SOLICITUD_BUSCANDO, result.getEstadoId());
        verify(solicitudPaseoRepository, times(1)).save(any(SolicitudPaseo.class));
    }

    @Test
    @DisplayName("crearSolicitudInmediata - Falla si la mascota no pertenece al cliente")
    void testCrearSolicitudInmediata_MascotaAjena() {
        // given
        Cliente otroCliente = Cliente.builder().id(99L).build();
        mascota.setCliente(otroCliente);

        var req = CrearSolicitudInmediataReqDto.builder()
                .mascotaUuids(List.of(mascota.getUuid()))
                .build();

        when(clienteRepository.findByCorreo("cliente@paseape.com")).thenReturn(cliente);
        when(mascotaRepository.findByUuid(mascota.getUuid())).thenReturn(mascota);

        // when & then
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.crearSolicitudInmediata("cliente@paseape.com", req));

        assertEquals(MessageCodes.ResponseCodeBR03, ex.code);
        verify(solicitudPaseoRepository, never()).save(any());
    }
}
```

---

## Comandos de Ejecución

```bash
# Ejecutar una suite de pruebas completa
./gradlew test --tests SolicitudPaseoServiceTest

# Ejecutar un método de prueba específico
./gradlew test --tests 'SolicitudPaseoServiceTest.testCrearSolicitudInmediata_Exito'

# Compilar sin correr pruebas
./gradlew clean compileJava -x test
```
