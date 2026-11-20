package com.paseape.apipaseape.infrastructure.controller;

import com.paseape.apipaseape.application.service.CatalogoService;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.catalogo.CatalogoBaseReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.catalogo.DistritoLimaReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.catalogo.TipoRazaReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.catalogo.TipoTamanoMascotaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.HeaderDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.dto.response.catalogo.CatalogoAdminResDto;
import com.paseape.apipaseape.infrastructure.dto.response.catalogo.DistritoLimaAdminResDto;
import com.paseape.apipaseape.infrastructure.dto.response.catalogo.TipoRazaAdminResDto;
import com.paseape.apipaseape.infrastructure.dto.response.catalogo.TipoTamanoMascotaAdminResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.shared.RequestService;
import com.paseape.apipaseape.infrastructure.validator.CatalogoValidator;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

@RestController
@RequestMapping(path = "catalogos", produces = "application/json")
@RequiredArgsConstructor
public class CatalogoController {

    private final RequestService requestService;
    private final CatalogoService catalogoService;
    private final CatalogoValidator catalogoValidator;

    private final Logger logger = LoggerFactory.getLogger(CatalogoController.class);

    // =========================================================================
    // 1. DISTRITOS LIMA
    // =========================================================================

    @Operation(summary = "Listar distritos de cobertura")
    @GetMapping("distritos")
    public ResponseEntity<ResponseDto<List<DistritoLimaAdminResDto>>> listarDistritos(
            Principal principal,
            @RequestParam(name = "estado", required = false) Integer estado
    ) {
        return ejecutarEndpoint(
                "listarDistritos",
                "estado=" + estado,
                StatusCodes.Code200,
                "Distritos recuperados exitosamente",
                () -> catalogoValidator.validateConsultaCatalogo(obtenerEmail(principal), estado),
                () -> catalogoService.listarDistritos(estado)
        );
    }

    @Operation(summary = "Crear distrito de cobertura")
    @PostMapping("distritos")
    public ResponseEntity<ResponseDto<DistritoLimaAdminResDto>> crearDistrito(
            Principal principal,
            @RequestBody DistritoLimaReqDto reqDto
    ) {
        return ejecutarEndpoint(
                "crearDistrito",
                reqDto,
                StatusCodes.Code201,
                "Distrito creado exitosamente",
                () -> catalogoValidator.validateDistrito(obtenerEmail(principal), null, reqDto),
                () -> catalogoService.crearDistrito(reqDto)
        );
    }

    @Operation(summary = "Actualizar distrito de cobertura")
    @PutMapping("distritos/{uuid}")
    public ResponseEntity<ResponseDto<DistritoLimaAdminResDto>> actualizarDistrito(
            Principal principal,
            @PathVariable String uuid,
            @RequestBody DistritoLimaReqDto reqDto
    ) {
        return ejecutarEndpoint(
                "actualizarDistrito",
                "uuid=" + uuid,
                StatusCodes.Code200,
                "Distrito actualizado exitosamente",
                () -> catalogoValidator.validateDistrito(obtenerEmail(principal), uuid, reqDto),
                () -> catalogoService.actualizarDistrito(uuid, reqDto)
        );
    }

    @Operation(summary = "Eliminar lógicamente distrito de cobertura")
    @DeleteMapping("distritos/{uuid}")
    public ResponseEntity<ResponseDto<DistritoLimaAdminResDto>> eliminarDistrito(
            Principal principal,
            @PathVariable String uuid
    ) {
        return ejecutarEndpoint(
                "eliminarDistrito",
                "uuid=" + uuid,
                StatusCodes.Code200,
                "Distrito desactivado exitosamente",
                () -> catalogoValidator.validateAccionPorUuid(obtenerEmail(principal), uuid),
                () -> catalogoService.eliminarDistrito(uuid)
        );
    }

    @Operation(summary = "Reactivar distrito de cobertura")
    @PatchMapping("distritos/{uuid}/activar")
    public ResponseEntity<ResponseDto<DistritoLimaAdminResDto>> activarDistrito(
            Principal principal,
            @PathVariable String uuid
    ) {
        return ejecutarEndpoint(
                "activarDistrito",
                "uuid=" + uuid,
                StatusCodes.Code200,
                "Distrito activado exitosamente",
                () -> catalogoValidator.validateAccionPorUuid(obtenerEmail(principal), uuid),
                () -> catalogoService.activarDistrito(uuid)
        );
    }

    // =========================================================================
    // 2. TIPOS DE MASCOTA
    // =========================================================================

    @Operation(summary = "Listar tipos de mascota")
    @GetMapping("tipos-mascota")
    public ResponseEntity<ResponseDto<List<CatalogoAdminResDto>>> listarTiposMascota(
            Principal principal,
            @RequestParam(name = "estado", required = false) Integer estado
    ) {
        return ejecutarEndpoint(
                "listarTiposMascota",
                "estado=" + estado,
                StatusCodes.Code200,
                "Tipos de mascota recuperados exitosamente",
                () -> catalogoValidator.validateConsultaCatalogo(obtenerEmail(principal), estado),
                () -> catalogoService.listarTiposMascota(estado)
        );
    }

    @Operation(summary = "Crear tipo de mascota")
    @PostMapping("tipos-mascota")
    public ResponseEntity<ResponseDto<CatalogoAdminResDto>> crearTipoMascota(
            Principal principal,
            @RequestBody CatalogoBaseReqDto reqDto
    ) {
        return ejecutarEndpoint(
                "crearTipoMascota",
                reqDto,
                StatusCodes.Code201,
                "Tipo de mascota creado exitosamente",
                () -> catalogoValidator.validateCatalogoBase(obtenerEmail(principal), null, reqDto, 50),
                () -> catalogoService.crearTipoMascota(reqDto)
        );
    }

    @Operation(summary = "Actualizar tipo de mascota")
    @PutMapping("tipos-mascota/{uuid}")
    public ResponseEntity<ResponseDto<CatalogoAdminResDto>> actualizarTipoMascota(
            Principal principal,
            @PathVariable String uuid,
            @RequestBody CatalogoBaseReqDto reqDto
    ) {
        return ejecutarEndpoint(
                "actualizarTipoMascota",
                "uuid=" + uuid,
                StatusCodes.Code200,
                "Tipo de mascota actualizado exitosamente",
                () -> catalogoValidator.validateCatalogoBase(obtenerEmail(principal), uuid, reqDto, 50),
                () -> catalogoService.actualizarTipoMascota(uuid, reqDto)
        );
    }

    @Operation(summary = "Eliminar lógicamente tipo de mascota")
    @DeleteMapping("tipos-mascota/{uuid}")
    public ResponseEntity<ResponseDto<CatalogoAdminResDto>> eliminarTipoMascota(
            Principal principal,
            @PathVariable String uuid
    ) {
        return ejecutarEndpoint(
                "eliminarTipoMascota",
                "uuid=" + uuid,
                StatusCodes.Code200,
                "Tipo de mascota desactivado exitosamente",
                () -> catalogoValidator.validateAccionPorUuid(obtenerEmail(principal), uuid),
                () -> catalogoService.eliminarTipoMascota(uuid)
        );
    }

    @Operation(summary = "Reactivar tipo de mascota")
    @PatchMapping("tipos-mascota/{uuid}/activar")
    public ResponseEntity<ResponseDto<CatalogoAdminResDto>> activarTipoMascota(
            Principal principal,
            @PathVariable String uuid
    ) {
        return ejecutarEndpoint(
                "activarTipoMascota",
                "uuid=" + uuid,
                StatusCodes.Code200,
                "Tipo de mascota activado exitosamente",
                () -> catalogoValidator.validateAccionPorUuid(obtenerEmail(principal), uuid),
                () -> catalogoService.activarTipoMascota(uuid)
        );
    }

    // =========================================================================
    // 3. RAZAS
    // =========================================================================

    @Operation(summary = "Listar razas de mascotas")
    @GetMapping("razas")
    public ResponseEntity<ResponseDto<List<TipoRazaAdminResDto>>> listarRazas(
            Principal principal,
            @RequestParam(name = "estado", required = false) Integer estado
    ) {
        return ejecutarEndpoint(
                "listarRazas",
                "estado=" + estado,
                StatusCodes.Code200,
                "Razas recuperadas exitosamente",
                () -> catalogoValidator.validateConsultaCatalogo(obtenerEmail(principal), estado),
                () -> catalogoService.listarRazas(estado)
        );
    }

    @Operation(summary = "Crear raza de mascota")
    @PostMapping("razas")
    public ResponseEntity<ResponseDto<TipoRazaAdminResDto>> crearRaza(
            Principal principal,
            @RequestBody TipoRazaReqDto reqDto
    ) {
        return ejecutarEndpoint(
                "crearRaza",
                reqDto,
                StatusCodes.Code201,
                "Raza creada exitosamente",
                () -> catalogoValidator.validateTipoRaza(obtenerEmail(principal), null, reqDto),
                () -> catalogoService.crearRaza(reqDto)
        );
    }

    @Operation(summary = "Actualizar raza de mascota")
    @PutMapping("razas/{uuid}")
    public ResponseEntity<ResponseDto<TipoRazaAdminResDto>> actualizarRaza(
            Principal principal,
            @PathVariable String uuid,
            @RequestBody TipoRazaReqDto reqDto
    ) {
        return ejecutarEndpoint(
                "actualizarRaza",
                "uuid=" + uuid,
                StatusCodes.Code200,
                "Raza actualizada exitosamente",
                () -> catalogoValidator.validateTipoRaza(obtenerEmail(principal), uuid, reqDto),
                () -> catalogoService.actualizarRaza(uuid, reqDto)
        );
    }

    @Operation(summary = "Eliminar lógicamente raza de mascota")
    @DeleteMapping("razas/{uuid}")
    public ResponseEntity<ResponseDto<TipoRazaAdminResDto>> eliminarRaza(
            Principal principal,
            @PathVariable String uuid
    ) {
        return ejecutarEndpoint(
                "eliminarRaza",
                "uuid=" + uuid,
                StatusCodes.Code200,
                "Raza desactivada exitosamente",
                () -> catalogoValidator.validateAccionPorUuid(obtenerEmail(principal), uuid),
                () -> catalogoService.eliminarRaza(uuid)
        );
    }

    @Operation(summary = "Reactivar raza de mascota")
    @PatchMapping("razas/{uuid}/activar")
    public ResponseEntity<ResponseDto<TipoRazaAdminResDto>> activarRaza(
            Principal principal,
            @PathVariable String uuid
    ) {
        return ejecutarEndpoint(
                "activarRaza",
                "uuid=" + uuid,
                StatusCodes.Code200,
                "Raza activada exitosamente",
                () -> catalogoValidator.validateAccionPorUuid(obtenerEmail(principal), uuid),
                () -> catalogoService.activarRaza(uuid)
        );
    }

    // =========================================================================
    // 4. TAMAÑOS DE MASCOTA
    // =========================================================================

    @Operation(summary = "Listar tamaños de mascota")
    @GetMapping("tamanos-mascota")
    public ResponseEntity<ResponseDto<List<TipoTamanoMascotaAdminResDto>>> listarTamanosMascota(
            Principal principal,
            @RequestParam(name = "estado", required = false) Integer estado
    ) {
        return ejecutarEndpoint(
                "listarTamanosMascota",
                "estado=" + estado,
                StatusCodes.Code200,
                "Tamaños de mascota recuperados exitosamente",
                () -> catalogoValidator.validateConsultaCatalogo(obtenerEmail(principal), estado),
                () -> catalogoService.listarTamanosMascota(estado)
        );
    }

    @Operation(summary = "Crear tamaño de mascota")
    @PostMapping("tamanos-mascota")
    public ResponseEntity<ResponseDto<TipoTamanoMascotaAdminResDto>> crearTamanoMascota(
            Principal principal,
            @RequestBody TipoTamanoMascotaReqDto reqDto
    ) {
        return ejecutarEndpoint(
                "crearTamanoMascota",
                reqDto,
                StatusCodes.Code201,
                "Tamaño de mascota creado exitosamente",
                () -> catalogoValidator.validateTipoTamanoMascota(obtenerEmail(principal), null, reqDto),
                () -> catalogoService.crearTamanoMascota(reqDto)
        );
    }

    @Operation(summary = "Actualizar tamaño de mascota")
    @PutMapping("tamanos-mascota/{uuid}")
    public ResponseEntity<ResponseDto<TipoTamanoMascotaAdminResDto>> actualizarTamanoMascota(
            Principal principal,
            @PathVariable String uuid,
            @RequestBody TipoTamanoMascotaReqDto reqDto
    ) {
        return ejecutarEndpoint(
                "actualizarTamanoMascota",
                "uuid=" + uuid,
                StatusCodes.Code200,
                "Tamaño de mascota actualizado exitosamente",
                () -> catalogoValidator.validateTipoTamanoMascota(obtenerEmail(principal), uuid, reqDto),
                () -> catalogoService.actualizarTamanoMascota(uuid, reqDto)
        );
    }

    @Operation(summary = "Eliminar lógicamente tamaño de mascota")
    @DeleteMapping("tamanos-mascota/{uuid}")
    public ResponseEntity<ResponseDto<TipoTamanoMascotaAdminResDto>> eliminarTamanoMascota(
            Principal principal,
            @PathVariable String uuid
    ) {
        return ejecutarEndpoint(
                "eliminarTamanoMascota",
                "uuid=" + uuid,
                StatusCodes.Code200,
                "Tamaño de mascota desactivado exitosamente",
                () -> catalogoValidator.validateAccionPorUuid(obtenerEmail(principal), uuid),
                () -> catalogoService.eliminarTamanoMascota(uuid)
        );
    }

    @Operation(summary = "Reactivar tamaño de mascota")
    @PatchMapping("tamanos-mascota/{uuid}/activar")
    public ResponseEntity<ResponseDto<TipoTamanoMascotaAdminResDto>> activarTamanoMascota(
            Principal principal,
            @PathVariable String uuid
    ) {
        return ejecutarEndpoint(
                "activarTamanoMascota",
                "uuid=" + uuid,
                StatusCodes.Code200,
                "Tamaño de mascota activado exitosamente",
                () -> catalogoValidator.validateAccionPorUuid(obtenerEmail(principal), uuid),
                () -> catalogoService.activarTamanoMascota(uuid)
        );
    }

    // =========================================================================
    // 5. NIVELES DE ENERGÍA
    // =========================================================================

    @Operation(summary = "Listar niveles de energía")
    @GetMapping("niveles-energia")
    public ResponseEntity<ResponseDto<List<CatalogoAdminResDto>>> listarNivelesEnergia(
            Principal principal,
            @RequestParam(name = "estado", required = false) Integer estado
    ) {
        return ejecutarEndpoint(
                "listarNivelesEnergia",
                "estado=" + estado,
                StatusCodes.Code200,
                "Niveles de energía recuperados exitosamente",
                () -> catalogoValidator.validateConsultaCatalogo(obtenerEmail(principal), estado),
                () -> catalogoService.listarNivelesEnergia(estado)
        );
    }

    @Operation(summary = "Crear nivel de energía")
    @PostMapping("niveles-energia")
    public ResponseEntity<ResponseDto<CatalogoAdminResDto>> crearNivelEnergia(
            Principal principal,
            @RequestBody CatalogoBaseReqDto reqDto
    ) {
        return ejecutarEndpoint(
                "crearNivelEnergia",
                reqDto,
                StatusCodes.Code201,
                "Nivel de energía creado exitosamente",
                () -> catalogoValidator.validateCatalogoBase(obtenerEmail(principal), null, reqDto, 50),
                () -> catalogoService.crearNivelEnergia(reqDto)
        );
    }

    @Operation(summary = "Actualizar nivel de energía")
    @PutMapping("niveles-energia/{uuid}")
    public ResponseEntity<ResponseDto<CatalogoAdminResDto>> actualizarNivelEnergia(
            Principal principal,
            @PathVariable String uuid,
            @RequestBody CatalogoBaseReqDto reqDto
    ) {
        return ejecutarEndpoint(
                "actualizarNivelEnergia",
                "uuid=" + uuid,
                StatusCodes.Code200,
                "Nivel de energía actualizado exitosamente",
                () -> catalogoValidator.validateCatalogoBase(obtenerEmail(principal), uuid, reqDto, 50),
                () -> catalogoService.actualizarNivelEnergia(uuid, reqDto)
        );
    }

    @Operation(summary = "Eliminar lógicamente nivel de energía")
    @DeleteMapping("niveles-energia/{uuid}")
    public ResponseEntity<ResponseDto<CatalogoAdminResDto>> eliminarNivelEnergia(
            Principal principal,
            @PathVariable String uuid
    ) {
        return ejecutarEndpoint(
                "eliminarNivelEnergia",
                "uuid=" + uuid,
                StatusCodes.Code200,
                "Nivel de energía desactivado exitosamente",
                () -> catalogoValidator.validateAccionPorUuid(obtenerEmail(principal), uuid),
                () -> catalogoService.eliminarNivelEnergia(uuid)
        );
    }

    @Operation(summary = "Reactivar nivel de energía")
    @PatchMapping("niveles-energia/{uuid}/activar")
    public ResponseEntity<ResponseDto<CatalogoAdminResDto>> activarNivelEnergia(
            Principal principal,
            @PathVariable String uuid
    ) {
        return ejecutarEndpoint(
                "activarNivelEnergia",
                "uuid=" + uuid,
                StatusCodes.Code200,
                "Nivel de energía activado exitosamente",
                () -> catalogoValidator.validateAccionPorUuid(obtenerEmail(principal), uuid),
                () -> catalogoService.activarNivelEnergia(uuid)
        );
    }

    // =========================================================================
    // 6. GÉNEROS DE MASCOTA
    // =========================================================================

    @Operation(summary = "Listar géneros de mascota")
    @GetMapping("generos-mascota")
    public ResponseEntity<ResponseDto<List<CatalogoAdminResDto>>> listarGenerosMascota(
            Principal principal,
            @RequestParam(name = "estado", required = false) Integer estado
    ) {
        return ejecutarEndpoint(
                "listarGenerosMascota",
                "estado=" + estado,
                StatusCodes.Code200,
                "Géneros de mascota recuperados exitosamente",
                () -> catalogoValidator.validateConsultaCatalogo(obtenerEmail(principal), estado),
                () -> catalogoService.listarGenerosMascota(estado)
        );
    }

    @Operation(summary = "Crear género de mascota")
    @PostMapping("generos-mascota")
    public ResponseEntity<ResponseDto<CatalogoAdminResDto>> crearGeneroMascota(
            Principal principal,
            @RequestBody CatalogoBaseReqDto reqDto
    ) {
        return ejecutarEndpoint(
                "crearGeneroMascota",
                reqDto,
                StatusCodes.Code201,
                "Género de mascota creado exitosamente",
                () -> catalogoValidator.validateCatalogoBase(obtenerEmail(principal), null, reqDto, 30),
                () -> catalogoService.crearGeneroMascota(reqDto)
        );
    }

    @Operation(summary = "Actualizar género de mascota")
    @PutMapping("generos-mascota/{uuid}")
    public ResponseEntity<ResponseDto<CatalogoAdminResDto>> actualizarGeneroMascota(
            Principal principal,
            @PathVariable String uuid,
            @RequestBody CatalogoBaseReqDto reqDto
    ) {
        return ejecutarEndpoint(
                "actualizarGeneroMascota",
                "uuid=" + uuid,
                StatusCodes.Code200,
                "Género de mascota actualizado exitosamente",
                () -> catalogoValidator.validateCatalogoBase(obtenerEmail(principal), uuid, reqDto, 30),
                () -> catalogoService.actualizarGeneroMascota(uuid, reqDto)
        );
    }

    @Operation(summary = "Eliminar lógicamente género de mascota")
    @DeleteMapping("generos-mascota/{uuid}")
    public ResponseEntity<ResponseDto<CatalogoAdminResDto>> eliminarGeneroMascota(
            Principal principal,
            @PathVariable String uuid
    ) {
        return ejecutarEndpoint(
                "eliminarGeneroMascota",
                "uuid=" + uuid,
                StatusCodes.Code200,
                "Género de mascota desactivado exitosamente",
                () -> catalogoValidator.validateAccionPorUuid(obtenerEmail(principal), uuid),
                () -> catalogoService.eliminarGeneroMascota(uuid)
        );
    }

    @Operation(summary = "Reactivar género de mascota")
    @PatchMapping("generos-mascota/{uuid}/activar")
    public ResponseEntity<ResponseDto<CatalogoAdminResDto>> activarGeneroMascota(
            Principal principal,
            @PathVariable String uuid
    ) {
        return ejecutarEndpoint(
                "activarGeneroMascota",
                "uuid=" + uuid,
                StatusCodes.Code200,
                "Género de mascota activado exitosamente",
                () -> catalogoValidator.validateAccionPorUuid(obtenerEmail(principal), uuid),
                () -> catalogoService.activarGeneroMascota(uuid)
        );
    }

    // =========================================================================
    // ENSAMBLADO ESTÁNDAR DE RESPUESTA Y MANEJO DE EXCEPCIONES
    // =========================================================================

    @FunctionalInterface
    private interface ServiceCall<T> {
        T execute() throws BadRequestException;
    }

    private <T> ResponseEntity<ResponseDto<T>> ejecutarEndpoint(
            String nombreMetodo,
            Object payloadLog,
            int statusExito,
            String mensajeExito,
            Supplier<ResponseDto<?>> validationCall,
            ServiceCall<T> serviceCall
    ) {
        var response = new ResponseDto<T>();
        Date startDatetime = Calendar.getInstance().getTime();
        try {
            ResponseDto<?> validationError = validationCall.get();
            if (validationError != null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            T resData = serviceCall.execute();

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(statusExito);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage(mensajeExito);
            response.setHeader(headerDto);
            response.setResponse(resData);
            return ResponseEntity.status(response.getStatusCode()).body(response);

        } catch (BadRequestException brex) {
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(brex.statusCode != null ? brex.statusCode : StatusCodes.Code400);
            response.setCode(brex.code != null ? brex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(brex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(response.getStatusCode()).body(response);

        } catch (Exception ex) {
            logger.error("Error no controlado en {} | Payload: {} | Causa: {}", nombreMetodo, payloadLog, ex.getMessage(), ex);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Ocurrió un error interno al procesar la solicitud");
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    private String obtenerEmail(Principal principal) {
        return principal != null ? principal.getName() : null;
    }
}
