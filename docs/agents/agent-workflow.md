# Flujo de Trabajo del Agente

Este documento define la metodología y el protocolo de desarrollo que todo asistente o desarrollador debe seguir en el backend **api-paseape**.

---

## 1. Antes de Escribir Código

1. **Revisión de Especificaciones:** Consulta los archivos de `docs/agents/` pertinentes para la tarea (`architecture.md`, `controllers.md`, `validators.md`, `services.md`, `persistence.md`, `mappers-dtos.md`).
2. **Contexto de Negocio:** Revisa las historias de usuario y reglas de negocio vigentes en `documentacion/contexto_y_arquitectura.md` y `documentacion/sprint_2_historias_y_reglas.md`.
3. **Inspección de Referencias:** Abre la clase de referencia del mismo agregado o módulo. **Antes de copiar código existente, revisa obligatoriamente `deviations.md`** para asegurarte de no reproducir patrones obsoletos (como heredar de `BaseController` o usar `ErrorDetailDto`).
4. **Verificación del Entorno:** Trabaja en la rama de feature correspondiente y verifica que el working tree esté limpio.

---

## 2. Puntos de Control: Detente y Pregunta Antes de...

- Modificar contratos públicos (`*ResDto` o rutas de endpoints) que son consumidos activamente por la aplicación móvil `app-android-paseape`.
- Modificar, renombrar o eliminar una migración Flyway que ya fue aplicada en producción o `main`.
- Crear capas o patrones arquitectónicos no contemplados en `architecture.md`.
- Cambiar la versión de dependencias en `build.gradle` o alterar la configuración central de seguridad JWT.
- Refactorizar módulos completos que no forman parte del alcance de la tarea actual.

---

## 3. Comandos de Verificación Permitidos

Puedes ejecutar los siguientes comandos de forma autónoma para validar tus cambios:

```bash
# Compilar el proyecto verificando mappers y tipos
./gradlew compileJava

# Compilación completa omitiendo pruebas
./gradlew build -x test

# Ejecutar una clase de prueba específica
./gradlew test --tests <NombreDeClaseTest>

# Inspeccionar el estado de git
git status
git diff
```

---

## 4. Estándar de Commits

- **Atomicidad:** Un commit por componente o tarea lógica (migración de BD, puerto + adaptador, servicio + pruebas).
- **Convención:** Formato Conventional Commits en minúsculas (`feat: ...`, `fix: ...`, `chore: ...`).
- **Verificación previa:** Cada commit debe compilar exitosamente sin errores de compilación ni fallos en pruebas unitarias.

---

## 5. Checklist Obligatorio Antes de Finalizar la Tarea

- [ ] `./gradlew compileJava` compila limpiamente sin advertencias críticas.
- [ ] Las pruebas unitarias del servicio abarcan caminos de éxito y las excepciones de `BadRequestException`.
- [ ] **Controllers:** Siguen el estándar Mortgage (no heredan de `BaseController`, logger propio, try/catch explícito).
- [ ] **Validators:** Siguen el estándar Mortgage (lanzan `BadRequestException` internamente y retornan vía `BuildErrorForValidation`).
- [ ] **DTOs:** Modelados como clases estándar Lombok (`@Getter`, `@Setter`, etc.) sin `record` y sin anotaciones de validación de Jakarta en `*ReqDto`.
- [ ] **Persistencia:** Repositorios en `application/repository` retornan entidad o `null` (nunca `Optional<T>`).
- [ ] **Entidades JPA:** Llevan `@SQLRestriction("estado = 1")` para borrado lógico (salvo catálogos).
- [ ] **Identificación:** Todo contrato público expone `uuid` en lugar de IDs numéricos autoincrementales.
- [ ] **Cero Hardcoding:** Valores numéricos, roles y estados se leen desde `Constant`, `StatusCodes` y `MessageCodes`.
- [ ] **Fechas:** Cálculos civiles evaluados bajo la zona horaria `America/Lima`.
- [ ] Sin `System.out.println` ni logs que expongan credenciales o datos sensibles.
