package com.paseape.apipaseape.infrastructure.controller;

import com.paseape.apipaseape.application.service.DigitalDocumentService;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.dto.response.*;
import com.paseape.apipaseape.infrastructure.validator.StorageValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.paseape.apipaseape.application.service.MascotaService;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.ActualizarMascotaReqDto;
import com.paseape.apipaseape.infrastructure.dto.request.RegistrarMascotaReqDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.shared.RequestService;
import com.paseape.apipaseape.infrastructure.validator.MascotaValidator;

import java.security.Principal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/mascotas")
@RequiredArgsConstructor
public class MascotaController extends BaseController {

    private final MascotaService mascotaService;
    private final MascotaValidator mascotaValidator;
    private final RequestService requestService;
    private final DigitalDocumentService digitalDocumentService;
    private final StorageValidator storageValidator;

    @PostMapping
    public ResponseEntity<ResponseDto<MascotaResDto>> registrarMascota(@RequestBody RegistrarMascotaReqDto request) {
        var response = new ResponseDto<MascotaResDto>();
        try {
            Date startDatetime = Calendar.getInstance().getTime();

            ErrorDetailDto validationError = mascotaValidator.validateRegistroUnitario(request);
            if (validationError != null) {
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            MascotaResDto mascotaCreada = mascotaService.registrarMascotaUnitaria(request);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code201);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Mascota registrada exitosamente para el cliente");
            response.setHeader(headerDto);
            response.setResponse(mascotaCreada);
            return ResponseEntity.status(StatusCodes.Code201).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "usuarioUuid=" + (request != null ? request.getUsuarioUuid() : "null"));
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "usuarioUuid=" + (request != null ? request.getUsuarioUuid() : "null"));
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al registrar la mascota");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @PutMapping("/{mascotaUuid}")
    public ResponseEntity<ResponseDto<MascotaResDto>> actualizarMascota(Principal principal, @PathVariable String mascotaUuid, @RequestBody ActualizarMascotaReqDto request) {
        var response = new ResponseDto<MascotaResDto>();
        try {
            Date startDatetime = Calendar.getInstance().getTime();

            if (principal == null || principal.getName() == null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(StatusCodes.Code401);
                response.setCode(MessageCodes.ResponseCodeE01);
                response.setMessage("No se encontro una sesion activa autenticada");
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            // El Principal del filtro JWT es el correo del usuario autenticado
            Usuario usuario = mascotaService.obtenerUsuarioPorCorreo(principal.getName());
            if (usuario == null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(StatusCodes.Code404);
                response.setCode(MessageCodes.ResponseCodeBR98);
                response.setMessage("Usuario no encontrado");
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            Cliente cliente = mascotaService.obtenerClientePorUsuarioId(usuario.getId());
            if (cliente == null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(StatusCodes.Code403);
                response.setCode(MessageCodes.ResponseCodeBR04);
                response.setMessage("El usuario no posee un perfil de cliente");
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            ErrorDetailDto validationError = mascotaValidator.validateActualizarMascota(request);
            if (validationError != null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            MascotaResDto mascotaActualizada = mascotaService.actualizarMascota(mascotaUuid, cliente.getId(), request);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Mascota actualizada exitosamente");
            response.setHeader(headerDto);
            response.setResponse(mascotaActualizada);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "mascotaUuid=" + mascotaUuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "mascotaUuid=" + mascotaUuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al actualizar la mascota");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @GetMapping("/mascota/{uuid}")
    public ResponseEntity<ResponseDto<MascotaResDto>> obtenerMascota(@PathVariable String uuid) {
        var response = new ResponseDto<MascotaResDto>();
        try {
            Date startDatetime = Calendar.getInstance().getTime();

            MascotaResDto mascotaResDto = mascotaService.obtenerMascotaPorUuid(uuid);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Mascota obtenido exitosamente");
            response.setHeader(headerDto);
            response.setResponse(mascotaResDto);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al obtener el paseador");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @GetMapping("/mascotas/{uuid}")
    public ResponseEntity<ResponseDto<List<MascotaResDto>>> obtenerMascotas(@PathVariable String uuid) {
        var response = new ResponseDto<List<MascotaResDto>>();
        try {
            Date startDatetime = Calendar.getInstance().getTime();

            List<MascotaResDto> mascotas = mascotaService.listarMascotasPorUsuario(uuid);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Mascotas obtenidas exitosamente");
            response.setHeader(headerDto);
            response.setResponse(mascotas);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "uuid=" + uuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al obtener el paseador");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @PostMapping("/{mascotaUuid}/fotos")
    public ResponseEntity<ResponseDto<DocumentUrlResDto>> subirFotoMascota(
            Principal principal,
            @PathVariable String mascotaUuid,
            @RequestParam("file") MultipartFile file) {
        var response = new ResponseDto<DocumentUrlResDto>();
        try {
            Date startDatetime = Calendar.getInstance().getTime();

            if (principal == null || principal.getName() == null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(StatusCodes.Code401);
                response.setCode(MessageCodes.ResponseCodeE01);
                response.setMessage("No se encontro una sesion activa autenticada");
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            Usuario usuario = mascotaService.obtenerUsuarioPorCorreo(principal.getName());
            if (usuario == null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(StatusCodes.Code404);
                response.setCode(MessageCodes.ResponseCodeBR98);
                response.setMessage("Usuario no encontrado");
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            Cliente cliente = mascotaService.obtenerClientePorUsuarioId(usuario.getId());
            if (cliente == null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(StatusCodes.Code403);
                response.setCode(MessageCodes.ResponseCodeBR04);
                response.setMessage("El usuario no posee un perfil de cliente");
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            ErrorDetailDto fileError = storageValidator.validateUpload(file);
            if (fileError != null) {
                Date endDatetime = Calendar.getInstance().getTime();
                response.setStatusCode(fileError.getStatusCode());
                response.setCode(fileError.getCode());
                response.setMessage(fileError.getMessage());
                response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            DocumentUrlResDto docDto = digitalDocumentService.subirFotoMascota(mascotaUuid, cliente.getId(), file);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code201);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Foto de mascota almacenada exitosamente");
            response.setHeader(headerDto);
            response.setResponse(docDto);
            return ResponseEntity.status(StatusCodes.Code201).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "mascotaUuid=" + mascotaUuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "mascotaUuid=" + mascotaUuid);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al subir la foto de la mascota");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }
}
